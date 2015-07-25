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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.amino.ds.graph.AdjacentNode;
import org.amino.ds.graph.DirectedGraph;
import org.amino.ds.graph.DirectedGraphImpl;
import org.amino.ds.graph.Edge;
import org.amino.ds.graph.Graph;
import org.amino.ds.graph.Node;
import org.amino.ds.graph.UndirectedGraph;
import org.amino.util.AbstractBaseTest;
import org.amino.util.ConcurrentRunner;
import org.amino.util.ThreadPoolRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 */
@RunWith(Parameterized.class)
public class GraphTest extends AbstractBaseTest {
	private int vertex;
	private int threadNum;
	private boolean isDirected;

	private Graph<String> graph;
	ConcurrentRunner runner;

	@Parameters
	public static Collection parameters() {
		Collection<Integer> tn = getThreadNums();
		int en = getElementNum();

		List<Object[]> args = new ArrayList<Object[]>();

		for (int num : tn) {
			Object[] item1 = { en, num, new Object[] {}, false };
			Object[] item2 = { en, num, new Object[] {}, true };
			args.add(item1);
			args.add(item2);
		}
		return args;
	}

	public GraphTest(int vertex, int threadNum, Object[] para,
			boolean isDirected) {
		super(UndirectedGraph.class, para, vertex, threadNum);
		this.vertex = vertex;
		this.vertex = 4;

		this.threadNum = threadNum;
		this.isDirected = isDirected;

		if (isDirected) {
			graph = new DirectedGraphImpl<String>();
			runner = new ThreadPoolRunner(threadNum, vertex,
					DirectedGraphImpl.class);
		} else {
			graph = new UndirectedGraph<String>();
			runner = new ThreadPoolRunner(threadNum, vertex,
					UndirectedGraph.class);
		}

	}

	@Test
	public void testAddEdge() throws Throwable {
		Runnable[] runnable = new Runnable[(vertex * (vertex - 1)) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;

				runnable[index++] = new Runnable() {
					public void run() {
						if (!graph.addEdge("" + v, "" + u, 1)) {
							throw new RuntimeException("add edge between " + v
									+ " " + u + " false");
						}
					}
				};
			}
		}

		runner.runThreads(runnable, "testAddEdgeByElement");
	}

	private void _addEdgeSerial() {
		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				if (!graph.addEdge("" + j, "" + i, 1)) {
					throw new RuntimeException("add edge between " + j + " "
							+ i + " false");
				}
			}
		}
	}

	private void _addNodeSerial() {
		for (int i = 0; i < vertex; ++i) {
			Node<String> node = graph.addNode("" + i);
			graph.containsNode(node);
			if (!node.getValue().equals("" + i)) {
				throw new RuntimeException(" add node error, node value = "
						+ node.getValue() + "  i =" + i);
			}

		}
	}

	@Test
	public void testAddEdge2() throws Throwable {
		Runnable[] runnable = new Runnable[(vertex * (vertex - 1)) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;

				runnable[index++] = new Runnable() {
					public void run() {
						Node<String> node1 = new Node<String>("" + v);
						Node<String> node2 = new Node<String>("" + u);
						boolean edge1 = graph.addEdge(node1, node2, 1);
						if (!edge1) {
							throw new RuntimeException("add edge between " + v
									+ " " + u + " false");
						}
						Collection<Edge<String>> edge2 = graph.getEdges(node1,
								node2);
						Assert.assertTrue(edge2.size() > 0);
						boolean found = false;
						for (Edge<String> eg : edge2) {
							if (eg.getStart().equals(node1)
									&& eg.getEnd().equals(node2)) {
								found = true;
								break;
							}
							if (eg.getStart().equals(node2.getValue())
									&& eg.getEnd().equals(node1)) {
								found = true;
								break;
							}
						}
						if (!found)
							Assert.fail("edge is not added " + node1.getValue()
									+ " " + node2.getValue());
					}
				};
			}
		}

		runner.runThreads(runnable, "testAddEdgeByNode");
		// ((AbstractGraph<String>)graph).dumpGraph();
	}

	@Test
	public void testRemoveEdge() throws Throwable {

		_addEdgeSerial();

		Runnable[] runnable = new Runnable[(vertex * (vertex - 1)) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;

				runnable[index++] = new Runnable() {
					public void run() {
						if (!graph.removeEdge("" + v, "" + u)) {
							throw new RuntimeException(" remove edge from " + v
									+ " to " + u + " false");
						}
					}
				};
			}
		}

		runner.runThreads(runnable, "testRemoveEdgeWithEmptyGraph");

		_addEdgeSerial();
		index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;

				runnable[index++] = new Runnable() {
					public void run() {
						if (!graph.removeEdge(new Edge<String>(
								new Node<String>("" + v), new Node<String>(""
										+ u), 1))) {
							throw new RuntimeException(" remove edge from " + v
									+ " to " + u + " false");
						}
					}
				};
			}
		}
		runner.runThreads(runnable, "testRemoveEdgeWithFullGraph");

	}

	@Test
	public void testGetNodes() throws Throwable {
		Runnable[] runnable = new Runnable[vertex];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Node<String>> coll = graph.getNodes("" + u);
					if (coll.size() != 0) {
						throw new RuntimeException(" getNodes.size() = "
								+ coll.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetNodesWithEmptyGraph");

		_addEdgeSerial();
		index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Node<String>> coll = graph.getNodes("" + u);
					if (coll.size() == 0) {
						throw new RuntimeException(" getNodes.size() = "
								+ coll.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetNodesWithFullGraph");
	}

	@Test
	public void testGetAllNodes() throws Throwable {
		Runnable[] runnable = new Runnable[1];
		int index = 0;

		runnable[index++] = new Runnable() {
			public void run() {
				Collection<Node<String>> coll = graph.getAllNodes();
				if (coll.size() != 0) {
					throw new RuntimeException(" getNodes.size() = "
							+ coll.size());
				}
			}
		};

		runner.runThreads(runnable, "testGetAllNodesWithEmptyGraph");

		_addEdgeSerial();
		index = 0;

		runnable[index++] = new Runnable() {
			public void run() {
				Collection<Node<String>> coll = graph.getAllNodes();
				if (coll.size() != vertex) {
					throw new RuntimeException(" getNodes.size() = "
							+ coll.size());
				}
			}
		};
		runner.runThreads(runnable, "testGetAllNodesWithFullGraph");
	}

	@Test
	public void testGetEdges() throws Throwable {
		Runnable[] runnable = new Runnable[(vertex * (vertex - 1)) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;
				runnable[index++] = new Runnable() {
					public void run() {
						Collection<Edge<String>> coll = graph.getEdges("" + v,
								"" + u);
						if (coll.size() != 0) {
							throw new RuntimeException(" getNodes.size() = "
									+ coll.size());
						}

						if (isDirected) {
							coll = graph.getEdges("" + u, "" + v);
							if (coll.size() != 0) {
								throw new RuntimeException(
										" getNodes.size() = " + coll.size());
							}
						}
					}
				};
			}
		}
		runner.runThreads(runnable, "testGetEdgesWithEmptyGraph");

		_addEdgeSerial();
		index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;
				runnable[index++] = new Runnable() {
					public void run() {
						Collection<Edge<String>> coll = graph.getEdges("" + v,
								"" + u);
						if (coll.size() == 0) {
							throw new RuntimeException(" getNodes.size() = "
									+ coll.size());
						}

						if (isDirected) {
							coll = graph.getEdges("" + u, "" + v);
							if (coll.size() != 0) {
								throw new RuntimeException(
										" getNodes.size() = " + coll.size());
							}
						}
					}
				};
			}
		}
		runner.runThreads(runnable, "testGetEdgesWithFullGraph");
	}

	@Test
	public void testAddNode() throws Throwable {
		Runnable[] runnable = new Runnable[vertex];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Node<String> node = graph.addNode("" + u);
					if (!node.getValue().equals("" + u)) {
						throw new RuntimeException(" add node(" + u
								+ ") return node with value " + node.getValue());
					}
				}
			};
		}

		runner.runThreads(runnable, "testAddNode");
		// ((AbstractGraph<String>)graph).dumpGraph();
	}

	@Test
	public void testGetLinkedNode() throws Throwable {
		Runnable[] runnable = new Runnable[vertex];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<AdjacentNode<String>> nodes = graph
							.getLinkedNodes(new Node<String>("" + u));

					if (nodes.size() != 0) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}
				}
			};
		}
		runner.runThreads(runnable, "testGetLinkedNodeWithEmptyGraph");

		_addNodeSerial();

		index = 0;
		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<AdjacentNode<String>> nodes = graph
							.getLinkedNodes(new Node<String>("" + u));

					if (nodes.size() != 0) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}
				}
			};
		}
		runner.runThreads(runnable, "testGetLinkedNodeWithFullGraph");

		_addEdgeSerial();
		index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<AdjacentNode<String>> nodes = graph
							.getLinkedNodes(new Node<String>("" + u));

					if (!isDirected && nodes.size() != (vertex - 1)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}

					if (isDirected && nodes.size() != (vertex - 1 - u)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetLinkedNode");
	}

	@Test
	public void tetGetLinkedEdges() throws Throwable {
		Runnable[] runnable = new Runnable[vertex];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Edge<String>> edges = graph
							.getLinkedEdges(new Node<String>("" + u));

					if (edges.size() != 0) {
						throw new RuntimeException(" linked edges of " + u
								+ " size = " + edges.size());
					}
				}
			};
		}
		runner.runThreads(runnable, "getLinkedEdgesWithEmptyGraph");

		_addNodeSerial();

		index = 0;
		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Edge<String>> edges = graph
							.getLinkedEdges(new Node<String>("" + u));

					if (edges.size() != 0) {
						throw new RuntimeException(" linked edges of " + u
								+ " size = " + edges.size());
					}
				}
			};
		}
		runner.runThreads(runnable, "getLinkedEdgesWithIsolationNodes");

		_addEdgeSerial();
		index = 0;

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Edge<String>> edges = graph
							.getLinkedEdges(new Node<String>("" + u));

					if (!isDirected && edges.size() != (vertex - 1)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + edges.size());
					}

					if (isDirected && edges.size() != (vertex - 1 - u)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + edges.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "getLinkedEdgesWithFullGraph");
	}

	@Test
	public void testContainEdge() throws Throwable {

		_addEdgeSerial();
		Runnable[] runnable = new Runnable[vertex * (vertex - 1) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;
				runnable[index++] = new Runnable() {
					public void run() {
						if (!graph.containsEdge("" + v, "" + u)) {
							throw new RuntimeException(
									" can not find edge from  " + v + " to "
											+ u);
						}
					}
				};
			}
		}
		runner.runThreads(runnable, "testContainEdge");
	}

	@Test
	public void testMultiAction() throws Throwable {
		/**
		 * 0 -- addNode 1 -- addEdge 2 -- removeEdge
		 */

		Runnable[] runnable = new Runnable[(vertex * (vertex - 1)) / 2];
		int index = 0;

		for (int i = 0; i < vertex; ++i) {
			for (int j = 0; j < i; ++j) {
				final int u = i;
				final int v = j;

				runnable[index++] = new Runnable() {
					public void run() {
						double ran = Math.random() * 3;
						if (ran < 1) {
							Node<String> node = graph.addNode("" + v);
							if (!node.getValue().equals("" + v)) {
								throw new RuntimeException(" add node(" + v
										+ ") return node with value "
										+ node.getValue());
							}
						} else if (ran < 2) {
							if (!graph.addEdge("" + v, "" + u, 1)) {
								throw new RuntimeException("add edge between "
										+ v + " " + u + " false");
							}
						} else {
							graph.removeEdge("" + v, "" + u);
						}

					}
				};
			}
		}
		runner.runThreads(runnable, "testMultiAction");
	}

	@Test
	public void testCollectionMethod() throws Throwable {
		_addEdgeSerial();

		List<Runnable> tasks = new ArrayList<Runnable>();

		// Iterator
		tasks.add(new Runnable() {
			public void run() {
				int i = 0;
				Iterator<String> iter = graph.iterator();
				while (iter.hasNext()) {
					iter.next();
					i++;
				}

				if (i != vertex) {
					throw new RuntimeException(" iterator error  i = " + i
							+ " vertex = " + vertex);
				}
			}
		});

		// toArray
		tasks.add(new Runnable() {
			public void run() {
				int i;
				Object[] array = (Object[]) graph.toArray();
				i = array.length;
				if (i != vertex) {
					throw new RuntimeException(" iterator error  = " + i
							+ " vertex = " + vertex);
				}
			}
		});

		// isEmpty
		tasks.add(new Runnable() {
			public void run() {
				if (graph.isEmpty()) {
					throw new RuntimeException(" empty graph");
				}
			}
		});

		Runnable[] run = new Runnable[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			run[i] = tasks.get(i);
		}

		runner.runThreads(run, "testCollectionMethod");

		tasks.clear();
		// removeAll
		tasks.add(new Runnable() {
			public void run() {
				graph.removeAll(Arrays.asList(graph.toArray()));
				if (graph.size() != 0) {
					throw new RuntimeException(" graph.size()=" + graph.size());
				}
			}
		});

		run = new Runnable[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			run[i] = tasks.get(i);
		}

		runner.runThreads(run, "testRemoveAll");
	}

	@Test
	public void testAddAllNodes() throws Throwable {
		String[] nodes = new String[vertex];
		for (int i = 0; i < vertex; ++i) {
			nodes[i] = "" + i;
		}

		graph.addAll(Arrays.asList(nodes));

		if (graph.size() != vertex) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}
	}

	@Test
	public void testContains() throws Throwable {

		String[] nodes = new String[vertex];
		Node<String>[] gnodes = new Node[vertex];

		for (int i = 0; i < vertex; ++i) {
			nodes[i] = "" + i;
			gnodes[i] = new Node<String>(nodes[i]);
		}

		graph.addAll(Arrays.asList(nodes));
		if (graph.size() != vertex) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}

		for (int i = 0; i < vertex; ++i) {
			if (!graph.contains(nodes[i])) {
				throw new RuntimeException(" graph.contains(" + nodes[i]
						+ ")  false");
			}
		}
		if (!graph.containsAll(Arrays.asList(nodes))) {
			throw new RuntimeException(" graph.containsAll(" + nodes
					+ ")  false");
		}

		nodes[vertex - 1] = "" + (vertex + 1);
		if (graph.containsAll(Arrays.asList(nodes))) {
			throw new RuntimeException(" graph.containsAll(" + nodes
					+ ")  false");
		}

		graph.addAllNodes(Arrays.asList(gnodes));
		if (graph.size() != vertex) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}

		graph.clear();
		if (graph.size() != 0) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}
	}

	@Test
	public void testOtherMethod() throws Throwable {
		_addEdgeSerial();
		String[] nodes = new String[vertex / 2];
		for (int i = 0; i < vertex / 2; ++i) {
			nodes[i] = "" + i;
		}

		if (!graph.retainAll(Arrays.asList(nodes))) {
			throw new RuntimeException(
					" graph.retain return false while true expected");
		}

		for (int i = vertex / 2; i < vertex; ++i) {
			if (graph.contains("" + i)) {
				throw new RuntimeException(" graph.contain   " + i);
			}
		}

		if (graph.size() != vertex / 2) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}
		graph.add("" + -1);

		nodes = new String[vertex];
		nodes = graph.toArray(nodes);
		for (int i = 0; i < nodes.length; ++i) {
			if (nodes[i] == null) {
				if (i != graph.size()) {
					throw new RuntimeException(" graph.size()=" + graph.size());
				}
				break;
			}
		}

	}

	@Test
	public void testClone() throws Throwable {
		_addEdgeSerial();
		Graph<String> g = graph.clone();

		if (g.size() != graph.size()) {
			throw new RuntimeException(" graph.size()=" + graph.size()
					+ "  while g.size()=" + g.size());
		}

		Iterator<String> iter = g.iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (!graph.contains(s)) {
				throw new RuntimeException(" graph does not contain " + s);
			}

			Collection<Edge<String>> edges = g.getLinkedEdges(new Node<String>(
					s));
			Iterator<Edge<String>> iterator = edges.iterator();
			while (iterator.hasNext()) {
				Edge<String> e = iterator.next();
				if (!graph.containsEdge(e.getStart().getValue(), e.getEnd()
						.getValue())) {
					throw new RuntimeException(" graph does not contain edge "
							+ e);
				}
			}
			iter.remove();
		}

		if (graph.size() != vertex) {
			throw new RuntimeException(" graph.size()=" + graph.size());
		}

		if (g.size() != 0) {
			throw new RuntimeException(" g.size()=" + g.size());
		}
	}

	@Test
	public void testGetDestionation() throws Throwable {
		if (!(graph instanceof DirectedGraphImpl)) {
			return;
		}

		final DirectedGraph<String> dgraph = (DirectedGraph<String>) graph;

		_addEdgeSerial();
		int index = 0;
		Runnable[] runnable = new Runnable[vertex];

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Node<String>> nodes = dgraph
							.getDestinations(new Node<String>("" + u));

					if (nodes.size() != (vertex - 1 - u)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetDestionation");

	}

	@Test
	public void testGetWeightDestionation() throws Throwable {
		if (!(graph instanceof DirectedGraphImpl)) {
			return;
		}

		final DirectedGraph<String> dgraph = (DirectedGraph<String>) graph;

		_addEdgeSerial();
		int index = 0;
		Runnable[] runnable = new Runnable[vertex];

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<AdjacentNode<String>> nodes = dgraph
							.getWeightDestinations(new Node<String>("" + u));

					if (nodes.size() != (vertex - 1 - u)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetWeightDestionation");

	}

	@Test
	public void testGetSource() throws Throwable {
		if (!(graph instanceof DirectedGraphImpl)) {
			return;
		}

		final DirectedGraph<String> dgraph = (DirectedGraph<String>) graph;

		_addEdgeSerial();
		int index = 0;
		Runnable[] runnable = new Runnable[vertex];

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Node<String>> nodes = dgraph
							.getSources(new Node<String>("" + u));

					if (nodes.size() != u) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}

					Collection<AdjacentNode<String>> weightSources = dgraph
							.getWeightSources(new Node<String>("" + u));

					if (weightSources.size() != u) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + weightSources.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testGetSource");

	}

	@Test
	public void testIncomingOutgoing() throws Throwable {
		if (!(graph instanceof DirectedGraphImpl)) {
			return;
		}

		final DirectedGraph<String> dgraph = (DirectedGraph<String>) graph;

		_addEdgeSerial();
		int index = 0;
		Runnable[] runnable = new Runnable[vertex];

		for (int i = 0; i < vertex; ++i) {
			final int u = i;
			runnable[index++] = new Runnable() {
				public void run() {
					Collection<Edge<String>> nodes = dgraph
							.getIncoming(new Node<String>("" + u));

					if (nodes.size() != u) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + nodes.size());
					}

					Collection<Edge<String>> outgoing = dgraph
							.getOutgoing(new Node<String>("" + u));

					if (outgoing.size() != (vertex - u - 1)) {
						throw new RuntimeException(" adjacent nodes of " + u
								+ " size = " + outgoing.size());
					}
				}
			};
		}

		runner.runThreads(runnable, "testIncomingOutgoing");

	}

	@Test
	public void testEdge() {
		Edge<String> e = new Edge<String>(new Node<String>("1"),
				new Node<String>("2"));
		graph.addEdge(e);

		if (!graph.containsEdge("1", "2")) {
			throw new RuntimeException(
					" graph do not contain edge start from 1 to 2");
		}
	}

}
