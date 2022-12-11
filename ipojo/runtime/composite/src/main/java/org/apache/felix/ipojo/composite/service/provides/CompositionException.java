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
package org.apache.felix.ipojo.composite.service.provides;

/**
 * Exception occurs when a composition error occurs.
 * 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class CompositionException extends Exception {
    //TODO consider removing this class to use configuration exception.
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -3063353267573738105L;

    /**
     * Constructor.
     * @param message a message.
     */
    public CompositionException(String message) {
        this(message, null);
    }

    public CompositionException(String message, Throwable cause) {
        super(message, cause);
    }
}
