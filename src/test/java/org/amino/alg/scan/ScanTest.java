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

package org.amino.alg.scan;

import org.amino.alg.scan.ParallelScanner;
import org.amino.alg.scan.SerialScanner;

import junit.framework.*;

/**
 * Test Scan implementation.
 *
 * @author donawa
 *
 */
public class ScanTest extends TestCase {

    private boolean _verbose;

    public ScanTest() {
        _verbose = false;
    }

    public void testParallelScan() {
        final int NUM_THREADS = 5;
        if (_verbose)
            System.out.println("Starting parallel test...");

        ParallelScanner ps = new ParallelScanner(NUM_THREADS);
        SerialScanner ds = new SerialScanner();

        byte[] baseString = new byte[1000];
        for (int i = 0; i < baseString.length; ++i)
            baseString[i] = 'a';

        int[] locations = ps.findAll(baseString, (byte) 'a');
        assertNotNull(locations);
        assertEquals(baseString.length, locations.length);

        byte[] searchString = { 'a', 'c' };

        baseString[400] = (byte) 'c';
        baseString[baseString.length - 1] = 'c';

        locations = ps.findAll(baseString, searchString);

        assertNotNull(locations);
        assertEquals(2, locations.length);
        assertEquals(399, locations[0]);
        assertEquals(baseString.length - 2, locations[1]);

        int[] expectedLocations = ds.findAll(baseString, searchString);
        assertNotNull(expectedLocations);
        assertEquals(expectedLocations.length, locations.length);
        for (int i = 0; i < expectedLocations.length; ++i) {
            // System.out.println("location["+i+":"+locations[i]);
            assertEquals(expectedLocations[i], locations[i]);
        }

        baseString[30] = 'c';
        int resultIndex = ps.findAny(baseString, (byte) 'c');
        assertNotSame(-1, resultIndex);
        assertTrue(30 == resultIndex || (baseString.length - 1 == resultIndex)
                || 400 == resultIndex);

        resultIndex = ps.findAny(baseString, searchString);
        assertNotNull(baseString);
        assertNotSame(-1, resultIndex);
        assertTrue(29 == resultIndex || 998 == resultIndex
                || 399 == resultIndex);

        resultIndex = ps.findNext(baseString, (byte) 'c', 0);
        assertEquals(30, resultIndex);
        resultIndex = ps.findNext(baseString, (byte) 'c', resultIndex + 1);
        assertEquals(400, resultIndex);
        resultIndex = ps.findNext(baseString, (byte) 'c', resultIndex + 1);
        assertEquals(999, resultIndex);
        resultIndex = ps.findNext(baseString, (byte) 'c', resultIndex + 1);
        assertEquals(-1, resultIndex);

        resultIndex = ps.findPrevious(baseString, (byte) 'c', 1000);
        assertEquals(999, resultIndex);
        resultIndex = ps.findPrevious(baseString, (byte) 'c', resultIndex);
        assertEquals(400, resultIndex);
        resultIndex = ps.findPrevious(baseString, (byte) 'c', resultIndex);
        assertEquals(30, resultIndex);
        resultIndex = ps.findPrevious(baseString, (byte) 'c', resultIndex);
        assertEquals(-1, resultIndex);

        resultIndex = ps.findNext(baseString, searchString, 0);
        assertEquals(29, resultIndex);

        resultIndex = ps.findNext(baseString, searchString, resultIndex + 1);
        assertEquals(399, resultIndex);

        resultIndex = ps.findNext(baseString, searchString, resultIndex + 1);
        assertEquals(998, resultIndex);

        resultIndex = ps.findNext(baseString, searchString, resultIndex + 1);
        assertEquals(-1, resultIndex);

        resultIndex = ps.findPrevious(baseString, searchString, 1000);
        assertEquals(998, resultIndex);
        resultIndex = ps.findPrevious(baseString, searchString, resultIndex);
        assertEquals(399, resultIndex);
        resultIndex = ps.findPrevious(baseString, searchString, resultIndex);
        assertEquals(29, resultIndex);
        resultIndex = ps.findPrevious(baseString, searchString, resultIndex);
        assertEquals(-1, resultIndex);

        int numReplaced = ps.replaceAll(baseString, (byte) 'c', (byte) 'r');
        assertEquals(3, numReplaced);
        locations = ps.findAll(baseString, (byte) 'r');
        assertNotNull(locations);
        assertEquals(3, locations.length);
        assertEquals(30, locations[0]);
        assertEquals(400, locations[1]);
        assertEquals(999, locations[2]);

        byte[] targetString = { 'a', 'a', 'a', 'r' };
        byte[] replaceString = { 'x', 'y', 'x', 'y' };

        numReplaced = ps.replaceAll(baseString, targetString, replaceString);
        assertEquals(3, numReplaced);
        locations = ps.findAll(baseString, replaceString);

        assertNotNull(locations);
        assertEquals(3, locations.length);
        assertEquals(27, locations[0]);
        assertEquals(397, locations[1]);
        assertEquals(996, locations[2]);

        byte[] targetString2 = replaceString;
        byte[] replaceString2 = { 'r', 'a', 'r', 'a' };
        numReplaced = ps.replaceAll(baseString, targetString2, replaceString2);
        assertEquals(3, numReplaced);

        byte[] targetString3 = { 'a', 'r', 'a' }; // Test which substring of
                                                    // existing "arara"
                                                    // sequences get identified

        locations = ps.findAll(baseString, targetString3);
        expectedLocations = ds.findAll(baseString, targetString3);
        assertNotNull(locations);
        assertNotNull(expectedLocations);

        assertEquals(expectedLocations.length, locations.length);
        for (int i = 0; i < expectedLocations.length; ++i) {
            // System.out.println("location["+i+":"+locations[i]);
            assertEquals(expectedLocations[i], locations[i]);
        }
        byte[] replaceStringXYZ = { 'x', 'y', 'z' };
        numReplaced = ps
                .replaceAll(baseString, targetString3, replaceStringXYZ);
        assertEquals(3, numReplaced);

        int[] checkLocations = ps.findAll(baseString, replaceStringXYZ);
        assertNotNull(checkLocations);
        assertEquals(locations.length, checkLocations.length);
        for (int i = 0; i < locations.length; ++i)
            assertEquals(locations[i], checkLocations[i]);

        numReplaced = ps
                .replaceAll(baseString, replaceStringXYZ, targetString3);
        assertEquals(3, numReplaced);

        checkLocations = ps.findAll(baseString, targetString3);
        assertNotNull(checkLocations);
        assertEquals(locations.length, checkLocations.length);
        for (int i = 0; i < locations.length; ++i)
            assertEquals(locations[i], checkLocations[i]);

        for (int i = 0; i < baseString.length; ++i)
            baseString[i] = 'a';
        // Create strings "arar" which span borders of chunks to test that the
        // right "ara" substring
        // gets detected. So one chunk will end with "...aaa" and the next
        // beging with "raraaaa...".
        // Need to ensure that the string "ara" gets detected and located
        // properly.
        final int chunkSize = baseString.length / NUM_THREADS;
        int[] lastChunkIndex = new int[NUM_THREADS - 2];
        int start = 0;
        for (int i = 0; i < lastChunkIndex.length; ++i) {
            int nextChunkStart = start + chunkSize;
            lastChunkIndex[i] = nextChunkStart - 1;
            start += chunkSize;

            baseString[nextChunkStart] = 'r';
            baseString[nextChunkStart + 2] = 'r';
        }
        locations = ps.findAll(baseString, targetString3);
        assertNotNull(locations);
        byte[] expectedString = baseString.clone();
        expectedLocations = ds.findAll(expectedString, targetString3);
        assertNotNull(expectedLocations);
        assertEquals(expectedLocations.length, locations.length);
        for (int i = 0; i < expectedLocations.length; ++i) {
            assertEquals(expectedLocations[i], locations[i]);
        }

        // Pathalogical case
        byte[] searchString3 = { 'a', 'r' };
        for (int i = 0; i < baseString.length; ++i) {
            if ((i % 2) == 1)
                baseString[i] = 'r';
            else
                baseString[i] = 'a';
        }
        locations = ps.findAll(baseString, searchString3);
        assertNotNull(locations);
        expectedLocations = ds.findAll(baseString, searchString3);
        assertNotNull(expectedLocations);
        assertEquals(expectedLocations.length, locations.length);
        for (int i = 0; i < expectedLocations.length; ++i) {
            assertEquals(expectedLocations[i], locations[i]);
        }

        byte[] replaceString4 = { 'x', 'y' };
        byte[] serialBase = baseString.clone();
        int count = ps.replaceAll(baseString, searchString3, replaceString4);
        int expectedCount = ds.replaceAll(serialBase, searchString3,
                replaceString4);
        assertEquals(expectedCount, count);
        int curSerialIndex = 0;
        int curParallelIndex = 0;
        while ((curSerialIndex = ds.findNext(baseString, replaceString4,
                curSerialIndex)) >= 0) {
            curParallelIndex = ps.findNext(baseString, replaceString4,
                    curParallelIndex);
            assertEquals(curSerialIndex, curParallelIndex);

            curSerialIndex += replaceString4.length;
            curParallelIndex += replaceString4.length;

        }
        curParallelIndex = ps.findNext(baseString, replaceString4,
                curParallelIndex);
        assertEquals(curSerialIndex, curParallelIndex);

        curSerialIndex = baseString.length;
        curParallelIndex = baseString.length;

        while ((curSerialIndex = ds.findPrevious(baseString, replaceString4,
                curSerialIndex)) >= 0) {
            curParallelIndex = ps.findPrevious(baseString, replaceString4,
                    curParallelIndex);
            // System.out.println("serialindex:"+curSerialIndex+"
            // par:"+curParallelIndex);
            assertEquals(curSerialIndex, curParallelIndex);
            curSerialIndex -= (replaceString4.length - 1);
            curParallelIndex -= (replaceString4.length - 1);
        }
        curParallelIndex = ps.findPrevious(baseString, replaceString4,
                curParallelIndex);
        assertEquals(curSerialIndex, curParallelIndex);
    }

    /**
     * Test Serial scan implementation.
     *
     */
    public void testSerialScan() {
        SerialScanner ds = new SerialScanner();

        if (_verbose)
            System.out.println("Starting serial test...");

        byte[] baseString = new byte[1000];
        for (int i = 0; i < baseString.length; ++i)
            baseString[i] = 'a';

        int[] locations = ds.findAll(baseString, (byte) 'a');
        assertEquals(baseString.length, locations.length);

        baseString[baseString.length - 1] = (byte) 'b';
        locations = ds.findAll(baseString, (byte) 'a');
        assertEquals(baseString.length - 1, locations.length);

        baseString[499] = (byte) 'b';
        int resultIndex = ds.findFirst(baseString, (byte) 'b');
        assertEquals(499, resultIndex);

        byte[] searchString = { 'a', 'b', 'a' };

        resultIndex = ds.findFirst(baseString, searchString);
        assertEquals(498, resultIndex);

        locations = ds.findAll(baseString, searchString);
        assertNotNull(locations);
        assertEquals(1, locations.length);
        assertEquals(498, locations[0]);

        // must sort the array
        byte[] sortedString = baseString.clone();
        java.util.Arrays.sort(sortedString);
        resultIndex = ds.binarySearch(sortedString, (byte) 'b');
        assertEquals(998, resultIndex);

        int count = ds.replaceAll(baseString, (byte) 'b', (byte) 'c');
        assertEquals(2, count);

        byte[] searchString2 = { 'a', 'c' };

        byte[] replaceString = { 'x', 'y' };

        count = ds.replaceAll(baseString, searchString2, replaceString);
        locations = ds.findAll(baseString, replaceString);
        assertEquals(2, count);

        locations = ds.findAll(baseString, replaceString);
        assertNotNull(locations);
        assertEquals(2, locations.length);
        assertEquals(498, locations[0]);
        assertEquals(998, locations[1]);

        resultIndex = ds.findFirst(baseString, (byte) 'x');
        assertEquals(498, resultIndex);
        resultIndex = ds.findNext(baseString, (byte) 'y', resultIndex);
        assertEquals(499, resultIndex);

        resultIndex = ds.findPrevious(baseString, (byte) 'x', resultIndex);
        assertEquals(498, resultIndex);

        resultIndex = ds.findNext(baseString, replaceString, resultIndex + 1);
        assertEquals(998, resultIndex);

        resultIndex = ds.findPrevious(baseString, replaceString,
                resultIndex + 1);
        assertEquals(498, resultIndex);

    }
}
