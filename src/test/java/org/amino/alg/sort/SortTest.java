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

package org.amino.alg.sort;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import org.amino.alg.sort.DefaultSorter;
import org.amino.alg.sort.InsertionSorter;
import org.amino.alg.sort.QuickSorter;
import org.amino.alg.sort.Sorter;
import org.amino.util.RandomArrayGenerator;
import org.junit.Test;

/**
 * test case for sort algorithm.
 *
 */
public class SortTest extends TestCase {
	RandomArrayGenerator g = new RandomArrayGenerator();
	Random r = new Random(System.currentTimeMillis());
	boolean verbose;

	/**
	 *
	 */
	public SortTest() {
		this(false);
	}

	/**
	 *
	 * @param _verbose whether to print verbose information
	 */
	public SortTest(boolean _verbose) {
		verbose = _verbose;
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testByteSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing byte[] .. ");

		try {
			byte[] a = RandomArrayGenerator.getRandByteArray(size);
			byte[] b = new byte[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			Arrays.fill(b, 0, prefix, (byte) 0);
			Arrays.fill(b, suffix, size, (byte) 42);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testCharSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing char[] .. ");

		try {
			char[] a = RandomArrayGenerator.getRandCharArray(size);
			char[] b = new char[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			Arrays.fill(b, 0, prefix, '$');
			Arrays.fill(b, suffix, size, '#');

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == '$');
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == '#');

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == '$');
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == '#');
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testShortSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing short[] .. ");

		try {
			short[] a = RandomArrayGenerator.getRandShortArray(size);
			short[] b = new short[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			Arrays.fill(b, 0, prefix, (short) 0);
			Arrays.fill(b, suffix, size, (short) 42);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testIntSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing int[] .. ");

		try {
			int[] a = g.getRandIntArray(size);
			int[] b = new int[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++) {
				final int x = a[i], y = a[i - 1];
				assertTrue(x >= y);
			}

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			Arrays.fill(b, 0, prefix, 0);
			Arrays.fill(b, suffix, size, 42);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testLongSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing long[] .. ");

		try {
			long[] a = RandomArrayGenerator.getRandLongArray(size);
			long[] b = new long[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			Arrays.fill(b, 0, prefix, 0);
			Arrays.fill(b, suffix, size, 42);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == 0);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == 42);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testFloatSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing float[] .. ");

		try {
			float[] a = RandomArrayGenerator.getRandFloatArray(size);
			float[] b = new float[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			final float pval = (float) Math.E, sval = (float) Math.PI;
			Arrays.fill(b, 0, prefix, pval);
			Arrays.fill(b, suffix, size, sval);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == pval);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == sval);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == pval);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == sval);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testDoubleSort(Sorter s, int size) {
//		if (verbose)
//			System.out.print("Testing double[] .. ");

		try {
			double[] a = g.getRandDoubleArray(size);
			double[] b = new double[size];
			System.arraycopy(a, 0, b, 0, size);

			/* Test sort into non-descending order */
			s.sort(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] >= a[i - 1]);

			/* Test sort into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a);
			for (int i = 1; i < size; i++)
				assertTrue(a[i] <= a[i - 1]);

			if (size < 2)
				return;

			/* Prepare array b for testing sort of array segment */
			final int prefix = r.nextInt(size / 2), suffix = size - prefix;
			final double pval = Math.E, sval = Math.PI;
			Arrays.fill(b, 0, prefix, pval);
			Arrays.fill(b, suffix, size, sval);

			/* Test sort of array segment into non-descending order */
			System.arraycopy(b, 0, a, 0, size);
			s.sort(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] >= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == pval);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == sval);

			/* Test sort of array segment into non-ascending order */
			System.arraycopy(b, 0, a, 0, size);
			s.reverse(a, prefix + 1, suffix);
			for (int i = prefix + 2; i < suffix; i++)
				assertTrue(a[i] <= a[i - 1]);
			for (int i = 0; i < prefix; i++)
				assertTrue(a[i] == pval);
			for (int i = suffix; i < size; i++)
				assertTrue(a[i] == sval);
		} finally {
//			if (verbose)
//				System.out.println("correct.");
		}
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param size array size
	 */
	public void testAllTypes(Sorter s, int size) {
		testByteSort(s, size);
		testCharSort(s, size);
		testShortSort(s, size);
		testIntSort(s, size);
		testLongSort(s, size);
		testFloatSort(s, size);
		testDoubleSort(s, size);
	}

	/**
	 *
	 * @param s sorter used to do sorting work
	 * @param name name
	 * @param size array size
	 */
	public void testSorter(Sorter s, String name, int size) {
//		System.out.print(name + ": ");
//		if (verbose)
//			System.out.println();
		testAllTypes(s, size);
//		if (!verbose)
//			System.out.println("correct.");
	}

	/**
	 *
	 * @param size array size
	 */
	public void testAllSorters(int size) {
//		System.out.println("* Testing size=" + size + " *");
		testSorter(new DefaultSorter(), "DefaultSorter", size);
		testSorter(new InsertionSorter(), "InsertionSorter", size);

		testSorter(new QuickSorter(), "QuickSorter", size);

//		testSorter(new ParallelQuickSorterWorkStealing(),
//				"ParallelQuickSorterWorkStealing", size);
//		testSorter(new ParallelQuickSorterWorkStealing(10),
//				"ParallelQuickSorterWorkStealing(10)", size);
	}

	/**
	 *
	 */
	@Test
	public void testAllSizes() {
		Random r = new Random(System.currentTimeMillis());
		testAllSorters(1);
		testAllSorters(2);
		testAllSorters(512);
		testAllSorters(r.nextInt(100) + 1);
	}
}
