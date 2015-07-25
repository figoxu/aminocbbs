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

package org.amino.ds.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.amino.ds.lockfree.LockFreeList;
import org.amino.mcas.LockFreeBSTree;
import org.amino.util.AbstractBaseTest;
import org.amino.util.RandomArrayGenerator;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unittest of LockFreeBSTree.
 *
 * @author Xiao Jun Dai
 *
 */
@RunWith(Parameterized.class)
public class LockFreeBSTreeTest extends AbstractBaseTest {
//	private static final int NELEMENT = 10000;
//	private static final int NTHREAD = 32;
	LockFreeBSTree<String, String> bst;
    String[] testData = RandomArrayGenerator.getRandStringArray(NELEMENT * NTHREAD);

	private ThreadRunner runner;

    @Parameters
    public static Collection paras() {
        List<Object[]> args = new ArrayList<Object[]>();

        args.addAll(genWorkLoadFixedLoad(LockFreeBSTree.class, new Object[] {}));

        return args;
    }

    public LockFreeBSTreeTest(Class classTested, Object[] params, int nthread,
            int nelement) {
        super(classTested, params, nthread, nelement);
    }

	@Before
	public void setUp() throws Exception {
		bst = new LockFreeBSTree<String, String>();
		runner = ThreadRunner
                .getRunner(classTested, NTHREAD, NELEMENT);
	}

	@Test(timeout=10000)
	public void testUpdateST() {
		for (int i = 0; i < NELEMENT; ++i) {
			bst.update(String.valueOf(i), "test" + i);
//			System.out.println("added " + i);
		}

		for (int i = 0; i < NELEMENT; ++i) {
			assertNotNull(bst.find(String.valueOf(i)));
		}
	}

	@Test(timeout=10000)
	public void testRemoveST() {
		for (int i = 0; i < NELEMENT; ++i) {
			bst.update(String.valueOf(i), "test" + i);
		}

		for (int i = 0; i < NELEMENT; ++i) {
			assertNotNull(bst.find(String.valueOf(i)));
		}

		for (int i = 0; i < NELEMENT; ++i) {
			bst.remove(String.valueOf(i));
		}

		for (int i = 0; i < NELEMENT; ++i) {
			assertNull(bst.find(String.valueOf(i)));
		}

	}

	@Test(timeout=60000)
	public void testUpdateAndRemoveMT() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int id = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {

				public void run() {
					for (int i = 0; i < NELEMENT; ++i) {
						bst.update(testData[NELEMENT * id + i], "test"
                                + i);
//						System.out.println("Thread " + id + " added " + i);
					}
				}
			};
		}
		runner.runThreads(threads, "testLockfreeBSTUpdate");

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int id = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; ++i) {
						assertNotNull("cannot find " + i, bst.find(testData[NELEMENT * id + i]));
//						System.out.println("Checking " + i);
					}
				}
			};
		}
		runner.runThreads(threads, "testLockfreeBSTFind");

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int id = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; ++i) {
						bst.remove(testData[NELEMENT * id + i]);
//						System.out.println("Removed " + i);
					}
				}
			};
		}
		runner.runThreads(threads, "testLockfreeBSRemove");

		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			final int id = threadID;
			threads[threadID] = new Thread("Thread-" + threadID) {
				public void run() {
					for (int i = 0; i < NELEMENT; ++i) {
						assertNull(bst.find(testData[NELEMENT * id + i]));
//						System.out.println("Checking " + i);
					}
				}
			};
		}
		runner.runThreads(threads, "testLockfreeBSTUnfind");

	}
}
