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
package org.apache.felix.hc.core.impl.executor;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Apache Felix Health Check Executor", description = "Runs health checks for a given list of tags in parallel.")
@interface HealthCheckExecutorImplConfiguration {

    long TIMEOUT_DEFAULT_MS = 2000L;

    long LONGRUNNING_FUTURE_THRESHOLD_CRITICAL_DEFAULT_MS = 1000L * 60 * 5; // 5 min default

    long RESULT_CACHE_TTL_DEFAULT_MS = 1000L * 2;

    long TEMPORARILY_UNAVAILABLE_GRACE_PERIOD_DEFAULT_MS = 1000L * 60 * 10; // 10 min default

    @AttributeDefinition(name = "Timeout", description = "Timeout in ms until a check is marked as timed out")
    long timeoutInMs() default TIMEOUT_DEFAULT_MS;

    @AttributeDefinition(name = "Timeout threshold for CRITICAL", description = "Threshold in ms until a check is marked as 'exceedingly' timed out and will marked CRITICAL instead of WARN only")
    long longRunningFutureThresholdForCriticalMs() default LONGRUNNING_FUTURE_THRESHOLD_CRITICAL_DEFAULT_MS;

    @AttributeDefinition(name = "Results Cache TTL in Ms", description = "Result Cache time to live - results will be cached for the given time")
    long resultCacheTtlInMs() default RESULT_CACHE_TTL_DEFAULT_MS;

    @AttributeDefinition(name = "TEMPORARILY_UNAVAILABLE Grace Period", description = "Grace period in ms until a continuously reported TEMPORARILY_UNAVAILABLE check becomes CRITICAL")
    long temporarilyAvailableGracePeriodInMs() default TEMPORARILY_UNAVAILABLE_GRACE_PERIOD_DEFAULT_MS;

    @AttributeDefinition(name = "Default Tags", description = "Default tags to be executed if no tags are explicitly supplied")
    String[] defaultTags() default {"default"};

    @AttributeDefinition(name = "Auto Logging", description = "If enabled, will automatically log entries of ResultLog (or FormattingResultLog resp.) using Log4j. The logging category used is the class instantiating ResultLog prefixed with 'healthchecks.', for instance 'healthchecks.com.mycorp.myplatform.mymodule.ModuleCheck'.")
    boolean autoLogging() default false;
}
