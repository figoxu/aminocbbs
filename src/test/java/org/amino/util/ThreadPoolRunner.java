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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @deprecated This runner is not used any more
 */
public class ThreadPoolRunner extends AbstractRunner {
    private StoreUncaughtExceptionHandler excpHandler = new StoreUncaughtExceptionHandler();

    private ExecutorService pool;

    private Class<?> testClass;

    private int tn;

    private int nele;

    public ThreadPoolRunner(int threadNum, int nele, Class testClass) {
        tn = threadNum;
        pool = Executors.newFixedThreadPool(tn);
        this.testClass = testClass;
        this.nele = nele;
    }

    @SuppressWarnings("unchecked")
    public void runThreads(Runnable[] threads, String testName)
            throws Throwable {
    	System.gc();
    	resetOpCount();
        Future<Runnable>[] futures = new Future[threads.length];
        long start = System.nanoTime();

        for (int i = 0; i < threads.length; ++i) {
            FutureTask<Runnable> f = new FutureTask<Runnable>(threads[i], null);
            futures[i] = f;
            pool.execute(f);
        }

        for (Future<Runnable> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                excpHandler.exceptions.add(e);
            }
        }

        long end = System.nanoTime();

        Loggers.performance(testClass, tn, nele, testName, (end - start));

        for (int i = 0, len = excpHandler.getExceptions().size(); i < len; ++i) {
            throw excpHandler.getExceptions().get(i);
        }
    }
}
