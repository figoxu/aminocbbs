package org.amino.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.amino.util.Parallelized.CheckFor;
import org.amino.util.Parallelized.InitFor;
import org.amino.util.Parallelized.Threaded;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Zhi Gan
 * 
 */
@RunWith(Parameterized.class)
public class ThreadedTest extends AbstractBaseTest {
	private final class TestThreadThread extends Thread {
		public TestThreadThread(int rank){
			super();
			this.rank = rank;
		}

		private int rank;

		public void run() {
			System.out.println("Thread " + rank + ": thread number = "
					+ NTHREAD);
			MapIntStr.put(rank, "putSomeData" + rank);
			// throw new RuntimeException();
		}
	}

	public ThreadedTest(Object setclass, Object[] params, int nthread,
			int nelement) {
		super(setclass, params, nthread, nelement);
	}

	private ThreadRunner runner;
	ConcurrentHashMap<Integer, String> MapIntStr;

	@Before
	public void setUp() throws Exception {
		MapIntStr = (ConcurrentHashMap<Integer, String>) getInstance();
		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
	}

	@Parameters
	public static Collection<Object[]> queues() {
		AMINO_NELEMENT = Integer.valueOf(prop.getProperty(
				"AMINO_ELEMENTS_QUEUE", String.valueOf(AMINO_NELEMENT)));

		List<Object[]> args = new ArrayList<Object[]>();
		args.addAll(genWorkLoadFixedLoad(ConcurrentHashMap.class,
				new Object[] {}));
		return args;
	}

	@Test
	public void doNothing() {

	}

	@Test(timeout=100000)
	public void testThread() throws Throwable {
		MapIntStr.put(-1, "putSomeData");
		Runnable[] threads = new Thread[NTHREAD];
		for (int threadID = 0; threadID < NTHREAD; threadID++) {
			threads[threadID] = new TestThreadThread(threadID);
		}
		runner.runThreads(threads, "testThread");
		assertEquals(NTHREAD + 1, MapIntStr.size());
	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++)
			JUnitCore.runClasses(ThreadedTest.class);
	}
}
