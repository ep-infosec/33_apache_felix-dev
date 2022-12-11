/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scr.integration.components.metadata.cache;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator
{

    private ServiceRegistration<?> registration;

    private SimpleService simpleService;

    @SuppressWarnings("unused")
    private void myActivate(BundleContext context, Map<?, ?> configuration)
    {
        registration = context.registerService(SimpleService.class.getName(),
            new SimpleServiceImpl(), null);
    }

    @SuppressWarnings("unused")
    private void myDeactivate(Map<?, ?> configuration)
    {
        if (registration != null)
        {
            registration.unregister();
            registration = null;
        }
    }

    public SimpleService getSimpleService()
    {
        return simpleService;
    }

    @SuppressWarnings("unused")
    private void bindService(SimpleService simpleService)
    {
        this.simpleService = simpleService;
    }

    @SuppressWarnings("unused")
    private void unbindService(SimpleService simpleService)
    {
        if (this.simpleService == simpleService)
        {
            this.simpleService = null;
        }
    }
}
