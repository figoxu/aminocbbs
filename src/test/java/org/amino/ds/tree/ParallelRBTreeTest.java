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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.amino.ds.tree.ParallelRBTree;
import org.amino.ds.tree.ParallelRBTree.Node;
import org.amino.pattern.internal.Doable;
import org.amino.util.AbstractBaseTest;
import org.amino.util.Loggers;
import org.amino.util.RandomArrayGenerator;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unittest of ParallelRBTree.
 * 
 * @author Xiao Jun Dai
 * 
 */
@RunWith(Parameterized.class)
public class ParallelRBTreeTest extends AbstractBaseTest {

    ParallelRBTree<Integer> bst;
    private ThreadRunner runner;
    protected int count;
    private int NELEMENT_Parallel = 50;
    int[] testData = RandomArrayGenerator.getRandIntArray(NELEMENT_Parallel * NTHREAD);

    @Parameters
    public static Collection paras() {
        List<Object[]> args = new ArrayList<Object[]>();
        args.addAll(genWorkLoadFixedLoad(ParallelRBTree.class, new Object[] {}));
        // args.addAll(genWorkLoad1(RelaxedRBTree.class, new Object[] {}));
        return args;
    }

    public ParallelRBTreeTest(Class classTested, Object[] params, int nthread,
            int nelement) {
        super(classTested, params, nthread, nelement);
    }

    @Before
    public void setUp() throws Exception {
        bst = new ParallelRBTree<Integer>();
        runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT_Parallel);
    }

    @Test(timeout = 60000)
    public void testInsertST() {
        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            // System.out.println("insert " + testData[i]);
            bst.insert(testData[i]);
            // bst.printBinTree();
        }
        // System.out.println("here");
        assertTrue(bst.verifyRBTreeHeight());

        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            if (!bst.find(testData[i])) {
                // bst.printBinTree();
                System.out.println("didn't find " + testData[i]);
                assert false;
            }
        }

        bst.shutdown();
    }

    @Test(timeout = 60000)
    public void testInsertRemoveST() {
        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            // System.out.println("insert " + testData[i]);
            bst.insert(testData[i]);
            // bst.printBinTree();
        }
        assertTrue(bst.verifyRBTreeHeight());

        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            assertTrue("didn't find " + testData[i], bst.find(testData[i]));
        }

        // bst.printBinTree();

        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            // System.out.println("remove " + testData[i]);
            if (!bst.remove(testData[i])) {
                // bst.printBinTree();
                // System.out.println("test data = " +
                // Arrays.toString(testData));
                assertTrue(bst.find(testData[i]));
                assert false;
            }
            // assertTrue(bst.remove(testData[i]));
            // bst.printBinTree();
        }

        // bst.printBinTree();

        for (int i = 0; i < NELEMENT_Parallel; ++i) {
            // System.out.println("----------------------------------find " +
            // testData[i] + "----------------------------------");
            if (bst.find(testData[i])) {
                // bst.printBinTree();
                // assertFalse(testData[i] + " should not exist", bst
                // .find(testData[i]));
                assert false;
            }
        }

        bst.shutdown();
    }

    @Test(timeout = 60000)
    public void testInsertMT() throws Throwable {
        Thread[] threads = new Thread[NTHREAD];

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        bst.insert(testData[NELEMENT_Parallel * id + i]);
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeInsert");

        assertTrue(bst.verifyRBTreeHeight());
        // bst.printLeafs();

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        assertTrue(
                                "cannot find " + testData[NELEMENT_Parallel * id + i],
                                bst.find(testData[NELEMENT_Parallel * id + i]));
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeFind");

        bst.shutdown();
    }

    @Test(timeout = 60000)
    public void testInsertRemoveMT() throws Throwable {
        Thread[] threads = new Thread[NTHREAD];

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        bst.insert(testData[NELEMENT_Parallel * id + i]);
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeInsert");

        // Thread.sleep(5000);

        assertTrue(bst.verifyRBTreeHeight());
        // bst.printLeafs();

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        assertTrue(
                                "cannot find " + testData[NELEMENT_Parallel * id + i],
                                bst.find(testData[NELEMENT_Parallel * id + i]));
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeFind");

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        assertTrue(bst.remove(testData[NELEMENT_Parallel * id + i]));
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeRemove");

        assertTrue(bst.verifyRBTreeHeight());

        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        if (bst.find(testData[NELEMENT_Parallel * id + i])) {
                            System.out.println("should not find "
                                    + testData[NELEMENT_Parallel * id + i]);
                            bst.printBinTree();
                            assert false;
                        }
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeUnfind");

        bst.shutdown();
    }

    @Test(timeout = 60000)
    public void testInsertRemovePairMT() throws Throwable {
        Thread[] threads = new Thread[NTHREAD];

        for (int threadID = NTHREAD / 2; threadID < NTHREAD; threadID++) {
            for (int i = 0; i < NELEMENT_Parallel; ++i) {
                // System.out.println("i = " + i);
                bst.insert(testData[NELEMENT_Parallel * threadID + i]);
                // System.out.println("inserting " + testData[NELEMENT_Parallel *
                // threadID + i]);
            }
        }

        for (int threadID = 0; threadID < NTHREAD / 2; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        bst.insert(testData[NELEMENT_Parallel * id + i]);
                    }
                }
            };
        }

        for (int threadID = NTHREAD / 2; threadID < NTHREAD; threadID++) {
            final int id = threadID;
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    for (int i = 0; i < NELEMENT_Parallel; ++i) {
                        assertTrue(bst.remove(testData[NELEMENT_Parallel * id + i]));
                    }
                }
            };
        }
        runner.runThreads(threads, "testParallelRBTreeInsertRemovePairMT");

        for (int threadID = 0; threadID < NTHREAD / 2; threadID++) {
            for (int i = 0; i < NELEMENT_Parallel; ++i) {
                assertTrue(
                        testData[NELEMENT_Parallel * threadID + i] + " doesn't exist",
                        bst.find(testData[NELEMENT_Parallel * threadID + i]));
            }
        }

        for (int threadID = NTHREAD / 2; threadID < NTHREAD; threadID++) {
            for (int i = 0; i < NELEMENT_Parallel; ++i) {
                if (bst.find(testData[NELEMENT_Parallel * threadID + i])) {
                    assertFalse(testData[NELEMENT_Parallel * threadID + i]
                            + " should not exist!", true);

                }
            }
        }

        bst.shutdown();
    }

    @Test(timeout = 60000)
    public void testWalk() {
        for (int i = 0; i < NELEMENT_Parallel; i++) {
            // System.out.println("inserting " + i + ": " + testData[i]);
            bst.insert(testData[i]);
        }

        bst.inOrderWalk(new Doable<Integer, Integer>() {
            public Integer run(Integer input) {
                count++;
                return null;
            }
        });
        bst.shutdown();

        assertEquals(NELEMENT_Parallel * 2 - 1, count);
    }

    @Test(timeout = 60000)
    public void testSearch() {
        for (int i = 0; i < NELEMENT_Parallel; i++) {
            bst.insert(testData[i]);
        }
        Node<Integer> resNode = bst.search(testData[NELEMENT_Parallel / 2]);

        bst.shutdown();
        assertEquals(testData[NELEMENT_Parallel / 2], resNode.getValue().intValue());
    }

    @Test(timeout = 60000)
    public void testBalance() {
        for (int i = 0; i < NELEMENT_Parallel; i++) {
            bst.insert(i);
        }

        for (int i = 0; i < NELEMENT_Parallel; i++) {
            assertTrue(bst.find(i));
        }

        assertTrue(bst.verifyRBTreeHeight());
        bst.shutdown();
    }
    public static void main(String[] args){
    	Loggers.silent = true;
		JUnitCore.runClasses(ParallelRBTreeTest.class);
		ThreadRunner.shutdown();
    }
}
