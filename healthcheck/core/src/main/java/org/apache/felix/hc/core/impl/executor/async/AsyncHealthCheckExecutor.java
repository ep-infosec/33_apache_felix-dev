/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.felix.hc.core.impl.executor.async;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.execution.HealthCheckExecutionResult;
import org.apache.felix.hc.api.execution.HealthCheckMetadata;
import org.apache.felix.hc.api.execution.HealthCheckSelector;
import org.apache.felix.hc.core.impl.executor.ExecutionResult;
import org.apache.felix.hc.core.impl.executor.HealthCheckExecutorThreadPool;
import org.apache.felix.hc.core.impl.executor.HealthCheckFuture;
import org.apache.felix.hc.core.impl.executor.HealthCheckFuture.Callback;
import org.apache.felix.hc.core.impl.executor.HealthCheckResultCache;
import org.apache.felix.hc.core.impl.scheduling.AsyncIntervalJob;
import org.apache.felix.hc.core.impl.scheduling.AsyncJob;
import org.apache.felix.hc.core.impl.scheduling.CronJobFactory;
import org.apache.felix.hc.core.impl.util.HealthCheckFilter;
import org.apache.felix.hc.core.impl.util.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Runs health checks asynchronously, either via cron or via interval. */
@Component(service = AsyncHealthCheckExecutor.class, immediate = true)
public class AsyncHealthCheckExecutor implements ServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncHealthCheckExecutor.class);

    private Map<HealthCheckMetadata, ExecutionResult> asyncResultsByDescriptor = new ConcurrentHashMap<>();

    private Map<HealthCheckMetadata, AsyncJob> registeredJobs = new HashMap<>();

    private BundleContext bundleContext;

    @Reference
    HealthCheckExecutorThreadPool healthCheckExecutorThreadPool;
    
    @Reference
    CronJobFactory cronJobFactory;

    @Activate
    protected final void activate(final BundleContext bundleContext) throws InvalidSyntaxException {
        this.bundleContext = bundleContext;
        this.bundleContext.addServiceListener(this, "(objectclass=" + HealthCheck.class.getName() + ")");
        int count = 0;
        HealthCheckFilter healthCheckFilter = new HealthCheckFilter(bundleContext);
        final ServiceReference<?>[] healthCheckReferences = healthCheckFilter.getHealthCheckServiceReferences(HealthCheckSelector.empty(), false);
        for (ServiceReference<?> serviceReference : healthCheckReferences) {
            HealthCheckMetadata healthCheckMetadata = new HealthCheckMetadata(serviceReference);
            if (isAsync(healthCheckMetadata)) {
                if (scheduleHealthCheck(healthCheckMetadata)) {
                    count++;
                }
            }
        }
        LOG.debug("Scheduled {} jobs for asynchronous health checks during bundle startup", count);
    }

    @Deactivate
    protected final void deactivate() {
        this.bundleContext.removeServiceListener(this);
        this.bundleContext = null;

        LOG.debug("Unscheduling {} jobs for asynchronous health checks", registeredJobs.size());
        for (HealthCheckMetadata healthCheckDescriptor : new LinkedList<HealthCheckMetadata>(registeredJobs.keySet())) {
            unscheduleHealthCheck(healthCheckDescriptor);
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (bundleContext == null) {
            // already deactivated?
            return;
        }
        ServiceReference<?> serviceReference = event.getServiceReference();
        final boolean isHealthCheck = serviceReference.isAssignableTo(bundleContext.getBundle(), HealthCheck.class.getName());

        if (isHealthCheck) {
            HealthCheckMetadata healthCheckMetadata = new HealthCheckMetadata(serviceReference);
            int eventType = event.getType();
            if (eventType == ServiceEvent.REGISTERED) {
                LOG.debug("Received service event REGISTERED for health check {}", healthCheckMetadata);
                scheduleHealthCheck(healthCheckMetadata);
            } else if (eventType == ServiceEvent.UNREGISTERING) {
                LOG.debug("Received service event UNREGISTERING for health check {}", healthCheckMetadata);
                unscheduleHealthCheck(healthCheckMetadata);
            } else if (eventType == ServiceEvent.MODIFIED) {
                LOG.debug("Received service event MODIFIED for health check {}", healthCheckMetadata);
                unscheduleHealthCheck(healthCheckMetadata);
                scheduleHealthCheck(healthCheckMetadata);
            }

        }
    }

    private boolean scheduleHealthCheck(HealthCheckMetadata descriptor) {
        try {
            AsyncJob healthCheckAsyncJob = null;
            if (isAsyncCron(descriptor)) {
                healthCheckAsyncJob = cronJobFactory.createAsyncCronJob(getAsyncJob(descriptor), "job-hc-" + descriptor.getTitle().replaceAll("\\s+","-"), "async-healthchecks", descriptor.getAsyncCronExpression());
            } else if (isAsyncInterval(descriptor)) {
                healthCheckAsyncJob = new AsyncIntervalJob(getAsyncJob(descriptor), healthCheckExecutorThreadPool, descriptor.getAsyncIntervalInSec());
            }
            if (healthCheckAsyncJob != null) {
                healthCheckAsyncJob.schedule();
                registeredJobs.put(descriptor, healthCheckAsyncJob);
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            LOG.warn("Could not schedule async health check for " + descriptor + ". Exception: " + e, e);
            return false;
        }
    }

    private Runnable getAsyncJob(HealthCheckMetadata descriptor) {

        return new Runnable() {
            @Override
            public void run() {
                LOG.debug("Running job {}", this);
                HealthCheckFuture healthCheckFuture = new HealthCheckFuture(descriptor, bundleContext, new Callback() {

                    @Override
                    public void finished(HealthCheckExecutionResult result) {
                        updateWith(result);
                    }
                });

                // run future in same thread (as we are already async via scheduler)
                healthCheckFuture.run();
            }

            @Override
            public String toString() {
                return descriptor.toString();
            }
        };
    }

    private boolean unscheduleHealthCheck(HealthCheckMetadata descriptor) {

        // here no check for isAsync must be used to ensure previously
        // scheduled async checks are correctly unscheduled if they have
        // changed from async to sync.

        AsyncJob job = registeredJobs.remove(descriptor);
        if (job != null) {
            return job.unschedule();
        } else {
            LOG.debug("No job was unregistered for descriptor {}", descriptor);
            return false;
        }
    }

    /** Called by the main Executor to get results from async HCs */
    public void collectAsyncResults(List<HealthCheckMetadata> healthCheckDescriptors, Collection<HealthCheckExecutionResult> results,
            HealthCheckResultCache cache) {
        Iterator<HealthCheckMetadata> checksIt = healthCheckDescriptors.iterator();

        Set<ExecutionResult> asyncResults = new TreeSet<ExecutionResult>();
        while (checksIt.hasNext()) {
            HealthCheckMetadata healthCheckMetadata = checksIt.next();
            if (isAsync(healthCheckMetadata)) {
                ExecutionResult result = asyncResultsByDescriptor.get(healthCheckMetadata);
                if (result == null) {
                    result = handleMissingResult(healthCheckMetadata);
                }
                asyncResults.add(result);
                // remove from HC collection to not execute the check in HealthCheckExecutorImpl
                checksIt.remove();
            }
        }

        LOG.debug("Caching {} results from async results", asyncResults.size());
        for (ExecutionResult result : asyncResults) {
            cache.updateWith(result);
        }

        LOG.debug("Adding {} results from async results", asyncResults.size());
        results.addAll(asyncResults);

    }

    private ExecutionResult handleMissingResult(HealthCheckMetadata healthCheckMetadata) {
        ExecutionResult result;
        if(isAsyncCron(healthCheckMetadata)) {
            if(registeredJobs.containsKey(healthCheckMetadata)) {
                result = new ExecutionResult(healthCheckMetadata,
                        new Result(Result.Status.OK, "Async Health Check with cron expression '" + healthCheckMetadata.getAsyncCronExpression() + 
                                "' has not yet been executed."), 0L);
            } else {
                result = new ExecutionResult(healthCheckMetadata,
                        new Result(Result.Status.WARN, "Async Health Check with cron expression '" + healthCheckMetadata.getAsyncCronExpression() + 
                                "' is never executed because of misconfiguration."), 0L);
            }

        } else {
            result = new ExecutionResult(healthCheckMetadata,
                    new Result(Result.Status.OK, "Async Health Check with interval '" + healthCheckMetadata.getAsyncIntervalInSec() + 
                            "' has not yet been executed."), 0L);
        }
        return result;
    }

    public void updateWith(HealthCheckExecutionResult result) {
        if (isAsync(result.getHealthCheckMetadata())) {
            asyncResultsByDescriptor.put(result.getHealthCheckMetadata(), (ExecutionResult) result);
            LOG.debug("Updated result for async hc {} with {}", result.getHealthCheckMetadata(), result);
        }
    }

    private boolean isAsync(HealthCheckMetadata healthCheckMetadata) {
        return isAsyncCron(healthCheckMetadata) || isAsyncInterval(healthCheckMetadata);
    }

    private boolean isAsyncCron(HealthCheckMetadata healthCheckMetadata) {
        return StringUtils.isNotBlank(healthCheckMetadata.getAsyncCronExpression());
    }

    private boolean isAsyncInterval(HealthCheckMetadata healthCheckMetadata) {
        return healthCheckMetadata.getAsyncIntervalInSec() != null && healthCheckMetadata.getAsyncIntervalInSec() > 0L;
    }

}
