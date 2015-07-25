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

package org.amino;

// import org.amino.mcas.MultiCASTest;
// import org.amino.pattern.MasterWorkerTest;
import org.amino.alg.AlgTests;
import org.amino.ds.lockfree.DSTests;
import org.amino.mcas.MultiCASTest;
import org.amino.scheduler.WorkStealingSchedulerTest;
import org.amino.util.RunnerTest;
import org.amino.util.ThreadRunner;
import org.amino.util.ThreadedTest;
import org.junit.AfterClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Zhi Gan
 * 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( { DSTests.class, RunnerTest.class, AlgTests.class,
		MultiCASTest.class, ThreadedTest.class, WorkStealingSchedulerTest.class
/* ,MasterWorkerTest.class */})
public class AllTests {
	public static void main(String[] args) {
		JUnitCore.runClasses(AllTests.class);
	}

	@AfterClass
	public static void cleanup() {
		ThreadRunner.shutdown();
	}
}
