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

package org.amino.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.amino.scheduler.internal.Scheduler;
import org.amino.scheduler.internal.WorkStealingScheduler;
import org.junit.Assert;
import org.junit.Test;

public class WorkStealingSchedulerTest {
    @Test(timeout=10000)
    public void testWorkStealing() throws Throwable {
        Scheduler ws = new WorkStealingScheduler(4);
        final BlockingQueue<Integer> res = new LinkedBlockingQueue<Integer>();
        for (int i = 0; i < 128; i++) {
            final int index = i;
            ws.submit(new Runnable() {
                public void run() {
                    while (!res.offer(index)) {
                        ;
                    }
                }
            });
        }
        ws.shutdown();
        boolean waitRes = ws.awaitTermination(10, TimeUnit.SECONDS);
		Assert.assertTrue(waitRes);
        Assert.assertEquals(res.size(), 128);
        Integer[] array = res.toArray(new Integer[] {});
        Arrays.sort(array);
        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(array[i].compareTo(i), 0);
        }
    }

    @Test(timeout=10000)
    public void testShutdownNow() {
        Scheduler ws = new WorkStealingScheduler(4);
        final List<Runnable> rejected = new ArrayList<Runnable>();
        ws.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                rejected.add(r);
            }
        });
        final BlockingQueue<Integer> res = new LinkedBlockingQueue<Integer>();
        for (int i = 0; i < 128; i++) {
            final int index = i;
            ws.submit(new Runnable() {
                public void run() {
                    while (!res.offer(index)) {
                        ;
                    }
                }
            });
        }
        List<Runnable> lefted = ws.shutdownNow();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(res.size() + lefted.size(), rejected.size(), 128);
        for (Iterator<Runnable> iterator = lefted.iterator(); iterator
                .hasNext();) {
            Runnable runer = iterator.next();
            runer.run();
        }
        Integer[] array = res.toArray(new Integer[] {});
        Arrays.sort(array);
        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(array[i].compareTo(i), 0);
        }
    }

    @Test(timeout=10000)
    public void testReject() throws InterruptedException {
        Scheduler ws = new WorkStealingScheduler(4);
        final List<Runnable> lefted = new ArrayList<Runnable>();
        ws.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                lefted.add(r);
            }
        });
        final BlockingQueue<Integer> res = new LinkedBlockingQueue<Integer>();
        for (int i = 0; i < 128; i++) {
            if (i == 64) {
                ws.shutdown();
            }
            final int index = i;
            ws.submit(new Runnable() {
                public void run() {
                    while (!res.offer(index)) {
                        ;
                    }
                }
            });
        }
        ws.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("res.size()=" + res.size());
        Assert.assertEquals(res.size(), 64);
        Assert.assertEquals(lefted.size(), 64);
        /*
         * if (res.size() != 64) Assert.assertEquals(128 / 2, res.size());
         */
        for (Iterator<Runnable> iterator = lefted.iterator(); iterator
                .hasNext();) {
            Runnable runer = iterator.next();
            runer.run();
        }
        Assert.assertEquals(res.size(), 128);
        Integer[] array = res.toArray(new Integer[] {});
        Arrays.sort(array);
        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(array[i].intValue(), i);
        }
    }
}
