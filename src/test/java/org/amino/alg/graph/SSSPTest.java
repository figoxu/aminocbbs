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

package org.amino.alg.graph;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.amino.alg.graph.GraphAlg;
import org.amino.ds.graph.DirectedGraphImpl;
import org.amino.ds.graph.Graph;
import org.amino.ds.graph.Node;
import org.amino.util.AbstractBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Xiao Jun Dai
 * 
 */
@RunWith(Parameterized.class)
public class SSSPTest extends AbstractBaseTest {

	private final ExecutorService exec = Executors.newFixedThreadPool(NTHREAD);
	private int NELEMENT_SSSPTest = 100;
	private Graph<String> graph;
	// private ShortestPath<String> sssp;
	private final int OFFSET = 50;

	/**
	 * 
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> sets() {
		List<Object[]> args = new ArrayList<Object[]>();

		args.addAll(genWorkLoadFixedLoad(null, null));

		return args;
	}

	/**
	 * 
	 * @param classTested
	 * @param params
	 * @param nthread
	 * @param nelement
	 */
	public SSSPTest(Object classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		graph = new DirectedGraphImpl<String>();
		// sssp = new ShortestPath<String>(graph, NTHREAD);
	}

	/**
     *
     */
	@Test(timeout = 5000)
	public void testDijkstra1() {
		graph.addEdge("s", "t", 10);
		graph.addEdge("s", "y", 5);
		graph.addEdge("t", "y", 2);
		graph.addEdge("y", "t", 3);
		graph.addEdge("t", "x", 1);
		graph.addEdge("y", "x", 9);
		graph.addEdge("z", "x", 6);
		graph.addEdge("x", "z", 4);
		graph.addEdge("z", "s", 7);
		graph.addEdge("y", "z", 2);

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"s"), new Node<String>("s")));
		assertEquals(5, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"s"), new Node<String>("y")));
		assertEquals(8, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"s"), new Node<String>("t")));
		assertEquals(9, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"s"), new Node<String>("x")));
		assertEquals(7, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"s"), new Node<String>("z")));

		// sssp.computeShortestPath(new Node<String>("s"));
		// assertEquals(5, sssp.getShortestPath(new Node<String>("y")));
		// assertEquals(8, sssp.getShortestPath(new Node<String>("t")));
		// assertEquals(9, sssp.getShortestPath(new Node<String>("x")));
		// assertEquals(7, sssp.getShortestPath(new Node<String>("z")));
	}

	/**
     *
     */
	@Test(timeout = 5000)
	public void testDijkstra2() {
		graph.addEdge("0", "1", 10);
		graph.addEdge("0", "4", 100);
		graph.addEdge("0", "3", 30);
		graph.addEdge("1", "2", 50);
		graph.addEdge("2", "4", 10);
		graph.addEdge("3", "2", 20);
		graph.addEdge("3", "4", 60);

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"0"), new Node<String>("0")));
		assertEquals(10, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("0"), new Node<String>("1")));
		assertEquals(50, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("0"), new Node<String>("2")));
		assertEquals(30, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("0"), new Node<String>("3")));
		assertEquals(60, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("0"), new Node<String>("4")));

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"1"), new Node<String>("1")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("1"), new Node<String>("0")));
		assertEquals(50, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("1"), new Node<String>("2")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("1"), new Node<String>("3")));
		assertEquals(60, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("1"), new Node<String>("4")));

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"2"), new Node<String>("2")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("2"), new Node<String>("0")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("2"), new Node<String>("1")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("2"), new Node<String>("3")));
		assertEquals(10, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("2"), new Node<String>("4")));

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"3"), new Node<String>("3")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("3"), new Node<String>("0")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("3"), new Node<String>("1")));
		assertEquals(20, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("3"), new Node<String>("2")));
		assertEquals(30, GraphAlg.getShortestPath(graph, exec,
				new Node<String>("3"), new Node<String>("4")));

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"4"), new Node<String>("4")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("4"), new Node<String>("0")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("4"), new Node<String>("1")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("4"), new Node<String>("2")));
		assertEquals(Double.POSITIVE_INFINITY, GraphAlg.getShortestPath(graph,
				exec, new Node<String>("4"), new Node<String>("3")));
	}

	/**
     *
     */
	@Test(timeout = 60000)
	public void testDijkstra3() {
		// System.out.println("NELEMENT_SSSPTest = " + NELEMENT_SSSPTest);

		for (int i = 1; i < NELEMENT_SSSPTest; i++) {
			graph.addEdge("0", new Integer(i).toString(), 10);
		}

		assertEquals(0, GraphAlg.getShortestPath(graph, exec, new Node<String>(
				"0"), new Node<String>("0")));
		// sssp.computeShortestPath(new Node<String>("0"));
		// assertEquals(0, sssp.getShortestPath(new Node<String>("0")));

		for (int i = 1; i < NELEMENT_SSSPTest; i++) {
			assertEquals(10, GraphAlg.getShortestPath(graph, exec,
					new Node<String>("0"), new Node<String>(String.valueOf(i))));
			// assertEquals(10, sssp.getShortestPath(new Node<String>(new
			// Integer(
			// i).toString())));
		}

		for (int i = 1; i < NELEMENT_SSSPTest; i += OFFSET) {
			// sssp
			// .computeShortestPath(new Node<String>(new Integer(i)
			// .toString()));

			for (int j = 0; j < NELEMENT_SSSPTest; j += OFFSET) {
				if (j == i) {
					assertEquals(0, GraphAlg.getShortestPath(graph, exec,
							new Node<String>(String.valueOf(i)),
							new Node<String>(String.valueOf(j))));
					// assertEquals(0, sssp.getShortestPath(new Node<String>(
					// new Integer(j).toString())));
				} else {
					assertEquals(Double.POSITIVE_INFINITY, GraphAlg
							.getShortestPath(graph, exec, new Node<String>(
									String.valueOf(i)), new Node<String>(String
									.valueOf(j))));
					// assertEquals(Double.POSITIVE_INFINITY, sssp
					// .getShortestPath(new Node<String>(new Integer(j)
					// .toString())));
				}
			}
		}
	}

	/**
     *
     */
	@Test(timeout = 60000)
	public void testDijkstra4() {
		// System.out.println("NELEMENT_SSSPTest = " + NELEMENT_SSSPTest);
		// NELEMENT_SSSPTest=64;

		for (int i = 0; i < NELEMENT_SSSPTest - 1; i++) {
			graph.addEdge(new Integer(i).toString(), new Integer(i + 1)
					.toString(), 1);
		}
		graph.addEdge(new Integer(NELEMENT_SSSPTest - 1).toString(), new Integer(0)
				.toString(), 1);

		for (int i = 0; i < NELEMENT_SSSPTest; i += OFFSET) {
			// sssp
			// .computeShortestPath(new Node<String>(new Integer(i)
			// .toString()));

			for (int j = 0; j < NELEMENT_SSSPTest; j += OFFSET) {
				assertEquals((j - i + NELEMENT_SSSPTest) % NELEMENT_SSSPTest, GraphAlg
						.getShortestPath(graph, exec, new Node<String>(String
								.valueOf(i)), new Node<String>(String
								.valueOf(j))));
			}
		}
	}
}