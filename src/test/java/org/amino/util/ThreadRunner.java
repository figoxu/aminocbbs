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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadRunner extends AbstractRunner {
	static ThreadRunner runner = new ThreadRunner(null, 0, 0);
	static Map<Integer, ExecutorService> executors = new ConcurrentHashMap<Integer, ExecutorService>();

	/**
	 * Get a runner to execute multi-thread tests.
	 * 
	 * @param testClass
	 *            class of test
	 * @param nthread
	 *            How many threads will be used to execute test in parallel.
	 * @param opCount
	 *            How many operation will be executed by each thread.
	 * @return thread runner
	 */
	public static ThreadRunner getRunner(Class<?> testClass, int nthread,
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
	 * @return thread runner
	 */
	public static ThreadRunner getRunner(String className, int nthread,
			int opCount) {
		System.out.println(MessageFormat.format(
				"Requesting fix operation count runner with className: {0}, "
						+ "thread number: {1}, operation count: {2}.",
				className, nthread, opCount));
		runner.className = className;
		if (runner.executor == null || runner.nthread != nthread) {
			runner.executor = executors.get(nthread);
			if (runner.executor == null) {
				runner.executor = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nthread);
				executors.put(nthread, runner.executor);
			}
		}

		runner.nthread = nthread;
		return runner;
	}

	ExecutorService executor;

	public static void shutdown() {
		Set<Integer> keySet = executors.keySet();
		for (Integer key : keySet) {
			executors.get(key).shutdown();
		}
		executors.clear();
	}

	StoreUncaughtExceptionHandler excpHandler = new StoreUncaughtExceptionHandler();

	private String className;

	private int nthread;

	/**
	 * @param setClass
	 * @param nthread
	 * @param opCount
	 */
	private ThreadRunner(Class<?> setClass, int nthread, int opCount) {
		if (setClass != null)
			this.className = setClass.getCanonicalName();
		else
			className = "";
		this.nthread = nthread;
	}

	/* (non-Javadoc)
	 * @see org.amino.util.ConcurrentRunner#runThreads(java.lang.Runnable[], java.lang.String)
	 */
	public void runThreads(final Runnable[] tasks, String testName)
			throws InterruptedException, ExecutionException {
		System.gc();
		resetOpCount();
		long start = System.nanoTime();

		Future[] futures = new Future[tasks.length];

		for (int i = 0; i < tasks.length; i++) {
			futures[i] = executor.submit(tasks[i]);
		}

		for (int i = 0; i < tasks.length; i++) {
			futures[i].get();
			addToOpCount(AbstractBaseTest.getBlockSize());
		}

		long end = System.nanoTime();

		Loggers.performance(className, nthread, getOpCount(), testName,
				(end - start));
	}

	/* (non-Javadoc)
	 * @see org.amino.util.AbstractRunner#runThreads(java.util.concurrent.Callable[], java.lang.String)
	 */
	public void runThreads(final Callable[] tasks, String testName)
			throws InterruptedException, ExecutionException {
		System.gc();
		resetOpCount();
		long start = System.nanoTime();

		Future[] futures = new Future[tasks.length];

		for (int i = 0; i < tasks.length; i++) {
			futures[i] = executor.submit(tasks[i]);
		}

		for (int i = 0; i < tasks.length; i++) {
			Integer opCount = (Integer) futures[i].get();
			addToOpCount(opCount);
		}

		long end = System.nanoTime();

		Loggers.performance(className, nthread, getOpCount(), testName,
				(end - start));
	}
}
