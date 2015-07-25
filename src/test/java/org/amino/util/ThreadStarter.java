/*
 * Copyright (c) 2008 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.amino.util;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

public class ThreadStarter{
    static ThreadStarter runner = new ThreadStarter(null, 0, 0);

    public static ThreadStarter getRunner(Class<?> testClass, int nthread,
            int opCount) {
        return getRunner(testClass.getCanonicalName(), nthread, opCount);
    }

    /**
     * Get a runner to execute multi-thread tests.
     * 
     * @param className
     *            Name of the tested class. It will be printed to log file.
     * @param nthread
     *            How many threads will be used to execute test in parallel.
     * @param opCount
     *            How many operation will be executed by each thread.
     * @return
     */
    public static ThreadStarter getRunner(String className, int nthread,
            int opCount) {
        System.out.println(MessageFormat.format(
                "Requesting runner with className: {0}, "
                        + "thread number: {1}, operation count: {2}.",
                className, nthread, opCount));
        runner.className = className;
        runner.nthread = nthread;
        return runner;
    }

    private String className;

    private int nthread;

    private ThreadStarter(Class<?> setClass, int nthread, int opCount) {
        if (setClass != null)
            this.className = setClass.getCanonicalName();
        else
            className = "";
        this.nthread = nthread;
    }
}
