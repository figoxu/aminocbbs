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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.List;

import org.amino.util.AbstractBaseTest;
import org.amino.util.ConcurrentRunner;
import org.amino.util.ThreadRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unittest of Stack.
 * @author Xiao Jun Dai
 *
 */
@RunWith(Parameterized.class)
public class StackTest extends AbstractBaseTest {

	@Parameters
	public static Collection paras() {
		List<Object[]> args = new ArrayList<Object[]>();

		args.addAll(genWorkLoadFixedLoad(EBStack.class, new Object[] {}));
//		args.addAll(genArguments(JDKStack.class, new Object[] {}));

		return args;
	}

	boolean _verbose = false;

	public StackTest(Object classTested, Object[] params, int nthread,
			int nelement) {
		super(classTested, params, nthread, nelement);
	}

	private ConcurrentRunner runner;
	private IStack<Long> mystack;

	@Before
	public void setup() throws SecurityException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		runner = ThreadRunner.getRunner(classTested, NTHREAD, NELEMENT);
		mystack = (IStack<Long>) getInstance();
	}

	@Test
	public void concurrentVisit() throws Throwable {
		Thread[] threads = new Thread[NTHREAD];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {
				public void run() {
					long id = Thread.currentThread().getId();

					for (int i = 0; i < (int) NELEMENT; ++i) {
						mystack.push(new Long(id));
						Long result = mystack.pop();
						if (result != null) {
							if (_verbose && id != result.longValue()) {
								System.out.println(id + " != "
										+ result.longValue());
							}
						}
					}
				}
			};
		}
		runner.runThreads(threads, "testStack");
	}

	@Test
	public void sequentialVisit() throws Throwable {
		for (int i = 0; i < (int) NELEMENT; ++i) {
			mystack.push((long)i);
			assertFalse(mystack.isEmpty());
			Long result = mystack.pop();
			assertEquals(result.intValue(), i);
		}


		for (int i = 0; i < (int) NELEMENT; ++i) {
			mystack.push((long)i);
		}

		for (int i = 0; i < (int) NELEMENT; ++i) {
			assertEquals(mystack.peek().intValue(), NELEMENT-1-i);
			assertEquals(mystack.pop().intValue(), NELEMENT-1-i);
		}
	}

	@Test(expected=EmptyStackException.class)
	public void testEmptyStack(){
		assertTrue(mystack.isEmpty());
		assertNull(mystack.peek());
		mystack.pop();
	}

	public static void main(String[] args) {
		JUnitCore.runClasses(StackTest.class);
	}
}
