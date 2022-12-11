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
package org.apache.felix.hc.core.impl.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.felix.hc.core.impl.util.lang.StringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsNotBlank() {
        assertTrue(StringUtils.isNotBlank("test"));
        assertTrue(StringUtils.isNotBlank("$"));
        assertTrue(StringUtils.isNotBlank("123"));
        assertFalse(StringUtils.isNotBlank("    "));
        assertFalse(StringUtils.isNotBlank("\t"));
    }
    
    @Test
    public void testIsBlank() {
        assertFalse(StringUtils.isBlank("test"));
        assertFalse(StringUtils.isBlank("$"));
        assertFalse(StringUtils.isBlank("123"));
        assertTrue(StringUtils.isBlank("    "));
        assertTrue(StringUtils.isBlank("\t"));
    }

}
