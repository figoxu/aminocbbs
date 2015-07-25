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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
 * @author Zhi Gan
 * @author Xiao Jun Dai
 *
 */
@RunWith(Parameterized.class)
public class IteratorTest extends AbstractBaseTest {
    private ConcurrentRunner runner;

    private Collection<String> colStr;

    private Collection<String> data;

    protected Object getInstance() throws InstantiationException,
            IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        if (params.length == 0 && classTested.equals(HashSet.class)) {
            return Collections.synchronizedSet((Set<?>) classTested
                    .newInstance());
        } else if (params.length == 1) {
            Constructor<?> constructor = classTested.getConstructor(int.class);
            return constructor.newInstance(((Integer) params[0]).intValue());
        } else {
            return super.getInstance();
        }
    }

    public IteratorTest(Object classTested, Object[] params, int nthread,
            int nelement) {
        super(classTested, params, nthread, nelement);
    }

    @Parameters
    public static Collection<Object[]> iterators() {
        List<Object[]> args = new ArrayList<Object[]>();

        args.addAll(genWorkLoadFixedLoad(LockFreeDeque.class, new Object[] {}));
        args.addAll(genWorkLoadFixedLoad(LockFreeList.class, new Object[] {}));
        args.addAll(genWorkLoadFixedLoad(LockFreeOrderedList.class, new Object[] {}));
        args.addAll(genWorkLoadFixedLoad(LockFreeSet.class,
                new Object[] { getElementNum() }));
        args.addAll(genWorkLoadFixedLoad(EBDeque.class, new Object[] {}));
        args.addAll(genWorkLoadFixedLoad(LockFreePriorityQueue.class, new Object[] {}));
//        args.addAll(genWorkLoadFixedLoad(LockFreeQueue.class, new Object[] {}));

        return args;
    }

    @Before
    public void setUp() throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);

        colStr = (Collection<String>) getInstance();
        data = new HashSet<String>();
        for (int i = 0; i < NELEMENT; i++) {
            data.add(String.valueOf(i));
            colStr.add(String.valueOf(i));
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmpty() throws Throwable {
        Collection<String> colStr = (Collection<String>) getInstance();
        Iterator<String> iter = colStr.iterator();
        iter.next();
    }

    @Test(timeout=60000)
    public void testIterater() throws Throwable {
        Thread[] threads = new Thread[NTHREAD];
        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    Set<String> sink = new HashSet<String>();
                    Iterator<String> iter = colStr.iterator();
                    int sum = 0;
                    while (iter.hasNext()) {
                        String next = iter.next();
                        assertTrue("didin't find " + next, data.contains(next));
                        sink.add(next);
                        sum++;
                    }
                    assertEquals(NELEMENT, sum);
                    assertEquals(sink.size(), colStr.size());
                    for (String str: sink) {
                        colStr.contains(str);
                    }
                }
            };
        }

        runner.runThreads(threads, "testIterater");
    }

    /**
     * iterater could not be used by multithread. This test must be wrong.
     * @throws Throwable
     */
//  @Test
    public void testSameIteraterUsedByMT() throws Throwable {
        final Iterator<String> iter = colStr.iterator();

        Thread[] threads = new Thread[NTHREAD];
        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    Set<String> sink = new HashSet<String>();
                    int sum = 0;
                    while (iter.hasNext()) {
                        String next = iter.next();
                        assertTrue(data.contains(next));
                        assertTrue(sink.add(next));
                        sum++;
                    }
                    assertEquals(NELEMENT, sum);
                }
            };
        }

        try {
            runner.runThreads(threads, "testSameIteraterUsedByMT");
            assert (false);
        } catch (AssertionError e) {
        } catch (NoSuchElementException e) {
        } catch (NullPointerException e) {
        }
    }

    /**
     *
     * @throws Throwable
     */
    @Test(timeout=60000)
    public void testWeakConsistence() throws Throwable {
        Thread[] threads = new Thread[NTHREAD + 1];
        for (int threadID = 0; threadID < NTHREAD; threadID++) {
            threads[threadID] = new Thread("Thread-" + threadID) {
                public void run() {
                    Set<String> sink = new HashSet<String>();
                    Iterator<String> iter = colStr.iterator();
                    int sum = 0;
                    while (iter.hasNext()) {
                        String next = iter.next();
                        assertTrue(data.contains(next));
                        assertTrue(sink.add(next));
                        sum++;
                    }
                    assertEquals(NELEMENT, sum);
                }
            };
        }

        threads[NTHREAD] = new Thread() {
            public void run() {
                Set<String> sink = new HashSet<String>();
                Iterator<String> iter = colStr.iterator();
                while (iter.hasNext()) {
                    String next = iter.next();
                    assertFalse(sink.contains(next));
                    sink.add(next);
                }
            }
        };

        runner.runThreads(threads, "testWeakConsistence");
    }

}
