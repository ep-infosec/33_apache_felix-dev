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

package org.apache.felix.ipojo.runtime.test.dependencies.timeout.components;

import org.apache.felix.ipojo.runtime.test.dependencies.timeout.services.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

public class CollectionCheckServiceProvider implements CheckService {
    
    /**
     * Temporal dependency.
     */
    private Collection<FooService> fs;

    public boolean check() {
        boolean result = true;
        //Use a local variable to avoid to wait at each access.
        Collection<FooService> col = fs;
        if (col  != null) {
            for (FooService svc : col) {
                result = result && svc.foo();
            }
        }        
        return result;
    }

    public Properties getProps() {
        Iterator<FooService> it = fs.iterator();
        if (it.hasNext()) {
            FooService svc = it.next();
            return svc.fooProps();
        }
        return null;
    }

}
