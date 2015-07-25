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

package org.amino.alg.parallelprefix;

import org.amino.alg.parallelprefix.AbstractParallelPrefix;
import org.amino.alg.parallelprefix.DefaultAddBinaryOp;
import org.amino.alg.parallelprefix.ThreadedParallelPrefix;

import junit.framework.TestCase;

/**
 * Test Scan implementation.
 *
 * @author donawa
 *
 */
public class ParallelPrefixTest extends TestCase {
    class SerialPrefixInt extends AbstractParallelPrefix<Integer> {
    }

    class ParallelPrefixInt extends ThreadedParallelPrefix<Integer> {
    }

    class basicOp extends DefaultAddBinaryOp<Integer> {
    }

    class fancyOp extends DefaultAddBinaryOp<Integer> {
        public Integer transform(Integer a, Integer b) {
            return new Integer(a.intValue() + b.intValue());
        }
    }

    /**
     * Test serial implementation.
     */
    public void testSerialParallelPrefix() {
        SerialPrefixInt ppTest = new SerialPrefixInt();

        // integer test
        {
            int[] inputArray = new int[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals(0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals(i, inputArray[i]);
            }
        }

        // long test
        {
            long[] inputArray = new long[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals(0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals(i, inputArray[i]);
            }
        }

        // float test
        {
            float[] inputArray = new float[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals((float) 0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals((float) i, inputArray[i]);
            }
        }

        // short test
        {
            short[] inputArray = new short[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals(0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals(i, inputArray[i]);
            }
        }
        // char test
        {
            char[] inputArray = new char[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals(0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals((char) i, inputArray[i]);
            }
        }
        // byte test
        {
            byte[] inputArray = new byte[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals(0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals((byte) i, inputArray[i]);
            }
        }

        // double test
        {
            double[] inputArray = new double[1000];

            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = 1;

            ppTest.scan(inputArray, inputArray, new basicOp());
            assertEquals((double) 0, inputArray[0]);
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals((double) i, inputArray[i]);
            }
        }

        // Integer test
        {
            Integer[] inputArray = new Integer[1000];

            inputArray[0] = new Integer(0);
            for (int i = 1; i < inputArray.length; ++i)
                inputArray[i] = new Integer(1);

            ppTest.scan(inputArray, inputArray, new fancyOp());
            assertEquals(0, inputArray[0].intValue());
            for (int i = 0; i < inputArray.length; ++i) {
                assertEquals(i, inputArray[i].intValue());
            }
        }
    }

    /**
     * Test default implementation.
     */
    public void testDefaultParallelPrefix() {
        final int TESTSIZE = 30;
        ThreadedParallelPrefix<Integer> tppTest = new ThreadedParallelPrefix<Integer>(
                5);
        tppTest.setMinimumArraySize(20); // ensure parallel portion used.
        SerialPrefixInt st = new SerialPrefixInt();
        basicOp addOp = new basicOp();

        // int
        {
            int[] input = new int[TESTSIZE];
            int[] output = new int[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = i;
            int[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);

            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // long
        {
            long[] input = new long[TESTSIZE];
            long[] output = new long[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = i;
            long[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // char
        {
            char[] input = new char[TESTSIZE];
            char[] output = new char[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = (char) i;
            char[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // byte
        {
            byte[] input = new byte[TESTSIZE];
            byte[] output = new byte[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = (byte) i;
            byte[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // float
        {
            float[] input = new float[TESTSIZE];
            float[] output = new float[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = i;
            float[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // short
        {
            short[] input = new short[TESTSIZE];
            short[] output = new short[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = (short) i;
            short[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // double
        {
            double[] input = new double[TESTSIZE];
            double[] output = new double[input.length];
            for (int i = 0; i < input.length; i++)
                input[i] = i;
            double[] expected = input.clone();

            st.scan(expected, expected, addOp);

            tppTest.scan(input, output, addOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i], output[i]);
        }
        // T (as Integer)
        {
            Integer[] input = new Integer[TESTSIZE];
            Integer[] output = new Integer[input.length];
            Integer[] expected = new Integer[output.length];
            for (int i = 0; i < input.length; i++) {
                input[i] = new Integer(i);
                expected[i] = new Integer(i);
            }

            fancyOp genericAddOp = new fancyOp();
            st.scan(expected, expected, genericAddOp);

            tppTest.scan(input, output, genericAddOp);
            // now verify
            for (int i = 0; i < expected.length; ++i)
                assertEquals(expected[i].intValue(), output[i].intValue());
        }
    }
}
