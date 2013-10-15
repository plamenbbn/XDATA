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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

public class ViolationRatiosTest extends TestCase {

	final double ERROR_ALLOWANCE = 0.01;

	private RnrmProcess process;
	Solution solutionOrdered;
	Solution solutionReverse;
	Solution solutionNull;
	Solution solutionUnbound;

	Collection<Binding> bindingsOrdered;
	Collection<Binding> bindingsReverse;
	Collection<Binding> bindingsNull;
	Collection<Binding> bindingsUnbound;

	private Binding binding;
	private int nextUriId = 0;

	private String randomUri() {
		return "http://foo#" + nextUriId++;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		process = TestRnrmProcessFactory.createSerialProcess();

		solutionOrdered = SolutionFactory
				.createOrderedColocatedSolution(process);
		bindingsOrdered = solutionOrdered.getNonNullBindings();

		solutionReverse = SolutionFactory
				.createReverseColocatedSolution(process);
		bindingsReverse = solutionOrdered.getNonNullBindings();

		solutionUnbound = SolutionFactory.createUnboundSolution(process);
		bindingsUnbound = solutionUnbound.getNonNullBindings();
		binding = new Binding(new Activity(0, randomUri()));
	}

	public void testEvaluateTemporalValidRatio() {

		double violationRatio;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			ViolationRatios.evaluateTemporalValidRatio(null, binding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ViolationRatios.evaluateTemporalValidRatio(solutionUnbound, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// violationRatio = ViolationRatios.evaluateTemporalValidRatio(
		// solutionOrdered, null);
		// // System.out.println(violationRatio);
		// assertEquals(1.0, violationRatio);
		//
		// violationRatio = ViolationRatios.evaluateTemporalValidRatio(
		// solutionNull, null);
		// // System.out.println(violationRatio);
		// assertEquals(1.0, violationRatio);

		// System.out.println("--------------------");
		for (Binding binding : bindingsOrdered) {
			violationRatio = ViolationRatios.evaluateTemporalValidRatio(
					solutionUnbound, binding);
			// System.out.println(violationRatio);
			assertEquals(1.0, violationRatio);
		}

		// System.out.println("--------------------");
		for (Binding binding : bindingsOrdered) {
			violationRatio = ViolationRatios.evaluateTemporalValidRatio(
					solutionOrdered, binding);
			// System.out.println(violationRatio);
			assertEquals(1.0, violationRatio);
		}

		ArrayList<Binding> bindingList = new ArrayList<Binding>(bindingsReverse);
		violationRatio = ViolationRatios.evaluateTemporalValidRatio(
				solutionReverse, bindingList.get(0));
		// System.out.println(violations);
		assertEquals(0.0, violationRatio);

		violationRatio = ViolationRatios.evaluateTemporalValidRatio(
				solutionReverse, bindingList.get(1));
		// System.out.println(violations);
		assertEquals(0.5, violationRatio);

		violationRatio = ViolationRatios.evaluateTemporalValidRatio(
				solutionReverse, bindingList.get(2));
		// System.out.println(violations);
		assertEquals(1.0, violationRatio);

	}

	public void testTemporalValidRatio() {

		double violationRatio;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			ViolationRatios.temporalValidRatio(null, binding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ViolationRatios.temporalValidRatio(solutionUnbound, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);
		// violationRatio = ViolationRatios.temporalValidRatio(solutionOrdered,
		// null);
		// // System.out.println(violationRatio);
		// assertEquals(1.0, violationRatio);
		//
		// violationRatio = ViolationRatios.temporalValidRatio(solutionNull,
		// null);
		// // System.out.println(violationRatio);
		// assertEquals(1.0, violationRatio);

		// System.out.println("--------------------");
		for (Binding binding : bindingsOrdered) {
			violationRatio = ViolationRatios.temporalValidRatio(
					solutionUnbound, binding);
			// System.out.println(violationRatio);
			assertEquals(1.0, violationRatio);
		}

		// System.out.println("--------------------");
		for (Binding binding : bindingsOrdered) {
			violationRatio = ViolationRatios.temporalValidRatio(
					solutionOrdered, binding);
			// System.out.println(violationRatio);
			assertEquals(1.0, violationRatio);
		}

		ArrayList<Binding> bindingList = new ArrayList<Binding>(bindingsReverse);
		violationRatio = ViolationRatios.temporalValidRatio(solutionReverse,
				bindingList.get(0));
		// System.out.println(violations);
		assertEquals(0.0, violationRatio);

		violationRatio = ViolationRatios.temporalValidRatio(solutionReverse,
				bindingList.get(1));
		// System.out.println(violations);
		assertEquals(0.5, violationRatio);

		violationRatio = ViolationRatios.temporalValidRatio(solutionReverse,
				bindingList.get(2));
		// System.out.println(violations);
		assertEquals(1.0, violationRatio);
	}

}
