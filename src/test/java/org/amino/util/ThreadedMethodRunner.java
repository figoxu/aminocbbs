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

/**
 *
 */
package org.amino.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.BeforeAndAfterRunner;
import org.junit.internal.runners.TestIntrospector;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * @author Zhi Gan
 * 
 */
public class ThreadedMethodRunner extends BeforeAndAfterRunner {
    private final Object fTest;

    private final Method fMethod;
    private Method fInitFor;
    private final Method fCheckFor;

    private final RunNotifier fNotifier;
    private final TestIntrospector fTestIntrospector;
    private final Description fDescription;
    private final int fThreadNum;
    private List<Throwable> excpList;

    public ThreadedMethodRunner(Object test, Method method, Method checkFor,
            Method initFor, RunNotifier notifier, Description description,
            int threadNum) {
        super(test.getClass(), Before.class, After.class, test);
        fTest = test;
        fMethod = method;
        fCheckFor = checkFor;
        fNotifier = notifier;
        fTestIntrospector = new TestIntrospector(test.getClass());
        fDescription = description;
        fThreadNum = threadNum;
        fInitFor = initFor;
        excpList = new CopyOnWriteArrayList<Throwable>();
    }

    public void run() {
        if (fTestIntrospector.isIgnored(fMethod)) {
            fNotifier.fireTestIgnored(fDescription);
            return;
        }
        fNotifier.fireTestStarted(fDescription);
        try {
            runMethod();
        } finally {
            fNotifier.fireTestFinished(fDescription);
        }
    }

    private void runMethod() {
        runProtected();
    }

    @Override
    protected void runUnprotected() {
        try {
            executeInitFor();
            executeThreadedMethod();
            executeCheckFor();
        } catch (InvocationTargetException e) {
            Throwable actual = e.getTargetException();
            addFailure(actual);
        } catch (Throwable e) {
            addFailure(e);
        }
    }

    /**
     * @throws Throwable
     * 
     */
    private void executeCheckFor() throws Throwable {
        if (excpList.size() > 0) {
            System.out.println("Exception List = " + excpList);
            throw excpList.get(0);
        }
        if (fCheckFor != null)
            fCheckFor.invoke(fTest, getThreadNumber());
    }

    /**
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * 
     */
    private void executeInitFor() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        if (fInitFor != null)
            fInitFor.invoke(fTest, getThreadNumber());
    }

    /**
     * @return
     */
    private int getThreadNumber() {
        return fThreadNum;
    }

    protected void executeThreadedMethod() throws Throwable {
        final int size = getThreadNumber();
        ExecutorService service = Executors.newFixedThreadPool(size);

        List<Future<?>> results = new ArrayList<Future<?>>();
        for (int i = 0; i < size; i++) {
            final int index = i;
            Callable<Object> callable = new Callable<Object>() {
                public Object call() throws Exception {
                    fMethod.invoke(fTest, index, size);
                    return null;
                }
            };
            results.add(service.submit(callable));
        }
        try {
            for (Future<?> f : results) {
                f.get();
            }
        } catch (ExecutionException e) {
            throw new Throwable(e.getCause());
        }
        // wait until all tasks are finished.
        service.shutdown();
        if (!service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
            service.shutdownNow();
            throw new InterruptedException();
        }
    }

    @Override
    protected void addFailure(Throwable e) {
        fNotifier.fireTestFailure(new Failure(fDescription, e));
    }
}
