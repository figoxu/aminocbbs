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

/**
 *
 */
package org.amino.alg.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.amino.alg.graph.GraphAlg;
import org.amino.ds.graph.DirectedGraph;
import org.amino.ds.graph.Node;
import org.amino.ds.graph.UndirectedGraph;
import org.amino.util.AbstractBaseTest;
import org.amino.util.RandomArrayGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Zhi Gan
 * 
 */

@RunWith(Parameterized.class)
public class GraphAlgTest extends AbstractBaseTest {
	@Parameters
	public static Collection parameters() {
		Collection<Integer> tn = getThreadNums();
		int elementNum = getElementNum();

		List<Object[]> args = new ArrayList<Object[]>();

		for (int threadNum : tn) {
			Object[] item1 = { elementNum, threadNum, new Object[] {} };
			args.add(item1);
		}
		return args;
	}

	private DirectedGraph<String> d_graph;
	private UndirectedGraph<String> u_graph;

	/**
	 * 
	 * @param vertex
	 * @param threadNum
	 * @param para
	 */
	public GraphAlgTest(int vertex, int threadNum, Object[] para) {
		super(GraphAlgTest.class, para, threadNum, vertex);

		u_graph = RandomArrayGenerator.getRandUndirectedGraph("Abc", NELEMENT,
				NELEMENT * NTHREAD, true);
		d_graph = RandomArrayGenerator.getRandDirectedGraph("Abc", NELEMENT,
				NELEMENT * NTHREAD, false);
	}

	/**
	 * 
	 */
	@Test(timeout=60000)
	public void testMST() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NTHREAD);
		GraphAlg.getMST(u_graph, executor);
		executor.shutdown();
	}

	@Test(timeout=60000)
	public void testCC() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NTHREAD);
		Collection<Collection<Node<String>>> result = GraphAlg
				.getConnectedComponents(u_graph, executor);
		executor.shutdown();
	}


	@Test(timeout=60000)
	public void testSCC() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NTHREAD);
		Collection<Collection<Node<String>>> result = GraphAlg
				.getStrongComponents(d_graph, executor);
		executor.shutdown();
	}
}
