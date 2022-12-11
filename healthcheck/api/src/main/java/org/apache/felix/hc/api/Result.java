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
package org.apache.felix.hc.api;

import java.util.Iterator;

/** The result of executing a {@link HealthCheck} */
public class Result implements Iterable<ResultLog.Entry> {

    /** The status of a {@link Result}. */
    public enum Status {
        OK, // system is fully operational
        WARN, // attention required but system is operational 
        TEMPORARILY_UNAVAILABLE, // system is not operational, it may become available soon
        CRITICAL, // critical problem exists, system should not be used
        HEALTH_CHECK_ERROR // health check itself did not execute properly (no reliable status is known)
    }

    protected final ResultLog resultLog;

    /** Build a single-value Result
     * 
     * @param s if lower than OK, our status is set to OK
     * @param explanation message for the status  */
    public Result(final Status s, final String explanation) {
        resultLog = new ResultLog().add(new ResultLog.Entry(s, explanation));
    }

    /** Build a single-value Result with exception
     * 
     * @param s if lower than OK, our status is set to OK 
     * @param explanation message for the status
     * @param e the exception for this Result */
    public Result(final Status s, final String explanation, final Exception e) {
        resultLog = new ResultLog().add(new ResultLog.Entry(s, explanation, e));
    }

    /** Build a a Result based on a ResultLog, which can provide more details than a single-value Result
     * @param log the log to base the result on*/
    public Result(final ResultLog log) {
        resultLog = new ResultLog(log);
    }

    /** True if our status is OK - provides a convenient way of checking that
     * @return true if the status is ok. */
    public boolean isOk() {
        return getStatus().equals(Status.OK);
    }

    /** @return the status of this result */
    public Status getStatus() {
        return resultLog.getAggregateStatus();
    }

    /** @return an Iterator on the entries of our ResultLog */
    @Override
    public Iterator<ResultLog.Entry> iterator() {
        return resultLog.iterator();
    }

    @Override
    public String toString() {
        return "Result [status=" + getStatus() + ", resultLog=" + resultLog + "]";
    }

}