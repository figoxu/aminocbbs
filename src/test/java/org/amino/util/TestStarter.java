package org.amino.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.amino.util.AbstractBaseTest.StateType;
import org.junit.runner.JUnitCore;

/**
 * perform the test specified times
 * 
 * @author Zhi Gan (ganzhi@gmail.com)
 * 
 */
public class TestStarter {
	/**
	 * initialization
	 * 
	 * @param className
	 *            class of test
	 */
	public TestStarter(Class className) {
		this(className, 1);
	}

	/**
	 * initialization
	 * 
	 * @param className
	 *            class of test
	 * @param runTimes
	 *            times of test
	 */
	public TestStarter(Class className, int runTimes) {
		this.className = className;
		this.runTimes = runTimes;
	}

	/**
	 * start test by className,and perform specified times the first test is
	 * used to warm-up, and log begin at second time
	 * 
	 * @param className
	 * @param runTimes
	 */
	public static void startTest(Class className, int runTimes) {
		AbstractBaseTest.AMINOTESTSTATE = StateType.WARMUPING;
		Loggers.silent = true;
		JUnitCore.runClasses(className);
		AbstractBaseTest.AMINOTESTSTATE = StateType.TESTING;
		Loggers.silent = false;
		for (int i = 0; i < runTimes; i++)
			JUnitCore.runClasses(className);
		switch (AbstractBaseTest.AMINOTESTMODE) {
		case FIX_OPCOUNT_RUNNER:
			ThreadRunner.shutdown();
			break;
		default:
			ConstTimeRunner.shutdown();
		}
	}

	/**
	 * start test by className two times the first test is used to warm-up, and
	 * log begin at second time
	 * 
	 * @param className
	 */
	public static void startTest(Class className) {
		try {
			Loggers.performance("machine:" + InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Loggers.performance(AbstractBaseTest.getConfString());
		long startTime = System.nanoTime();
		startTest(className, 1);
		long testTime = System.nanoTime() - startTime;
		Loggers.performance("time:" + (int)(testTime/1000000000L) + "s");
	}

	/**
	 * class of test
	 */
	private Class className;

	/**
	 * times of test
	 */
	private int runTimes;

	public int getRunTimes() {
		return runTimes;
	}

	public void setRunTimes(int runTimes) {
		this.runTimes = runTimes;
	}

	/**
	 * 
	 * Get a runner to execute multi-thread tests according by AMINOTESTMODE
	 * 
	 * @param optionalLabel
	 * @param NTHREAD
	 * @param NELEMENT
	 * @return
	 */
	public static ConcurrentRunner getRunner(String optionalLabel, int NTHREAD,
			int NELEMENT) {
		switch (AbstractBaseTest.AMINOTESTMODE) {
		case FIX_OPCOUNT_RUNNER:
			return ThreadRunner.getRunner(optionalLabel, NTHREAD, NELEMENT);
		default:
			return ConstTimeRunner.getRunner(optionalLabel, NTHREAD);
		}
	}
}
