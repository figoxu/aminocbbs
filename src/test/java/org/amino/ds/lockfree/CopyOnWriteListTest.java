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

package org.amino.ds.lockfree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.amino.util.AbstractBaseTest;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This case will test basic function of List class. such as:
 * <ol>
 * <li>add(Object);</li>
 * <li>remove(Object);</li>
 * <li>contains(Object);</li>
 * </ol>
 *
 * @author Xiao Jun Dai
 *
 */
@RunWith(Parameterized.class)
public class CopyOnWriteListTest extends AbstractBaseTest {

	List<Integer> listInt;

	List<String> listStr;

	private ThreadRunner runner;

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection paras() {
		List<Object[]> args = new ArrayList<Object[]>();

		args.addAll(genWorkLoadFixedLoad(LockFreeList.class, new Object[] {}));

		return args;
	}

	@SuppressWarnings("unchecked")
	public CopyOnWriteListTest(Class classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}

	@SuppressWarnings("unchecked")
	@Before
	public void init() throws SecurityException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		listInt = (List<Integer>) getInstance();
		listStr = (List<String>) getInstance();
		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}

	@Test
	public void testAddAndRemoveSameElement() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++)
						assertTrue(listInt.add(i));
				}
			};
		}
		runner.runThreads(threads, "testAddSameElement");

		assertEquals(NTHREAD * NELEMENT, listInt.size());

		threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++)
						assertTrue(listInt.remove(new Integer(i)));
				}
			};
		}
		runner.runThreads(threads, "testRemoveSameElement");

		assertEquals(0, listInt.size());
	}

	@Test
	public void testIsEmpty() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					assertTrue(listInt.isEmpty());
				}
			};
		}

		runner.runThreads(threads, "testIsEmpty");

		listInt.add(1);

		threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					assertFalse(listInt.isEmpty());
				}
			};
		}

		runner.runThreads(threads, "testIsNotEmpty");
	}

	@Test
	public void testSize() throws Throwable {
		for (int threadID = 0; threadID < NTHREAD; threadID++)
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertTrue(listStr.add(key));
			}

		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					assertEquals(NTHREAD * NELEMENT, listStr.size());
				}
			};
		}

		runner.runThreads(threads, "testSize");
	}

	@Test
	public void testAdd() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final String key = "test Thread-" + threadID;
			threads[threadID] = new Thread(key) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue(listStr.add(key + "_" + j));
					}
				}
			};
		}

		runner.runThreads(threads, "testAdd");

		for (int threadID = 0; threadID < NTHREAD; threadID++)
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertTrue(listStr.contains(key));
			}

		assertEquals(NTHREAD * NELEMENT, listStr.size());
	}

	//@Test
	public void testIndexOf() throws Throwable {
		for (int j = 0; j < NELEMENT; j++) {
			listStr.add("" + j);
		}

		for (int threadID = 0; threadID < NTHREAD; threadID++)
			for (int j = 0; j < NELEMENT; j++) {
				String key = "" + j;
				assertEquals(j, listStr.indexOf(key));
			}
	}

	// @Test
	public void testAddWithIndex() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final String key = "test Thread-" + threadID;
			threads[threadID] = new Thread(key) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						listStr.add(5, key + "_" + j);
					}
				}
			};
		}

		runner.runThreads(threads, "testAddWithIndex");

		System.out.println(listStr);
		for (int threadID = 0; threadID < NTHREAD; threadID++)
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertTrue(listStr.contains(key));
			}

		assertEquals(NTHREAD * NELEMENT, listStr.size());
	}

	@Test
	public void testRemove() throws Throwable {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final String key = "test Thread-" + threadID;
			for (int j = 0; j < NELEMENT; j++) {
				assertTrue(listStr.add(key + "_" + j));
			}
		}

		Thread[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final String key = "test Thread-" + threadID;
			threads[threadID] = new Thread(key) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue(listStr.remove(key + "_" + j));
					}
				}
			};
		}

		runner.runThreads(threads, "testRemove");

		for (int threadID = 0; threadID < NTHREAD; threadID++)
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				if (listStr.contains(key)) {
					System.out.println("key = " + key);
				}
				assertFalse(listStr.contains(key));
			}

		assertEquals(0, listStr.size());
	}

	@Test
	public void testContains() throws Throwable {

		Thread[] threads = new Thread[NTHREAD];

		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertFalse(listInt.contains(new Integer(j)));
					}
				}
			};
		}

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(listInt.add(new Integer(i)));
		}

		threads = new Thread[NTHREAD];

		for (int i = NTHREAD - 1; i >= 0; i--) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue(listInt.contains(new Integer(j)));
					}
				}
			};
		}

		runner.runThreads(threads, "testContains");
	}

	@Test
	public void concurrentAddRemoves() throws Throwable {
		// System.out.println("NTHREAD = " + NTHREAD + ", " + "NELEMENT = " +
		// NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0)
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue(listStr.add(key));
				}
		}

		Thread[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int index = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					if (index % 2 == 0)
						for (int i = 0; i < NELEMENT; i++) {
							assertTrue(listStr.remove("test "
									+ (i + index * NELEMENT)));
						}
					else
						for (int i = 0; i < NELEMENT; i++) {
							assertTrue(listStr.add("test "
									+ (i + index * NELEMENT)));
						}
				}
			};
		}

		runner.runThreads(threads, "concurrentDelIns");

		assertEquals(((NTHREAD % 2 == 0) ? NTHREAD : (NTHREAD - 1)) * NELEMENT
				/ 2, listStr.size());

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0)
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertFalse("Key: " + key + " ThID: " + threadID + " i: "
							+ i + " should be removed", listStr.contains(key));
				}
			else
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue("Key: " + key + " ThID: " + threadID + " i: "
							+ i + " should not be removed", listStr
							.contains(key));
				}
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < 2; i++)
			JUnitCore.runClasses(CopyOnWriteListTest.class);
	}
}
