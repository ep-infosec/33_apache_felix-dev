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
package org.apache.felix.gogo.options;

import java.util.List;

public interface Option {
    /**
     * stop parsing on the first unknown option. This allows one parser to get its own options and
     * then pass the remaining options to another parser.
     *
     * @param stopOnBadOption stopOnBadOption
     * @return Option
     */
    Option setStopOnBadOption(boolean stopOnBadOption);

    /**
     * require options to precede args. Default is false, so options can appear between or after
     * args.
     *
     * @param optionsFirst optionsFirst
     * @return Option
     */
    Option setOptionsFirst(boolean optionsFirst);

    /**
     * parse arguments. If skipArgv0 is true, then parsing begins at arg1. This allows for commands
     * where argv0 is the command name rather than a real argument.
     *
     * @param argv argv
     * @param skipArg0 skipArg0
     * @return Option
     */
    Option parse(List<?> argv, boolean skipArg0);

    /**
     * parse arguments.
     *
     * See {@link #parse(List, boolean)}
     *
     * @param argv the arg
     * @return Option
     */
    Option parse(List<?> argv);

    /**
     * parse arguments.
     *
     * See {@link #parse(List, boolean)}
     *
     * @param argv the arg
     * @param skipArg0 skipArg0
     * @return Option
     */
    Option parse(Object[] argv, boolean skipArg0);

    /**
     * parse arguments.
     *
     * See {@link #parse(List, boolean)}
     *
     * @param argv argv
     * @return Option
     */
    Option parse(Object[] argv);

    /**
     * test whether specified option has been explicitly set.
     *
     * @param name name
     * @return boolean
     */
    boolean isSet(String name);

    /**
     * get value of named option. If multiple options given, this method returns the last one. Use
     * {@link #getList(String)} to get all values.
     *
     * @param name the name
     * @return String
     * @throws IllegalArgumentException
     *             if value is not a String.
     */
    String get(String name);

    /**
     * get list of all values for named option.
     *
     * @param name the name
     * @return empty list if option not given and no default specified.
     * @throws IllegalArgumentException
     *             if all values are not Strings.
     */
    List<String> getList(String name);

    /**
     * get value of named option as an Object. If multiple options given, this method returns the
     * last one. Use {@link #getObjectList(String)} to get all values.
     *
     * @param name the name
     * @return Object
     */
    Object getObject(String name);

    /**
     * get list of all Object values for named option.
     *
     * @param name the name
     * @return List&lt;Object&gt;
     */
    List<Object> getObjectList(String name);

    /**
     * get value of named option as a Number.
     *
     * @param name the name
     * @return int
     * @throws IllegalArgumentException
     *             if argument is not a Number.
     */
    int getNumber(String name);

    /**
     * get remaining non-options args as Strings.
     *
     * @return List&lt;String&gt;
     * @throws IllegalArgumentException
     *             if args are not Strings.
     */
    List<String> args();

    /**
     * get remaining non-options args as Objects.
     *
     * @return List&lt;Object&gt;
     */
    List<Object> argObjects();

    /**
     * print usage message to System.err.
     */
    void usage();

    /**
     * print specified usage error to System.err. You should explicitly throw the returned
     * exception.
     *
     * @param error the error
     * @return IllegalArgumentException
     */
    IllegalArgumentException usageError(String error);
}
