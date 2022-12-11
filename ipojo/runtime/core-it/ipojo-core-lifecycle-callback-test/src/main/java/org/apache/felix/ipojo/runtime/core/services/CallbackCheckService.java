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
package org.apache.felix.ipojo.runtime.core.services;

import org.apache.felix.ipojo.runtime.core.components.ParentClass;

import java.util.Properties;

public class CallbackCheckService extends ParentClass implements CheckService {

    int i = 0;

    FooService fs;

    public static CallbackCheckService singleton;

    public static int count = 0;

    private static CallbackCheckService singleton() {
        if (singleton == null) {
            count++;
            singleton = new CallbackCheckService();
        }
        return singleton;
    }

    public static CallbackCheckService several() {
        count++;
        return new CallbackCheckService();
    }

    private void start() {
        i++;
    }

    protected void stop() {
        i++;
    }

    public boolean check() {
        return fs.foo();
    }

    public Properties getProps() {
        Properties p = new Properties();
        p.put("int", new Integer(i));
        p.put("count", new Integer(count));
        return p;
    }

}
