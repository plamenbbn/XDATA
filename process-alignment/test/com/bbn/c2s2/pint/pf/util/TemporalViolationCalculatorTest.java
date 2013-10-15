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

public class TemporalViolationCalculatorTest extends TestCase {

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

		solutionNull = null;
		bindingsNull = null;

		solutionUnbound = SolutionFactory.createUnboundSolution(process);
		bindingsUnbound = solutionUnbound.getNonNullBindings();

		binding = new Binding(new Activity(0, randomUri()));
	}

	public void testTemporalViolationsSolutionBinding() {

		int violations;
		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.temporalViolations(null,
					binding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.temporalViolations(
					solutionUnbound, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// // no violations for ordered solution with no binding
		// violations = TemporalViolationCalculator.temporalViolations(
		// solutionOrdered, null);
		// assertEquals(0, violations);
		//
		// // no violations for a null solution
		// violations = TemporalViolationCalculator.temporalViolations(
		// solutionNull, null);
		// assertEquals(0, violations);

		// No violations for an unbound solution
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.temporalViolations(
					solutionUnbound, binding);
			assertEquals(0, violations);
		}

		// No violations for an order solution
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.temporalViolations(
					solutionOrdered, binding);
			assertEquals(0, violations);
		}

		// For a reverse solution, test three cases of a head, tail and middle.
		ArrayList<Binding> bindingList = new ArrayList<Binding>(bindingsReverse);
		violations = TemporalViolationCalculator.temporalViolations(
				solutionReverse, bindingList.get(0));
		// System.out.println(violations);
		assertEquals(2, violations);

		violations = TemporalViolationCalculator.temporalViolations(
				solutionReverse, bindingList.get(1));
		// System.out.println(violations);
		assertEquals(1, violations);

		violations = TemporalViolationCalculator.temporalViolations(
				solutionReverse, bindingList.get(2));
		// System.out.println(violations);
		assertEquals(0, violations);
	}

	public void testTemporalViolationsSolution() {

		int violations;

		boolean exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.temporalViolations(null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// // Test null solution
		// int violations = TemporalViolationCalculator
		// .temporalViolations(solutionNull);
		// assertEquals(0, violations);

		// test unbound solution
		violations = TemporalViolationCalculator
				.temporalViolations(solutionUnbound);
		assertEquals(0, violations);

		// test order solution
		violations = TemporalViolationCalculator
				.temporalViolations(solutionOrdered);
		assertEquals(0, violations);

		// test reverse solution
		violations = TemporalViolationCalculator
				.temporalViolations(solutionReverse);
		assertEquals(3, violations);
	}

	public void testAfterViolations() {

		int violations;
		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.afterViolations(null,
					binding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.afterViolations(
					solutionUnbound, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// // Test null binding
		// int violations = TemporalViolationCalculator.afterViolations(
		// solutionOrdered, null);
		// assertEquals(0, violations);
		//
		// // Test null solution
		// violations =
		// TemporalViolationCalculator.afterViolations(solutionNull,
		// null);
		// assertEquals(0, violations);

		// Test unbound solution
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.afterViolations(
					solutionUnbound, binding);
			assertEquals(0, violations);
		}

		// Test front, end and middle of ordered solution
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.afterViolations(
					solutionOrdered, binding);
			// System.out.println(violations);
			assertEquals(0, violations);
		}

		// Should be 2,1,0
		// Test front, end and middle of revered solution.
		ArrayList<Binding> bindingReverseList = new ArrayList<Binding>(
				bindingsReverse);

		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingReverseList.get(0));
		// System.out.println(violations);
		assertEquals(2, violations);

		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingReverseList.get(1));
		// System.out.println(violations);
		assertEquals(1, violations);

		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingReverseList.get(2));
		// System.out.println(violations);
		assertEquals(0, violations);
	}

	public void testBeforeViolations() {

		int violations;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.beforeViolations(null,
					binding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violations = TemporalViolationCalculator.beforeViolations(
					solutionUnbound, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// // Test a null binding
		// int violations = TemporalViolationCalculator.beforeViolations(
		// solutionOrdered, null);
		// assertEquals(0, violations);
		//
		// // Test a null solution
		// violations =
		// TemporalViolationCalculator.beforeViolations(solutionNull,
		// null);
		// assertEquals(0, violations);

		// test an unbound solution
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.beforeViolations(
					solutionUnbound, binding);
			assertEquals(0, violations);
		}

		// test an ordered solution - front, end and middle
		for (Binding binding : bindingsOrdered) {
			violations = TemporalViolationCalculator.beforeViolations(
					solutionOrdered, binding);
			// System.out.println(violations);
			assertEquals(0, violations);
		}

		ArrayList<Binding> bindingList = new ArrayList<Binding>(bindingsReverse);

		// test a reverse solution - front, end and middle.
		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingList.get(0));
		// System.out.println(violations);
		assertEquals(2, violations);

		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingList.get(1));
		// System.out.println(violations);
		assertEquals(1, violations);

		violations = TemporalViolationCalculator.afterViolations(
				solutionReverse, bindingList.get(2));
		// System.out.println(violations);
		assertEquals(0, violations);
	}

}
