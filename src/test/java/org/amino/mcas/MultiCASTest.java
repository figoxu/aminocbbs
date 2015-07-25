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

package org.amino.mcas;

import junit.framework.TestCase;

import org.amino.mcas.MultiCAS;
import org.amino.mcas.UnsafeWrapper;
import org.junit.Before;
import org.junit.Test;

import sun.misc.Unsafe;

/**
 * 
 * Unit test of MultiCAS.
 * 
 * @author Xiao Jun Dai
 * 
 */
public class MultiCASTest extends TestCase {

	volatile Integer a;
	volatile Integer b;
	volatile Integer c;
	volatile Integer d;

	MultiCASTest test;

	static final long aOffset;
	static final long bOffset;
	static final long cOffset;
	static final long dOffset;
	private static final Unsafe unsafe = UnsafeWrapper.getUnsafe();
	private static final int NTHREAD = 32;

	static {
		try {
			aOffset = unsafe.objectFieldOffset(MultiCASTest.class
					.getDeclaredField("a"));
			bOffset = unsafe.objectFieldOffset(MultiCASTest.class
					.getDeclaredField("b"));
			cOffset = unsafe.objectFieldOffset(MultiCASTest.class
					.getDeclaredField("c"));
			dOffset = unsafe.objectFieldOffset(MultiCASTest.class
					.getDeclaredField("d"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Before
	protected void setUp() throws Exception {
		test = this;
		a = new Integer(1);
		b = new Integer(2);
		c = new Integer(3);
		d = new Integer(4);
	}

	@Test
	public void testMCASST() {
		Integer aBak = a;
		Integer bBak = b;
		Integer cBak = c;
		Integer dBak = d;

		MultiCAS.mcas(3, new Object[] { test, test, test }, new long[] {
				aOffset, bOffset, cOffset }, new Object[] { aBak, bBak, cBak },
				new Object[] { new Integer(11), new Integer(12),
						new Integer(13) });

		MultiCAS.mcas(3, new Object[] { test, test, test }, new long[] {
				bOffset, cOffset, dOffset }, new Object[] { bBak, cBak, dBak },
				new Object[] { new Integer(22), new Integer(23),
						new Integer(24) });

		assertEquals(a.intValue(), 11);
		assertEquals(b.intValue(), 12);
		assertEquals(c.intValue(), 13);
		assertEquals(d.intValue(), 4);
	}

	@Test
	public void testMCASMT() {
		final Integer aBak = a;
		final Integer bBak = b;
		final Integer cBak = c;
		final Integer dBak = d;

		Thread[] threads = new Thread[NTHREAD];
		for (int i = 0; i < NTHREAD;) {
			threads[i++] = new Thread() {
				public void run() {
					MultiCAS.mcas(3, new Object[] { test, test, test },
							new long[] { aOffset, bOffset, cOffset },
							new Object[] { aBak, bBak, cBak }, new Object[] {
									new Integer(11), new Integer(12),
									new Integer(13) });
				}
			};

			threads[i++] = new Thread() {
				public void run() {
					MultiCAS.mcas(3, new Object[] { test, test, test },
							new long[] { bOffset, cOffset, dOffset },
							new Object[] { bBak, cBak, dBak }, new Object[] {
									new Integer(22), new Integer(23),
									new Integer(24) });
				}
			};
			threads[i++] = new Thread() {
				public void run() {
					MultiCAS.mcas(3, new Object[] { test, test, test },
							new long[] { bOffset, cOffset, dOffset },
							new Object[] { bBak, cBak, dBak }, new Object[] {
									new Integer(32), new Integer(33),
									new Integer(34) });
				}
			};
			threads[i++] = new Thread() {
				public void run() {
					MultiCAS.mcas(3, new Object[] { test, test, test },
							new long[] { bOffset, cOffset, dOffset },
							new Object[] { aBak, bBak, cBak }, new Object[] {
									new Integer(42), new Integer(43),
									new Integer(44) });
				}
			};
		}

		for (Thread thread : threads) {
			thread.start();
		}

		try {
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue((a.equals(11) && b.equals(12) && c.equals(13) && d.equals(4))
				|| (a.equals(1) && b.equals(22) && c.equals(23) && d.equals(24))
				|| (a.equals(1) && b.equals(32) && c.equals(33) && d.equals(34))
				|| (a.equals(1) && b.equals(42) && c.equals(43) && d.equals(44)));
	}
}