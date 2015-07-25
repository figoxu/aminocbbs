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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.amino.util.AbstractBaseTest;
import org.amino.util.RandomArrayGenerator;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unittest of Queue.
 * 
 * @author Xiao Jun Dai
 * 
 */
@RunWith(Parameterized.class)
public class QueueTest extends AbstractBaseTest {
	@Parameters
	public static Collection paras() {
		List<Object[]> args = new ArrayList<Object[]>();

		args.addAll(genWorkLoadFixedLoad(LockFreePriorityQueue.class, new Object[] {}));
//		args.addAll(genWorkLoadFixedLoad(LockFreeQueue.class, new Object[] {}));
//		args.addAll(genWorkLoadFixedLoad(LockFreeBlockQueue.class, new Object[] {NELEMENT}));

		return args;
	}

	public QueueTest(Class classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}
	
	@Before
	public void init() throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		queue = (Queue<String>) getInstance();
		Arrays.sort(sortedTestData);
		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}
	
	private static final int NELEMENT = 512;
	Queue<String> queue;
	String[] testData = RandomArrayGenerator.getRandStringArray(NELEMENT);
    String[] sortedTestData = Arrays.copyOf(testData, testData.length);
	List<String> polledData;
	private ThreadRunner runner;
	
	@Test
    public void testPeekST() {
        for (int i = 0; i < testData.length; i++) {
            queue.offer(sortedTestData[i]);
        }

        if(queue instanceof LockFreePriorityQueue)
        	assertEquals(sortedTestData[0], ((LockFreePriorityQueue)queue).peekFirst());
        
        for (int i = 0; i < NELEMENT; i++) {
            assertEquals(sortedTestData[i], queue.peek());
            assertNotNull(queue.poll());
        }
    }
	
	@Test
    public void testContainsST() {
        for (int i = 0; i < testData.length; i++) {
            queue.offer(testData[i]);
        }
        
        for (int i = 0; i < NELEMENT; i++) {
            assertTrue(queue.contains(sortedTestData[i]));
        }
        
        assertTrue(!queue.contains("faint data"));
    }
	
	@Test
	public void testPollST() {
		for (int i = 0; i < sortedTestData.length; i++) {
			queue.offer(sortedTestData[i]);
		}

		for (int i = 0; i < sortedTestData.length; i++) {
			assertEquals(sortedTestData[i], queue.peek());
			assertEquals(sortedTestData[i], queue.poll());
		}

		assertTrue(queue.isEmpty());
		assertNull(queue.peek());
		
	}

	@Test
	public void testOfferST() {
		for (int i = 0; i < testData.length; i++) {
			queue.offer(sortedTestData[i]);
		}

		int count = 0;
		for (int i = 0; i < testData.length; i++) {
			assertEquals(sortedTestData[count++], queue.poll());
		}
	}

	@Test
	public void testSizeST() {
		for (int i = 0; i < testData.length; i++) {
			queue.offer(testData[i]);
		}
		assertEquals(testData.length, queue.size());

		for (int i = 0; i < testData.length; i++) {
			assertNotNull(queue.poll());
		}
		assertEquals(0, queue.size());
	}

	@Test
	public void testOfferMT() throws InterruptedException, ExecutionException {
		testData = RandomArrayGenerator.getRandStringArray(NELEMENT * NTHREAD);
		
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int index = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; i++) {
						queue.offer(testData[NELEMENT * index + i]);
					}
				}
			};
		}
		runner.runThreads(threads, "testOfferMT");
		
		assertEquals(NELEMENT * NTHREAD, queue.size());
		
		for (int i = 0; i < testData.length; i++) {
			assertTrue(queue.contains(testData[i]));
		}
	}

	@Test
	public void testPollMT() throws InterruptedException, ExecutionException {
		testData = RandomArrayGenerator.getRandStringArray(NELEMENT * NTHREAD);
		polledData = new LockFreeList<String>();

		for (int i = 0; i < testData.length; i++) {
			queue.offer(testData[i]);
		}
		
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					String temp;
					for (int i = 0; i < NELEMENT; i++) {
						temp = queue.poll();
						assertNotNull(temp);
						polledData.add(temp);
					}
				}
			};
		}
		runner.runThreads(threads, "testPollMT");
		assertNull(queue.poll());
		assertTrue(queue.isEmpty());

		assertEquals(testData.length, polledData.size());
		for (String data : testData) {
			assertTrue(polledData.contains(data));
		}
	}

	@Test
	public void testRemoveMT() throws InterruptedException, ExecutionException {
		testData = RandomArrayGenerator.getRandStringArray(NELEMENT * NTHREAD);
		polledData = new LockFreeList<String>();

		for (int i = 0; i < testData.length; i++) {
			queue.offer(testData[i]);
		}
		assertEquals(0, polledData.size());
		assertEquals(testData.length, queue.size());

		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					String temp;
					for (int i = 0; i < NELEMENT; i += 2) {
						temp = queue.remove();
						assertNotNull(temp);
						polledData.add(temp);
					}
				}
			};
		}
		runner.runThreads(threads, "testRemoveMT");
		
		assertEquals(testData.length / 2, polledData.size());
		assertEquals(testData.length / 2, queue.size());
	}

	@Test
	public void checkForEnDePairMT() throws InterruptedException, ExecutionException {
		testData = RandomArrayGenerator.getRandStringArray(NELEMENT * NTHREAD);
		polledData = new LockFreeList<String>();
		queue = new ConcurrentLinkedQueue<String>();

		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int index = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					if (index % 2 == 0) {// even threads
						for (int i = 0; i < NELEMENT; i++) {
							queue.offer(testData[NELEMENT * index + i]);
						}
					} else { // odd threads
						for (int i = 0; i < NELEMENT; i++) {
							String temp = queue.poll();
							if (temp != null) {
								polledData.add(temp);
							}
						}
					}
				}
			};
		}
		runner.runThreads(threads, "testEnDePairMT");
		
		List<String> list = new ArrayList<String>();

		while (!queue.isEmpty()) {
			list.add(queue.poll());
		}

		assertEquals(NELEMENT * ((NTHREAD + 1) / 2) - polledData.size(), list
				.size());
	}

	@Test(expected = NullPointerException.class)
	public void testOfferNullElement() {
		queue.offer(null);
	}
}
