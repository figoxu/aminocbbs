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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class AbstractBaseTest {
	protected Class<?> classTested;
	protected Object[] params;
	protected String optionalLabel;

	/**
	 * A list of thread number that all amino tests respect
	 */
	protected static List<Integer> AMINO_NTHREAD;

	/**
	 * A suggestion to number of elements that data structures should contain
	 * during test. This will dramatically affect performance to list, array,
	 * vector component.
	 */
	protected static int AMINO_NELEMENT;

	/**
	 * This number is a suggestion to number of operation executed in each test.
	 * For timed test, which runs in a fix amount of time, this number is used
	 * to control inner loop count.
	 */
	protected static int AMINO_BLOCK_SIZE;

	/**
	 * type of runner
	 * 
	 * @author ganzhi
	 * 
	 */
	protected enum RunnerType {
		/**
		 * For each test case submitted to this runner, a fix operation count
		 * will be used to execute the test case repeatedly.
		 */
		FIX_OPCOUNT_RUNNER,
		/**
		 * For each test case submitted to this runner, a fix amount of time
		 * will be used to execute the test case repeatedly.
		 */
		FIX_TIME_RUNNER
	};

	/**
	 * type of runner for each test case submitted
	 */
	protected static RunnerType AMINOTESTMODE = RunnerType.FIX_TIME_RUNNER;

	/**
	 * This number is the suggested duration in second for timed test.
	 */
	protected static int AMINO_TIME;

	/**
	 * This number is the suggested duration in second for timed warm up.
	 */
	protected static int AMINO_WARMUP_TIME;

	/**
	 * type of state.
	 */
	protected enum StateType {
		/**
		 * The progress is executing test
		 */
		TESTING,
		/**
		 * The progress is executing warm up
		 */
		WARMUPING
	};

	/**
	 * type of task for executing test
	 */
	protected static StateType AMINOTESTSTATE = StateType.TESTING;
	/**
	 * This number is a suggestion to number of operation executed in each test.
	 * For fixed operation count test, this number is used to control inner loop
	 * count.
	 */
	protected static int AMINO_OPERATION_COUNT;

	/**
	 * Thread number for current executing test case
	 */
	protected int NTHREAD;

	/**
	 * Number of elements for current executing test case. Until now, this
	 * number always equals to AMINO_NELEMENT
	 */
	protected int NELEMENT;

	protected static String needLog;

	/**
	 * Properties
	 */
	protected static Properties prop;

	/**
	 * Name of the file where configuration parameter for testing is stored
	 */
	private static String confFile;

	/**
	 * content of the file where configuration parameter for testing is stored
	 */
	private static String confString;

	/**
	 * This flag is used by timed test. When time is running out, this flag
	 * should be set to notify testing threads end itself.
	 */
	static {
		prop = new Properties();

		confFile = System.getenv("AMINO_CONF_FILE");

		boolean usingDefault = false;
		if (confFile == null || "".equals(confFile)) {
			confFile = System.getProperty("user.dir")
					+ System.getProperties().getProperty("file.separator")
					+ "amino.conf";
			usingDefault = true;
		}
		try {
			prop.load(new FileInputStream(confFile));
		} catch (FileNotFoundException e2) {
		} catch (IOException e2) {
		}
		if (prop.size() == 0) {
			System.out
					.println("Didn't find a configure file! Please setup environment AMINO_CONF_FILE pointing to your configuration.\n");
		} else {
			if (usingDefault) {
				System.out
						.println("Environment variable is not set, using default configure file");
			}
			System.out.println();
			System.out.println("Load configure file " + confFile);
			System.out.println();
			prop.list(System.out);
			System.out.println("-- end of listing properties --\n");
		}

		needLog = prop.getProperty("AMINO_TEST_LOG", "false");

		try {
			AMINO_NELEMENT = Integer.valueOf(prop.getProperty("AMINO_ELEMENTS",
					"512"));
		} catch (NumberFormatException e) {

		} finally {
			if (AMINO_NELEMENT == 0) {
				AMINO_NELEMENT = 512;
			}
		}

		try {
			AMINO_NTHREAD = new ArrayList<Integer>();
			String thread_str = prop.getProperty("AMINO_THREADS",
					"8,1,2,3,4,5,6,7,8");
			if (thread_str != null) {
				String[] threads = thread_str.split(", *");
				for (int i = 0; i < threads.length; i++) {
					AMINO_NTHREAD.add(Integer.valueOf(threads[i]));
				}
			}
		} catch (NumberFormatException e) {

		} finally {
			if (AMINO_NTHREAD == null || AMINO_NTHREAD.size() == 0) {
				AMINO_NTHREAD = new ArrayList<Integer>();
				AMINO_NTHREAD.add(4);
			}
		}

		try {
			AMINO_OPERATION_COUNT = Integer.valueOf(prop.getProperty(
					"AMINO_OPERATION_COUNT", "2048"));
		} catch (NumberFormatException e) {

		} finally {
			if (AMINO_OPERATION_COUNT < 2048) {
				System.out
						.println("AMINO_OPERATION_COUNT is too small,please reset larger than the 2048 next time");
				AMINO_OPERATION_COUNT = 2048;
			}
		}

		try {
			AMINOTESTMODE = Enum.valueOf(RunnerType.class, String.valueOf(prop
					.getProperty("AMINOTESTMODE", "FIX_TIME_RUNNER")));
		} catch (Exception e1) {
			AMINOTESTMODE = RunnerType.FIX_TIME_RUNNER;
		}

		try {
			switch (AMINOTESTMODE) {
			case FIX_OPCOUNT_RUNNER:
				AMINO_BLOCK_SIZE = AMINO_OPERATION_COUNT;
				break;
			default:
				AMINO_BLOCK_SIZE = Integer.valueOf(prop.getProperty(
						"AMINO_BLOCK_SIZE", "2048"));
			}
		} catch (NumberFormatException e) {

		} finally {
			if (AMINO_BLOCK_SIZE < 2048) {
				System.out
						.println("AMINO_BLOCK_SIZE is too small,please reset larger than the 2048 next time");
				AMINO_BLOCK_SIZE = 2048;
			}
		}

		try {
			AMINO_TIME = Integer.valueOf(prop.getProperty("AMINO_TIME", "5"));
		} catch (NumberFormatException e) {
		} finally {
			if (AMINO_TIME == 0) {
				AMINO_TIME = 5;
			}
		}

		try {
			AMINO_WARMUP_TIME = Integer.valueOf(prop.getProperty(
					"AMINO_WARMUP_TIME", String.valueOf(AMINO_TIME)));
		} catch (NumberFormatException e) {
		} finally {
			if (AMINO_WARMUP_TIME == 0) {
				AMINO_WARMUP_TIME = AMINO_TIME;
			}
		}

		confString = "AMINO_ELEMENTS = " + AMINO_NELEMENT + "\n";
		confString += "AMINO_THREADS = " + AMINO_NTHREAD + "\n";
		confString += "AMINO_BLOCK_SIZE = " + AMINO_BLOCK_SIZE + "\n";
		confString += "AMINO_WARMUP_TIME = " + AMINO_WARMUP_TIME + "\n";
		confString += "AMINO_TIME = " + AMINO_TIME + "\n";
		confString += "AMINOTESTMODE = " + AMINOTESTMODE + "\n";
		
		System.out.println(confString);

	}

	public AbstractBaseTest(Object classTested, Object[] params, int nthread,
			int nelement) {
		this.classTested = (Class<?>) classTested;
		this.params = params;
		NTHREAD = nthread;
		NELEMENT = nelement;
	}

	public AbstractBaseTest(Object classTested, Object[] params, int nthread,
			int nelement, String optionalLabel) {
		this(classTested, params, nthread, nelement);
		this.optionalLabel = optionalLabel;
	}

	/**
	 * This method will generate workload which increases as thread number
	 * increases. Workload per thread remains a constant.
	 * 
	 * @param tClass
	 *            type of testing objects. It will be used by getInstance()
	 *            method.
	 * @param params
	 *            Parameter used to initialize testing objects.
	 * @return A list of parameters to initialize test case. Please refer to
	 *         Parameterized class of JUnit
	 */
	protected static Collection<Object[]> genWorkLoadFixedLoad(Class<?> tClass,
			Object[] params) {
		List<Object[]> args = new ArrayList<Object[]>();

		Collection<Integer> nums = getThreadNums();
		int elementNum = getElementNum();

		for (int threadNum : nums) {
			Object[] item = new Object[] { tClass, params, threadNum,
					elementNum };
			args.add(item);
		}

		return args;
	}

	/**
	 * This method will generate workload which remains constant for any number
	 * of threads. If thread number increase, the workload per thread will
	 * decrease.
	 * 
	 * @param tClass
	 *            type of testing objects. It will be used by getInstance()
	 *            method.
	 * @param params
	 *            Parameter used to initialize testing objects.
	 * @return A list of parameters to initialize test case. Please refer to
	 *         Parameterized class of JUnit
	 */
	protected static Collection<Object[]> genWorkLoadFixedIncome(
			Class<?> tClass, Object[] params) {
		List<Object[]> args = new ArrayList<Object[]>();

		Collection<Integer> nums = getThreadNums();
		int elementNum = getElementNum();

		for (int threadNum : nums) {
			Object[] item = new Object[] { tClass, params, threadNum,
					elementNum / threadNum };
			args.add(item);
		}

		return args;
	}

	protected static Collection<Object[]> genArguments(Class<?> tClass,
			Object[] params, String label) {
		List<Object[]> args = new ArrayList<Object[]>();

		Collection<Integer> nums = getThreadNums();
		int elementNum = AMINO_NELEMENT;

		for (int threadNum : nums) {
			Object[] item = new Object[] { tClass, params, threadNum,
					elementNum, label };
			args.add(item);
		}

		return args;
	}

	protected Object getInstance() throws InstantiationException,
			IllegalAccessException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException {
		if (params.length == 0) {
			Object instance = classTested.newInstance();
			return instance;
		} else if (params.length == 1) {
			Class<?> type = params[0].getClass();
			if (type.equals(Integer.class))
				type = int.class;
			if (type.equals(Short.class))
				type = short.class;
			// System.out.println("GetInstance:" + classTested);
			Constructor<?> constructor = classTested.getConstructor(type);
			return constructor.newInstance(params);
		} else { // FIXME: need to differ Integer and int
			Class<?>[] types = new Class[params.length];
			for (int i = 0; i < types.length; i++) {
				types[i] = params[i].getClass();
				if (types[i].equals(Integer.class)) {
					types[i] = int.class;
				}
				if (types[i].equals(Boolean.class)) {
					types[i] = boolean.class;
				}
				if (types[i].equals(Short.class)) {
					types[i] = short.class;
				} else if (types[i].equals(Double.class)) {
					types[i] = Double.TYPE;
				} else if (types[i].equals(Float.class)) {
					types[i] = Float.TYPE;
				}
			}
			System.out.println(classTested.getClass());
			Constructor<?> constructor = classTested.getConstructor(types);
			return constructor.newInstance(params);
		}
	}

	public static List<Integer> getThreadNums() {
		return AMINO_NTHREAD;
	}

	public static int getElementNum() {
		return AMINO_NELEMENT;
	}

	public static int getBlockSize() {
		return AMINO_BLOCK_SIZE;
	}

	public static int getOperationTime() {
		switch (AMINOTESTSTATE) {
		case WARMUPING:
			return AMINO_WARMUP_TIME;
		case TESTING:
			return AMINO_TIME;
		default:
			return AMINO_TIME;
		}
	}

	public static String needLogFile() {
		if (needLog != null && !needLog.equalsIgnoreCase("false"))
			return needLog;
		else
			return null;
	}
	
	public static String getConfString(){
		return confString;
	}
}
