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

package com.bbn.c2s2.pint.pf.util;

import java.util.Date;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author krohloff
 * 
 */
public class RingBasedSpatialViolationCalculatorTest extends TestCase {

	private RnrmProcess process;
	private Solution solutionEmpty;
	private Solution solutionColocated;
	private Solution solutionDistributed;

	private IBinding bindingColocated;
	private IBinding bindingFar;

	final double smallDistance = 0.01;
	final double largeDistance = Double.MAX_VALUE;

	final double NASHVILLE_LAT = 36.12;
	final double NASHVILLE_LON = -86.67;
	private IBinding nullObsBinding;
	private int nextUriId = 0;

	private String randomUri() {
		return "http://foo#" + nextUriId++;
	}

	protected void setUp() throws Exception {
		super.setUp();
		process = TestRnrmProcessFactory.createSerialProcess();

		solutionEmpty = SolutionFactory.createUnboundSolution(process);
		solutionColocated = SolutionFactory
				.createOrderedColocatedSolution(process);
		solutionDistributed = SolutionFactory
				.createOrderedDistributedSolution(process);

		bindingColocated = solutionColocated.getOrderedBindings().get(0);
		bindingFar = new Binding(new Activity(0, randomUri()), new Observation(randomUri(),
				null, NASHVILLE_LAT, NASHVILLE_LAT, new Date(0)), 0);

		nullObsBinding = new Binding(new Activity(0, randomUri()), null, 0);

	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.RingBasedSpatialViolationCalculator#evaluateBinding(com.bbn.c2s2.pint.pf.Solution, com.bbn.c2s2.pint.Binding, double, double)}
	 * .
	 */
	public void testEvaluateBinding() {

		int eval;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			RingBasedSpatialViolationCalculator.evaluateBinding(null,
					bindingColocated, smallDistance, smallDistance);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			RingBasedSpatialViolationCalculator.evaluateBinding(solutionEmpty,
					null, smallDistance, smallDistance);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			RingBasedSpatialViolationCalculator.evaluateBinding(solutionEmpty,
					nullObsBinding, smallDistance, smallDistance);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}

		// Cases of interest:
		// Empty Solution - should return 0
		// SolutionFine before, binding outside;
		// SolutionFine before, binding inside;
		// SolutionBad before, binding outside;

		// Empty Solution
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionEmpty, bindingColocated, smallDistance, smallDistance);
		assertEquals(0, eval);

		// Solution fine before, binding also fine
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionColocated, bindingColocated, smallDistance,
				smallDistance);
		assertEquals(0, eval);

		// Solution fine before, binding outside
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionColocated, bindingFar, smallDistance, smallDistance);
		assertEquals(Integer.MAX_VALUE, eval);

		// Solution bad before
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionDistributed, bindingColocated, smallDistance,
				smallDistance);
		assertEquals(Integer.MAX_VALUE, eval);

		// Distance 0, fine before, binding good.
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionColocated, bindingColocated, 0.0, smallDistance);
		assertEquals(0, eval);

		// Distance 0, fine before, binding bad.
		eval = RingBasedSpatialViolationCalculator.evaluateBinding(
				solutionColocated, bindingFar, 0.0, smallDistance);
		assertEquals(Integer.MAX_VALUE, eval);

	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.RingBasedSpatialViolationCalculator#evaluateSolution(com.bbn.c2s2.pint.pf.Solution, double, double)}
	 * .
	 */
	public void testEvaluateSolution() {

		int eval;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			RingBasedSpatialViolationCalculator.evaluateSolution(null,
					smallDistance, smallDistance);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// Cases of interest:
		// Empty Solution - should return 0
		// Solution inside the max_radius - should return 0
		// Solution with any observation outside the max radius - should return
		// max_int.
		// distance_threshold 0 - should return 0 if all points collocated.
		// distance_threshold 0 - should return 0 if points not collocated.

		// empty solution
		eval = RingBasedSpatialViolationCalculator.evaluateSolution(
				solutionEmpty, smallDistance, smallDistance);
		assertEquals(0, eval);

		// solution inside ring
		eval = RingBasedSpatialViolationCalculator.evaluateSolution(
				solutionDistributed, largeDistance, smallDistance);
		assertEquals(0, eval);

		// solution outside ring
		eval = RingBasedSpatialViolationCalculator.evaluateSolution(
				solutionDistributed, smallDistance, smallDistance);
		assertEquals(Integer.MAX_VALUE, eval);

		// solution collocated, distance 0
		eval = RingBasedSpatialViolationCalculator.evaluateSolution(
				solutionColocated, 0.0, smallDistance);
		assertEquals(0, eval);

		// solution not collocated, distance 0.
		eval = RingBasedSpatialViolationCalculator.evaluateSolution(
				solutionDistributed, 0.0, smallDistance);
		assertEquals(Integer.MAX_VALUE, eval);
	}
}
