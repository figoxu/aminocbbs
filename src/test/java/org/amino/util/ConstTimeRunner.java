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

/**
 * For each test case submitted to this runner, a fix amount of time will be
 * used to execute the test case repeatedly.
 * 
 * @author ganzhi
 * 
 */
public class ConstTimeRunner extends AbstractRunner {
	/**
	 * signal to notify test to stop
	 */
	private volatile static boolean _finishFlag = false;
	static ConstTimeRunner runner = new ConstTimeRunner(null, 0, 0);
	static Map<Integer, ExecutorService> executors = new ConcurrentHashMap<Integer, ExecutorService>();

	/**
	 * Get a runner to execute multi-thread tests.
	 * 
	 * @param testClass
	 *            class of the testcases
	 * @param nthread
	 *            How many threads will be used to execute test in parallel.
	 * @param opCount
	 *            How many operation will be executed by each thread.
	 * @return thread runner
	 */
	public static ConstTimeRunner getRunner(Class<?> testClass, int nthread) {
		return getRunner(testClass.getCanonicalName(), nthread);
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
	public static ConstTimeRunner getRunner(String className, int nthread) {
		System.out.println(MessageFormat.format(
				"Requesting const-time runner with className: {0}, "
						+ "thread number: {1}, operation time {2} seconds.",
				className, nthread, AbstractBaseTest.getOperationTime()));
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

	/**
	 * shutdown executors
	 */
	public static void shutdown() {
		Set<Integer> keySet = executors.keySet();
		for (Integer key : keySet) {
			executors.get(key).shutdown();
		}
		executors.clear();
	}

	StoreUncaughtExceptionHandler excpHandler = new StoreUncaughtExceptionHandler();

	/**
	 * Name of the tested class
	 */
	private String className;

	/**
	 * How many threads will be used to execute test in parallel.
	 */
	private int nthread;

	/**
	 * initialization
	 * 
	 * @param setClass
	 *            class of test case
	 * @param nthread
	 *            How many threads will be used to execute test in parallel.
	 * @param opCount
	 *            How many operation will be executed by each thread.
	 */
	private ConstTimeRunner(Class<?> setClass, int nthread, int opCount) {
		if (setClass != null)
			this.className = setClass.getCanonicalName();
		else
			className = "";
		this.nthread = nthread;
	}

	/**
	 * execute the test case repeatedly in a fix amount of time
	 * 
	 * @author ganzhi
	 * 
	 */
	public class Wrapper implements Runnable {
		Runnable test;

		public Wrapper(Runnable test) {
			this.test = test;
		}

		public void run() {
			_finishFlag = false;
			while (!_finishFlag) {
				try {
					int opCountAdd = AbstractBaseTest.getBlockSize();
					test.run();
					addToOpCount(opCountAdd);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.amino.util.ConcurrentRunner#runThreads(java.lang.Runnable[],
	 *      java.lang.String)
	 */
	public void runThreads(final Runnable[] tasks, String testName)
			throws InterruptedException, ExecutionException {
		System.gc();
		resetOpCount();

		long start = System.nanoTime();
		Future[] futures = new Future[tasks.length];

		for (int i = 0; i < tasks.length; i++) {
			futures[i] = executor.submit(new Wrapper(tasks[i]));
		}

		try {
			int sleepTime = AbstractBaseTest.getOperationTime() * 1000;
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			_finishFlag = true;
		}

		for (int i = 0; i < tasks.length; i++) {
			futures[i].get();
		}

		long end = System.nanoTime();
		Loggers.performance(className, nthread, getOpCount(), testName,
				(end - start));
	}

	/**
	 * wrap Callable in Runnable ,set OpCount by return of Callable
	 * 
	 * @author ganzhi
	 * 
	 */
	public class WrapperForCallable implements Runnable {
		Callable test;

		public WrapperForCallable(Callable test) {
			this.test = test;
		}

		public void run() {
			_finishFlag = false;
			while (!_finishFlag) {
				try {
					Integer opCount = (Integer) test.call();
					addToOpCount(opCount);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.amino.util.AbstractRunner#runThreads(java.util.concurrent.Callable[],
	 *      java.lang.String)
	 */
	@Override
	public void runThreads(Callable[] tasks, String testName) throws Throwable {
		System.gc();
		resetOpCount();

		long start = System.nanoTime();
		Future[] futures = new Future[tasks.length];

		for (int i = 0; i < tasks.length; i++) {
			futures[i] = executor.submit(new WrapperForCallable(tasks[i]));
		}

		try {
			int sleepTime = AbstractBaseTest.getOperationTime() * 1000;
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			_finishFlag = true;
		}

		for (int i = 0; i < tasks.length; i++) {
			futures[i].get();
		}

		long end = System.nanoTime();
		Loggers.performance(className, nthread, getOpCount(), testName,
				(end - start));
	}
}
