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
import java.util.Date;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;

public class SimpleComCalculatorTest extends TestCase {

	// Going to use simpler values for testing. See below.
	// final double ERROR_ALLOWANCE = 0.01;
	// final double latCenter = 26.15;
	// final double lonCenter = 37.45;
	Collection<IObservation> solution;
	Collection<IObservation> emptySolution;
	Collection<IObservation> nonIntegerSolution;
	private int nextUriId = 0;

	private String randomUri() {
		return "http://foo#" + nextUriId++;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		/*
		 * You should use this method for doing initialization instead of making
		 * your own method like below. JUnit will call setUp() before it
		 * executes each test method. So if you have test1() and test2() in a
		 * file, JUnit will call: setUp(), test1(), setUp(), test2(). This is
		 * important because some tests involve changing the objects that you
		 * initialized. Calling setUp() each time returns them to the init state
		 * before calling the next test. JUnit times the unit tests, so you
		 * don't want initialization to be counted in the timings.
		 * 
		 * Eclipse will create this method for you. You need to check the
		 * setUp() box in the JUnit Test Case wizard when creating new tests.
		 */

		IObservation obs1 = new Observation(randomUri(), null, 10.0, 10.0, new Date(0));
		IObservation obs2 = new Observation(randomUri(), null, 10.0, 20.0, new Date(0));
		IObservation obs3 = new Observation(randomUri(), null, 0.0, 10.0, new Date(0));
		IObservation obs4 = new Observation(randomUri(), null, 0.0, 20.0, new Date(0));
		IObservation obs5 = new Observation(randomUri(), null, 5.0, 0.5, new Date(0));

		ArrayList<IObservation> sln = new ArrayList<IObservation>();
		sln.add(obs1);
		sln.add(obs2);
		sln.add(obs3);
		sln.add(obs4);
		solution = sln;
		
		nonIntegerSolution = new ArrayList<IObservation>();
		nonIntegerSolution.add(obs1);
		nonIntegerSolution.add(obs5);

		emptySolution = new ArrayList<IObservation>();
	}

	public void testGetCenter() {

		boolean exceptionFired = false;
		try {
			SimpleComCalculator.getCenter(null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		double[] center; // = new double[2];

		// Test the solution for a "real" case.
		center = SimpleComCalculator.getCenter(solution);
		assertEquals(5.0, center[0]);
		assertEquals(15.0, center[1]);

		// Test the solution for an empty case.
		center = SimpleComCalculator.getCenter(emptySolution);
		assertEquals(0.0, center[0]);
		assertEquals(0.0, center[1]);
		
		//test a non-integer case
		center = SimpleComCalculator.getCenter(nonIntegerSolution);
		assertEquals(7.5, center[0]); 
		assertEquals(5.25, center[1]);
	}

}
