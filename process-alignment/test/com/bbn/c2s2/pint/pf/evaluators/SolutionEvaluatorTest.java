/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
/* =============================================================================
 *
 *                  COPYRIGHT 2010 BBN Technologies Corp.
 *                  1300 North 17th Street, Suite 600
 *                       Arlington, VA  22209
 *                          (703) 284-1200
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * ==============================================================================
 */
package com.bbn.c2s2.pint.pf.evaluators;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.pf.EvaluatedSolution;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

public class SolutionEvaluatorTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSolutionEvaluator() throws Exception {
		// testing the construction of the class
		SolutionEvaluator evaluator = new SolutionEvaluator();
		assertNotNull(evaluator);
	}

	public void testGetEvaluatedList() throws Exception {

		List<Solution> solutions = null;
		List<EvaluatedSolution> eSolutions = null;

		solutions = new ArrayList<Solution>();
		eSolutions = SolutionEvaluator.getEvaluatedList(solutions);
		assertNotNull(eSolutions);
		assertEquals(0, eSolutions.size());

		solutions.add(SolutionFactory
				.createOrderedColocatedSolution(TestRnrmProcessFactory
						.createSerialProcess()));
		solutions.add(SolutionFactory
				.createOrderedColocatedSolution(TestRnrmProcessFactory
						.createParallelProcess()));
		eSolutions = SolutionEvaluator.getEvaluatedList(solutions);
		assertEquals(2, eSolutions.size());

	}

	public void testEvaluate() throws Exception {

		Solution solution = null;
		RnrmProcess process = null;
		double scoreUnbound = -1;
		double scoreReverseColocated = -1;
		double scoreOrderedColocated = -1;
		double scoreReverseDistributed = -1;
		double scoreOrderedDistributed = -1;

		/*
		 * Empty process
		 */
		process = TestRnrmProcessFactory.createEmptyProcess();
		solution = SolutionFactory.createUnboundSolution(process);
		scoreUnbound = SolutionEvaluator.evaluate(solution);
		assertEquals(100.0, scoreUnbound);

		/*
		 * Single step process
		 */
		process = TestRnrmProcessFactory.createSingleActivityProcess();

		// unbound
		solution = SolutionFactory.createUnboundSolution(process);
		scoreUnbound = SolutionEvaluator.evaluate(solution);
		assertEquals(0.0, scoreUnbound);

		// ordered colocated
		solution = SolutionFactory.createOrderedColocatedSolution(process);
		scoreOrderedColocated = SolutionEvaluator.evaluate(solution);
		assertEquals(100.0, scoreOrderedColocated);

		/*
		 * Parallel Process
		 */
		process = TestRnrmProcessFactory.createParallelProcess();

		// unbound
		solution = SolutionFactory.createUnboundSolution(process);
		scoreUnbound = SolutionEvaluator.evaluate(solution);
		assertEquals(0.0, scoreUnbound);

		// ordered colocated
		solution = SolutionFactory.createOrderedColocatedSolution(process);
		scoreOrderedColocated = SolutionEvaluator.evaluate(solution);
		assertEquals(100.0, scoreOrderedColocated);

		// reverse colocated
		solution = SolutionFactory.createReverseColocatedSolution(process);
		scoreReverseColocated = SolutionEvaluator.evaluate(solution);
		assertEquals(100.0, scoreReverseColocated);

		// ordered distributed
		solution = SolutionFactory.createOrderedDistributedSolution(process);
		scoreOrderedDistributed = SolutionEvaluator.evaluate(solution);
		assertRange(0.0, 100.0, scoreOrderedDistributed,
				"Parallel ordered distributed");

		// reverse distributed
		solution = SolutionFactory.createReverseDistributedSolution(process);
		scoreReverseDistributed = SolutionEvaluator.evaluate(solution);
		assertRange(0.0, 100.0, scoreReverseDistributed,
				"Parallel reverse distributed");

		assertTrue(scoreOrderedColocated == scoreReverseColocated);
		assertTrue(scoreOrderedColocated > scoreOrderedDistributed);
		assertTrue(scoreReverseDistributed <= scoreReverseColocated);
		assertTrue(scoreReverseDistributed == scoreOrderedDistributed);

		/*
		 * Serial Process
		 */
		process = TestRnrmProcessFactory.createSerialProcess();
		testOrderSensitiveProcess(process, "Serial");

		/*
		 * Fork/Join Process
		 */
		process = TestRnrmProcessFactory.createForkJoinProcess();
		testOrderSensitiveProcess(process, "Fork/Join");

		/*
		 * Double Fork/Join Process
		 */
		process = TestRnrmProcessFactory.createDoubleForkJoinProcess();
		testOrderSensitiveProcess(process, "Double Fork/Join");
	}

	private static void testOrderSensitiveProcess(RnrmProcess process,
			String msg) {

		// unbound
		Solution solution = SolutionFactory.createUnboundSolution(process);
		double scoreUnbound = SolutionEvaluator.evaluate(solution);
		assertEquals(0.0, scoreUnbound);

		// ordered colocated
		solution = SolutionFactory.createOrderedColocatedSolution(process);
		double scoreOrderedColocated = SolutionEvaluator.evaluate(solution);
		assertEquals(100.0, scoreOrderedColocated);

		// reverse colocated
		solution = SolutionFactory.createReverseColocatedSolution(process);
		double scoreReverseColocated = SolutionEvaluator.evaluate(solution);
		assertRange(0.0, 100.0, scoreReverseColocated, msg
				+ " reverse colocated");

		// ordered distributed
		solution = SolutionFactory.createOrderedDistributedSolution(process);
		double scoreOrderedDistributed = SolutionEvaluator.evaluate(solution);
		assertRange(0.0, 100.0, scoreOrderedDistributed, msg
				+ " ordered distributed");

		// reverse distributed
		solution = SolutionFactory.createReverseDistributedSolution(process);
		double scoreReverseDistributed = SolutionEvaluator.evaluate(solution);
		assertRange(0.0, 100.0, scoreReverseDistributed, msg
				+ " reverse distributed");

		assertTrue(scoreOrderedColocated > scoreReverseColocated);
		assertTrue(scoreOrderedColocated > scoreOrderedDistributed);
		assertTrue(scoreReverseDistributed <= scoreReverseColocated);
		assertTrue(scoreReverseDistributed <= scoreOrderedDistributed);
	}

	public static void assertRange(double lb, double ub, double actual,
			String msg) {
		assertTrue("Upper bound for case: " + msg, actual <= ub);
		assertTrue("Lower bound for case: " + msg, actual >= lb);
	}
}
