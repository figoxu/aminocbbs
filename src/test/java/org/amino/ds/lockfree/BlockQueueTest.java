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
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.amino.util.AbstractBaseTest;
import org.amino.util.ThreadRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test of LockFreeDeque.
 * 
 * @author Zhi Gan
 * 
 */
@RunWith(Parameterized.class)
public class BlockQueueTest extends AbstractBaseTest {

	private final class TestPutAndTakeThread extends Thread {
		private final int threadID;

		private TestPutAndTakeThread(int threadID) {
			super("threadID:" + threadID);
			this.threadID = threadID;
		}

		public void run() {
			for (int i = 0; i < NELEMENT; i++) {
				try {
					if (threadID % 2 == 0)
						defaultQueue.put((long) i);
					else
						assertNotNull(defaultQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Parameters
	public static Collection paras() {
		List<Object[]> args = new ArrayList<Object[]>();

		// args.addAll(genWorkLoadFixedLoad(ConcurrentLinkedQueueAmino.class,
		// new Object[] {}));
		// args.addAll(genWorkLoadFixedLoad(LockFreeDeque.class, new Object[]
		// {}));
		args.addAll(genWorkLoadFixedLoad(LockFreeBlockQueue.class,
				new Object[] { AMINO_NELEMENT }));
		return args;
	}

	public BlockQueueTest(Class classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}

	public LockFreeBlockQueue<Long> defaultQueue;

	boolean verbose = false;
	private ThreadRunner runner;

	@Before
	public void init() throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		defaultQueue = (LockFreeBlockQueue<Long>) getInstance();

		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}

	/**
	 * This is the basic test to add and remove data from the deque Add NTHREADS *
	 * NELEMENTS * 2 data to the Deque (left and right) then remove NTHREADS
	 * *NELEMENTS *2 from the deque (left and right) In the end the size of the
	 * deque should be 0
	 */
	@Test
	public void testOfferAndPoll() throws Throwable {
		for (int i = 0; i < NELEMENT; i++) {
			if (defaultQueue.size() < NELEMENT)
				assertTrue(defaultQueue.offer((long) i));
			else
				assertTrue(!defaultQueue.offer((long) i));
		}

		assertEquals(NELEMENT, defaultQueue.size());

		for (int i = 0; i < NELEMENT; i++) {
			if (defaultQueue.size() > 0)
				assertNotNull(defaultQueue.poll());
			else
				assertNull(defaultQueue.poll());
		}

		assertEquals(0, defaultQueue.size());
	}

	/**
	 * Test clear and offer functions
	 */

	@Test
	public void testClear() {
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long) i));
		}
		defaultQueue.clear();
		assertEquals(0, defaultQueue.size());
	}

	/**
	 * Test first-in last-our scenario
	 */
	@Test
	public void testFIFO() {
		for (long i = 0; i < NELEMENT; i++) {
			defaultQueue.add(i);
		}
		for (long i = 0; i < NELEMENT; i++) {
			final Long res = defaultQueue.poll();
			assertEquals(i, res.longValue());
		}
	}

	/**
	 * test functions on empty deque
	 */
	@Test(expected = NoSuchElementException.class)
	public void testEmpty() {
		Assert.assertNull(defaultQueue.peek());
		defaultQueue.remove();
	}

	/**
	 * test various functions of the deque
	 */
	public void testEmptyRL() {
		Assert.assertNull(defaultQueue.peek());
		Assert.assertNull(defaultQueue.poll());
	}

	@Test
	public void testDrainTo() throws InterruptedException, ExecutionException {
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long)i));
		}
		Assert.assertTrue(!defaultQueue.isEmpty());
		Assert.assertEquals(NELEMENT, defaultQueue.size());

		ArrayList<Long> elementsQueue = new ArrayList<Long>();
		defaultQueue.drainTo(elementsQueue);

		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(elementsQueue.contains((long)i));
		}
	}

	@Test
	public void testPeekAndPollST() {
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long) i));
		}

		for (int i = 0; i < NELEMENT; i++) {
			assertEquals(i, defaultQueue.peek().intValue());
			assertNotNull(defaultQueue.poll());
		}
		assertNull(defaultQueue.peek());
		assertNull(defaultQueue.poll());
	}

	@Test
	public void testElement() {
		defaultQueue.offer(0L);
		defaultQueue.element();
	}

	@Test
	public void testCapacityAndRemainingCapacity() {
		assertEquals(NELEMENT, defaultQueue.capacity());
		for (int i = 1; i <= NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long) i));
			assertEquals(NELEMENT - i, defaultQueue.remainingCapacity());
		}
	}

	@Test
	public void testExpand() {
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long) i));
		}
		assertTrue(!defaultQueue.offer((long) 1));
		defaultQueue.expand(NELEMENT);
		for (int i = 0; i < NELEMENT; i++) {
			assertTrue(defaultQueue.offer((long) i));
		}
	}

	@Test
	public void testPutAndTake() throws InterruptedException,
			ExecutionException {
		Thread[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new TestPutAndTakeThread(threadID);
		}
		runner.runThreads(threads, "testOffer");

		if (NTHREAD % 2 == 0) {
			Assert.assertTrue(defaultQueue.isEmpty());
			Assert.assertEquals(0, defaultQueue.size());
		} else {
			Assert.assertTrue(!defaultQueue.isEmpty());
			Assert.assertEquals(NELEMENT, defaultQueue.size());
		}
	}
}