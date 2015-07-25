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

package org.amino.util;

import java.util.Collection;
import java.util.Random;

import org.amino.ds.graph.DirectedGraph;
import org.amino.ds.graph.DirectedGraphImpl;
import org.amino.ds.graph.Edge;
import org.amino.ds.graph.Graph;
import org.amino.ds.graph.Node;
import org.amino.ds.graph.UndirectedGraph;

public class RandomArrayGenerator {
	// Random seed will change per day
	private static Random r = new Random(System.currentTimeMillis()
			% (1000 * 3600 * 24));

	public static Random getRandom() {
		return r;
	}

	public static void resetRandom() {
		r.setSeed(System.currentTimeMillis() % (1000 * 3600 * 24));
	}

	public static void setSeed(long seed) {
		r.setSeed(seed);
	}

	/**
	 * This method will generate a random graph which contains String objects.
	 * The generated graph doesn't contain duplicated edges, or edge with one
	 * node as both start and end points. The graph is an UndirectedGraph.
	 * 
	 * @param prefix
	 *            prefix of random string.
	 * @param nodeSize
	 *            Number of nodes inside the graph
	 * @param edgeSize
	 *            The upper bound of edges inside the graph. Since we will
	 *            reduce multiplex edge, the actual number of edges will be less
	 *            than it.
	 * @param connected
	 *            if it's true, the generated graph will be connected. Else,
	 *            this method don't care if the graph is connected or not.
	 * @return a random graph
	 */
	public static UndirectedGraph<String> getRandUndirectedGraph(String prefix,
			int nodeSize, int edgeSize, boolean connected) {
		UndirectedGraph<String> graph = new UndirectedGraph<String>();

		Node[] nodes = new Node[nodeSize];

		for (int i = 0; i < nodeSize; i++) {
			nodes[i] = graph.addNode(prefix + i);
		}

		int[] start = getRandIntArray(edgeSize, 0, nodeSize);
		int[] end = getRandIntArray(edgeSize, 0, nodeSize);
		int[] weight = getRandIntArray(edgeSize, 1, nodeSize);

		for (int i = 0; i < edgeSize; i++) {
			Collection<Edge<String>> edges1 = graph.getEdges(nodes[start[i]],
					nodes[end[i]]);
			Collection<Edge<String>> edges2 = graph.getEdges(nodes[end[i]],
					nodes[start[i]]);
			if ((edges1 == null || edges1.size() == 0)
					&& (edges2 == null || edges2.size() == 0))
				if (end[i] != weight[i])
					graph.addEdge(nodes[start[i]], nodes[end[i]], weight[i]);
		}

		if (connected) {
			for (int i = 0; i < nodeSize - 1; i++) {
				graph.addEdge(nodes[i], nodes[i + 1], weight[i]);
			}
		}

		return graph;
	}

	/**
	 * This method will generate a random graph which contains String objects.
	 * The generated graph doesn't contain duplicated edges, or edge with one
	 * node as both start and end points. The graph is an DirectedGraph.
	 * 
	 * @param prefix
	 *            prefix of random string.
	 * @param nodeSize
	 *            Number of nodes inside the graph
	 * @param edgeSize
	 *            The upper bound of edges inside the graph. Since we will
	 *            reduce multiplex edge, the actual number of edges will be less
	 *            than it.
	 * @param connected
	 *            if it's true, the generated graph will be connected. Else,
	 *            this method don't care if the graph is connected or not.
	 * @return a random graph
	 */
	public static DirectedGraph<String> getRandDirectedGraph(String prefix,
			int nodeSize, int edgeSize, boolean connected) {
		DirectedGraph<String> graph = new DirectedGraphImpl<String>();

		Node[] nodes = new Node[nodeSize];

		for (int i = 0; i < nodeSize; i++) {
			nodes[i] = graph.addNode(prefix + i);
		}

		int[] start = getRandIntArray(edgeSize, 0, nodeSize);
		int[] end = getRandIntArray(edgeSize, 0, nodeSize);
		int[] weight = getRandIntArray(edgeSize, 1, nodeSize);

		for (int i = 0; i < edgeSize; i++) {
			graph.addEdge(nodes[start[i]], nodes[end[i]], weight[i]);
		}

		if (connected) {
			for (int i = 0; i < nodeSize - 1; i++) {
				graph.addEdge(nodes[i], nodes[i + 1], weight[i]);
			}
			graph.addEdge(nodes[nodeSize-1], nodes[0], 1);
		}

		return graph;
	}

	public static String[] getRandStringArray(int size) {
		String[] result = new String[size];
		fillRandArray(result);
		return result;
	}

	public static byte[] getRandByteArray(int size) {
		byte[] result = new byte[size];
		fillRandArray(result);
		return result;
	}

	public static char[] getRandCharArray(int size) {
		char[] result = new char[size];
		fillRandArray(result);
		return result;
	}

	public static short[] getRandShortArray(int size) {
		short[] result = new short[size];
		fillRandArray(result);
		return result;
	}

	public static int[] getRandIntArray(int size) {
		int[] result = new int[size];
		fillRandArray(result);
		return result;
	}

	public static int[] getRandIntArray(int size, int minimal, int maximize) {
		int[] result = new int[size];
		fillRandArray(result, minimal, maximize);
		return result;
	}

	public static long[] getRandLongArray(int size) {
		long[] result = new long[size];
		fillRandArray(result);
		return result;
	}

	public static float[] getRandFloatArray(int size) {
		float[] result = new float[size];
		fillRandArray(result);
		return result;
	}

	public static double[] getRandDoubleArray(int size) {
		double[] result = new double[size];
		fillRandArray(result);
		return result;
	}

	public static void fillRandArray(byte[] a) {
		final int span = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
		for (int i = 0; i < a.length; i++) {
			a[i] = (byte) (r.nextInt(span) + Byte.MIN_VALUE);
		}
	}

	public static void fillRandArray(char[] a) {
		final int span = Character.MAX_CODE_POINT - Character.MIN_CODE_POINT
				+ 1;
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (r.nextInt(span) + Character.MIN_CODE_POINT);
		}
	}

	public static void fillRandArray(String[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = String.valueOf(r.nextInt());
		}
	}

	public static void fillRandArray(short[] a) {
		final int span = Short.MAX_VALUE - Short.MIN_VALUE + 1;
		for (int i = 0; i < a.length; i++) {
			a[i] = (short) (r.nextInt(span) + Short.MIN_VALUE);
		}
	}

	public static void fillRandArray(int[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = r.nextInt();
		}
	}

	public static void fillRandArray(int[] a, int min, int max) {
		for (int i = 0; i < a.length; i++) {
			a[i] = r.nextInt(max - min) + min;
		}
	}

	public static void fillRandArray(long[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = r.nextLong();
		}
	}

	public static void fillRandArray(float[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = r.nextFloat();
		}
	}

	public static void fillRandArray(double[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = r.nextDouble();
		}
	}
}
