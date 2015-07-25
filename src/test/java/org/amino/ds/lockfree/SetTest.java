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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.amino.util.AbstractBaseTest;
import org.amino.util.ConcurrentRunner;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This case will test basic function of Set class. such as:
 * <ol>
 * <li>add(Object);</li>
 * <li>remove(Object);</li>
 * <li>contains(Object);</li>
 * </ol>
 * 
 * @author Xiao Jun Dai
 * @author Zhi Gan
 * 
 */

@RunWith(Parameterized.class)
public class SetTest extends AbstractBaseTest {

	private Set<String> setStr;
	private Set<Integer> setInt;

	private ConcurrentRunner runner;

	protected Object getInstance() throws InstantiationException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		if (params.length == 0 && classTested.equals(HashSet.class)) {
			return Collections.synchronizedSet((Set<?>) classTested
					.newInstance());
		} else if (params.length == 1) {
			Constructor<?> constructor = classTested.getConstructor(int.class);
			// System.out.println(" class " + setClass + " constructor " +
			// constructor);
			return constructor.newInstance(((Integer) params[0]).intValue());
		} else {
			return super.getInstance();
		}
	}

	@Parameters
	public static Collection<Object[]> sets() {
		List<Object[]> args = new ArrayList<Object[]>();

		// args.addAll(genArguments(LazySet.class, new Object[]
		// {getElementNum()}));
		args.addAll(genWorkLoadFixedLoad(LockFreeSet.class,
				new Object[] { getElementNum() }));
		// args.addAll(genArguments(OpenHashSet.class,
		// new Object[] {}));
		// args.addAll(genArguments(LittleLockHashSet.class,
		// new Object[] { getElementNum() * 50 }));
		return args;
	}

	public SetTest(Object setclass, Object[] params, int nthread, int nelement) {
		super(setclass, params, nthread, nelement);
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		setInt = (Set<Integer>) getInstance();
		setStr = (Set<String>) getInstance();

		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}

	// @Test(expected = NullPointerException.class)
	public void testAddNullElement() throws Throwable {
		setInt.add(null);
	}

	// @Test(expected = NullPointerException.class)
	public void testRemoveNullElement() throws Throwable {
		setInt.remove(null);
	}

	// @Test(expected = NullPointerException.class)
	public void testContainsNullElement() throws Throwable {
		setInt.contains(null);
	}

	@Test
	public void testIsEmpty() throws Throwable {
		Runnable[] tasks = new Runnable[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			tasks[threadID] = new Runnable() {
				public void run() {
					assertTrue(setInt.isEmpty());
				}
			};
		}

		runner.runThreads(tasks, "testIsEmpty");

		setInt.add(1);

		tasks = new Runnable[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			tasks[threadID] = new Runnable() {
				public void run() {
					assertFalse(setInt.isEmpty());
				}
			};
		}

		runner.runThreads(tasks, "testIsNotEmpty");
	}

	@Test
	public void testSize() throws Throwable {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;

				boolean res = setStr.add(key);
				assertTrue(res);
			}
		}

		assertEquals(setStr.size(), NTHREAD*NELEMENT);

		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					assertEquals(setStr.size(), NTHREAD * NELEMENT);
				}
			};
		}

		runner.runThreads(threads, "testSize");
	}

	@Test
	public void testContainsOld() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		final Integer[] intArray = new Integer[NELEMENT];
		for (int i = 0; i < NELEMENT; i++) {
			Integer oldint = Integer.valueOf(i);
			intArray[i] = oldint;
			assertTrue(setInt.add(oldint));
		}

		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue("Missing element " + j, setInt
								.contains(intArray[j]));
					}
				}
			};
		}

		runner.runThreads(threads, "testContainsOld");
	}

	@Test
	public void testContainsNew() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertFalse(setInt.contains(Integer.valueOf(j)));
					}
				}
			};
		}

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(setInt.add(new Integer(i)));
		}

		threads = new Thread[NTHREAD];

		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue("Missing Element " + j + " at bucket ",
								setInt.contains(new Integer(j)));
					}
				}
			};
		}

		runner.runThreads(threads, "testContainsNew");
	}

	@Test
	public void testContainsNon() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];
		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertFalse(setInt.contains(new Integer(j)));
					}
				}
			};
		}

		runner.runThreads(threads, "testContainsEmpty");

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(setInt.add(new Integer(i)));
		}

		// Testing with elements doesn't lie in this set
		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertFalse(setInt.contains(new Integer(2 * NELEMENT
								+ j)));
					}
				}
			};
		}

		runner.runThreads(threads, "testContainsMissing");
	}

	@Test
	public void testAddAndRemoveSameElement() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						assertFalse(setInt.remove(new Integer(i)));
					}
				}
			};
		}
		runner.runThreads(threads, "testRemoveElement");

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(setInt.add(i));
		}

		threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						assertFalse(i + " already existed ", setInt.add(i));
					}
				}
			};
		}
		runner.runThreads(threads, "testAddSameElement");

		assertEquals(NELEMENT, setInt.size());

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue("fail to remove " + i, setInt.remove(i));
			assertFalse("" + i, setInt.contains(i));
		}

		threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						assertFalse(setInt.contains(new Integer(i)));
						assertFalse(setInt.remove(new Integer(i)));
					}
				}
			};
		}
		runner.runThreads(threads, "testRemoveSameElement");

		assertEquals(0, setInt.size());
	}

	@Test
	public void concurrentAddRemoves() throws Throwable {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0) {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue(setStr.add(key));
					assertTrue(setStr.contains(key));
				}
			}
		}
		assertEquals(NELEMENT * (NTHREAD - NTHREAD / 2), setStr.size());

		if (setStr instanceof LockFreeSet) {
			LockFreeSet lfSet = (LockFreeSet) setStr;
			if (lfSet.dummyRate() > 2) {
				fail("We should have more dummy nodes"
						+ lfSet.bucketSizeLog2.get() + " " + lfSet.size());
			}
		}

		Runnable[] threads = new Runnable[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int index = threadID;
			threads[threadID] = new Runnable() {
				public void run() {
					if (index % 2 == 0) {
						for (int i = 0; i < NELEMENT; i++) {
							String element = "test " + (i + index * NELEMENT);
							// assertTrue(setStr.contains(element));
							boolean removeRes = setStr.remove(element);
							if (!removeRes) {
								// removeRes = setStr.contains(element);
								fail("Removing '" + element + "' i: " + i
										+ " threadID:" + index);
							} else {
								assertFalse(setStr.contains(element));
							}
						}
					} else {
						for (int i = 0; i < NELEMENT; i++) {
							String element = "test " + (i + index * NELEMENT);
							assertTrue("Adding '" + element + "' i: " + i
									+ " index:" + index, setStr.add(element));
						}
					}
				}
			};
		}

		runner.runThreads(threads, "concurrentAddRemoves");

		assertEquals(NELEMENT * (NTHREAD / 2), setStr.size());

		if (setStr instanceof LockFreeSet) {
			LockFreeSet lfSet = (LockFreeSet) setStr;
			if (lfSet.dummyRate() > 2) {
				fail("We should have more dummy nodes");
			}
		}

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0) {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					boolean temp = setStr.contains(key);
					assertFalse("Key: " + key + " ThID: " + threadID + " i: "
							+ i, temp);
				}
			} else {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue("Key: " + key + " ThID: " + threadID + " i: "
							+ i, setStr.contains(key));
				}
			}
		}
	}

	@Test
	public void testAddAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
			}
		}

		assertTrue(setStr.addAll(rndStrings));

		assertEquals(setStr.size(), rndStrings.size());

	}

	@Test
	public void testClear() {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				setStr.add("" + (i + threadID * NELEMENT));
			}
		}

		setStr.clear();

		assertEquals(0, setStr.size());
	}

	@Test(timeout = 100000)
	public void testRemoveAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				setStr.add("" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(setStr.removeAll(rndStrings));

		assertEquals(setStr.size(), 0);
	}

	@Test(timeout = 100000)
	public void testRetainAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				setStr.add("" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(!setStr.retainAll(rndStrings));

		for (Iterator<String> i = setStr.iterator(); i.hasNext();) {
			String s = i.next();
			assertTrue(rndStrings.contains(s));
		}
	}

	@Test(timeout = 100000)
	public void testContainsAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				setStr.add("" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(setStr.containsAll(rndStrings));
	}
	
	@Test(timeout = 100000)
	public void testToArray() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				setStr.add("" + (i + threadID * NELEMENT));
			}
		}
		Object[] setArray = setStr.toArray();
		for (Object o: setArray) {
			String s = (String)o;
			assertTrue(rndStrings.contains(s));
		}
	}
}
