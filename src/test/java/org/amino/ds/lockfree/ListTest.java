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
import java.util.Iterator;
import java.util.List;

import org.amino.util.AbstractBaseTest;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test of LockFreeList.
 * 
 * Testcase with MT suffix is running in multi-threads, otherwise is running in
 * single-thread.
 * 
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
public class ListTest extends AbstractBaseTest {

	/**
	 * List if Integer.
	 */
	List<Integer> listInt;

	/**
	 * List if String.
	 */
	List<String> listStr;

	/**
	 * Run an array of threads and wait until all finish.
	 */
	private ThreadRunner runner;

	/**
	 * Parameters for parameterized unittest.
	 * 
	 * @return list of parameters
	 */
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection paras() {
		List<Object[]> args = new ArrayList<Object[]>();
		args.addAll(genWorkLoadFixedLoad(LockFreeList.class, new Object[] {}));
		args.addAll(genWorkLoadFixedLoad(LockFreeOrderedList.class,
				new Object[] {}));
		return args;
	}

	/**
	 * Constructor.
	 * 
	 * @param classTested
	 *            class under unit test
	 * @param params
	 *            parameters
	 * @param nthread
	 *            number of threads
	 * @param nelement
	 *            number of elements
	 */
	@SuppressWarnings("unchecked")
	public ListTest(Class classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}

	/**
	 * Setup method before every unittest runs.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		listInt = (List<Integer>) getInstance();
		listStr = (List<String>) getInstance();
		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}

	/**
	 * Every thread add the same int from [0, NELEMENT). Then the list has
	 * NELEMENT * NTHREAD elements.
	 * 
	 * Then every thread add the same int from [0, NELEMENT). Every remove
	 * should be successful.
	 * 
	 * Finally, the list is empty.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testAddAndRemoveSameElementMT() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		// every thread add the same int from [0, NELEMENT).
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						assertTrue(listInt.add(i));
					}
				}
			};
		}
		runner.runThreads(threads, "testAddSameElement");

		assertEquals(NTHREAD * NELEMENT, listInt.size());

		// every thread add the same int from [0, NELEMENT).
		threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						assertTrue(listInt.remove(new Integer(i)));
					}
				}
			};
		}
		runner.runThreads(threads, "testRemoveSameElement");

		assertEquals(0, listInt.size());
	}

	/**
	 * Test set() and get() in single thread.
	 */
	@Test
	public void testGetSet() {
		for (int i = 0; i < NELEMENT; i++) {
			listInt.add(NELEMENT - i);
		}
		
		for (int i = 0; i < NELEMENT; i++) {
			listInt.set(NELEMENT - i,i);
		}

		for (int i = 0; i < NELEMENT; i++) {
			int res = listInt.get(i);
			assertEquals(i, res);
		}
	}

	/**
	 * Test isEmpty() in multi-threads.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testIsEmptyMT() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		// every thread check if the list is empty.
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					assertTrue(listInt.isEmpty());
				}
			};
		}

		runner.runThreads(threads, "testIsEmpty");

		/* add one element */
		listInt.add(1);

		threads = new Thread[NTHREAD];
		// every thread check if the list is not empty.
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					/* isEmpty() should be return false after adding one element */
					assertFalse(listInt.isEmpty());
				}
			};
		}

		runner.runThreads(threads, "testIsNotEmpty");
	}

	/**
	 * Test size() in multi-threads.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testSizeMT() throws Throwable {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertTrue(listStr.add(key));
			}
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

	/**
	 * Test add() in multi-threads.
	 * 
	 * @throws Throwable
	 */
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

		/* check if the list contains the element added by multi-threads */
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertTrue(listStr.contains(key));
			}
		}

		assertEquals(NTHREAD * NELEMENT, listStr.size());
	}

	/**
	 * Test indexOf() in single-thread.
	 * 
	 * indexOf() is not thread-safe.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testIndexOf() throws Throwable {
		NELEMENT = 10;

		for (int j = 0; j < NELEMENT; j++) {
			listStr.add("" + j);
		}

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int j = 0; j < NELEMENT; j++) {
				String key = "" + j;
				assertEquals(j, listStr.indexOf(key));
			}
		}
	}

	/**
	 * Test remove() in multi-threads.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testRemoveMT() throws Throwable {
		/* set up initial data before removing from list */
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

		/* check if elements are removed from list. */
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int j = 0; j < NELEMENT; j++) {
				String key = "test Thread-" + threadID + "_" + j;
				assertFalse(listStr.contains(key));
			}
		}

		assertEquals(0, listStr.size());
	}

	/**
	 * Test contains() in multi-threads.
	 * 
	 * The list is empty at first and contains() return false. Then add some
	 * elements into it and contains() return true.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testContainsMT() throws Throwable {

		Thread[] threads = new Thread[NTHREAD];

		for (int i = 0; i < NTHREAD; i++) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertFalse(listInt.contains(Integer.valueOf(j)));
					}
				}
			};
		}

		// add some elements.
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(listInt.add(Integer.valueOf(i)));
		}

		threads = new Thread[NTHREAD];

		for (int i = NTHREAD - 1; i >= 0; i--) {
			threads[i] = new Thread("Thread-" + i) {
				public void run() {
					for (int j = 0; j < NELEMENT; j++) {
						assertTrue(listInt.contains(Integer.valueOf(j)));
					}
				}
			};
		}

		runner.runThreads(threads, "testContains");
	}

	/**
	 * Test add() and remove() in multi-threads.
	 * 
	 * initially, some elements is added into the list. Then threads with even
	 * id will remove initial elements from list. Threads with odd id will add
	 * elements into list. Finally, check if there exist initial elements and
	 * new added elements.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void concurrentAddRemovesMT() throws Throwable {
		// add some elements as initialization.
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0) {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue(listStr.add(key));
				}
			}
		}

		Thread[] threads = new Thread[NTHREAD];
		/*
		 * Threads with even id will remove initial ements from list. Threads
		 * with odd id will add eelements into list.
		 */
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int index = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					if (index % 2 == 0) {
						for (int i = 0; i < NELEMENT; i++) {
							assertTrue(listStr.remove("test "
									+ (i + index * NELEMENT)));
						}
					} else {
						for (int i = 0; i < NELEMENT; i++) {
							assertTrue(listStr.add("test "
									+ (i + index * NELEMENT)));
						}
					}
				}
			};
		}

		runner.runThreads(threads, "concurrentDelIns");

		assertEquals(((NTHREAD % 2 == 0) ? NTHREAD : (NTHREAD - 1)) * NELEMENT
				/ 2, listStr.size());

		/*
		 * Initial elements should be removed and new added elements should
		 * exist.
		 */
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			if (threadID % 2 == 0) {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertFalse("Key: " + key + " ThID: " + threadID + " i: "
							+ i + " should be removed", listStr.contains(key));
				}
			} else {
				for (int i = 0; i < NELEMENT; i++) {
					String key = "test " + (i + threadID * NELEMENT);
					assertTrue("Key: " + key + " ThID: " + threadID + " i: "
							+ i + " should exist", listStr.contains(key));
				}
			}
		}
	}

	@Test(expected = NullPointerException.class)
	public void testContainsNullElement() {
		listStr.contains(null);
	}

	@Test(expected = NullPointerException.class)
	public void testAddNullElement() {
		listStr.add(null);
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveNullElement() {
		listStr.remove(null);
	}

	/**
	 * Test clear functions
	 */
	@Test(timeout = 100000)
	public void testClear() {
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				assertTrue(listStr.add("testClear" + i));
			}
		}
		listStr.clear();
		assertEquals(0, listStr.size());
	}

	/**
	 * iterator iterates through all elements
	 */
	@Test
	public void testIterator() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				listStr.add("" + (i + threadID * NELEMENT));
			}
		}
		Iterator<String> it = listStr.listIterator();
		while (it.hasNext()) {
			assertTrue(rndStrings.contains(it.next()));
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

		assertTrue(listStr.addAll(rndStrings));

		assertEquals(listStr.size(), rndStrings.size());

		for (String s : rndStrings) {
			assertTrue(listStr.contains(s));
		}
	}

	@Test(timeout = 100000)
	public void testRetainAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				listStr.add("" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(!listStr.retainAll(rndStrings));

		for (Iterator<String> i = listStr.iterator(); i.hasNext();) {
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
				listStr.add("" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(listStr.containsAll(rndStrings));
	}

	@Test(timeout = 100000)
	public void testToArray() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				listStr.add("" + (i + threadID * NELEMENT));
			}
		}
		Object[] setArray = listStr.toArray();
		for (Object o : setArray) {
			String s = (String) o;
			assertTrue(rndStrings.contains(s));
		}
	}
	
	@Test(timeout = 100000)
	public void testRemoveAll() {
		final ArrayList<String> rndStrings = new ArrayList<String>(NTHREAD
				* NELEMENT);
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			for (int i = 0; i < NELEMENT; i++) {
				rndStrings.add("" + (i + threadID * NELEMENT));
				listStr.add(i,"" + (i + threadID * NELEMENT));
			}
		}
		assertTrue(!listStr.removeAll(rndStrings));

		assertEquals(0,listStr.size());
	}
}
