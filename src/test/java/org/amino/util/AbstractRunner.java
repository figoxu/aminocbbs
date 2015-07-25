package org.amino.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author ganzhi
 *
 */
public abstract class AbstractRunner implements ConcurrentRunner {

	/**
	 * This variable specifies how many operation will be executed per thread
	 */
	protected AtomicLong operationCount = new AtomicLong(0);;

	/**
	 * add newOpCount into operationCount
	 * @param newOpCount
	 */
	protected void addToOpCount(int newOpCount) {
		operationCount.addAndGet(newOpCount);
	}

	/* (non-Javadoc)
	 * @see org.amino.util.ConcurrentRunner#getOpCount()
	 */
	@Override
	public long getOpCount() {
		return operationCount.get();
	}

	/* (non-Javadoc)
	 * @see org.amino.util.ConcurrentRunner#resetOpCount()
	 */
	public void resetOpCount() {
		operationCount.set(0L);
	}

	/* (non-Javadoc)
	 * @see org.amino.util.ConcurrentRunner#runThreads(java.util.concurrent.Callable[], java.lang.String)
	 */
	public void runThreads(final Callable[] threads, String testName)
			throws Throwable {
		int arrLen = threads.length;
		Runnable[] tasks = new Runnable[arrLen];
		for (int i = 0; i < arrLen; i++) {
			final int index = i;
			tasks[i] = new Runnable() {
				@Override
				public void run() {
					try {
						threads[index].call();
					} catch (Exception e) {
						new RuntimeException(e);
					}
				}
			};
		}
		runThreads(tasks, testName);
	}
}
