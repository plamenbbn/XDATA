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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.testdata.BindingGroupFactory;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author reblace
 * 
 */
public class PairwiseConsistencyMetricsTest extends TestCase {

	List<Binding> bindings = null;
	List<Binding> bSame = null;
	List<Binding> bReverse = null;
	PintConfiguration config = null;
	RnrmProcess processSerial = null;
	RnrmProcess processParallel = null;

	IBinding nashvilleEarly;
	IBinding nashvilleLate;
	IBinding laxEarly;
	IBinding laxLate;

	Activity nullActivity;

	final double ACTUAL_DISTANCE = 2886.45;
	final double DISTANCE_ERROR_ALLOWANCE = 5.0;
	final double REVERSE_ERROR_ALLOWANCE = 0.01;

	final double ACTUAL_DIFFERENCE = 1;
	final double DIFFERENCE_ERROR_ALLOWANCE = 0.01;

	final double NASHVILLE_LAT = 36.12;
	final double NASHVILLE_LON = -86.67;
	final double LAX_LAT = 33.94;
	final double LAX_LON = -118.40;
	private Binding nullObsBinding;
	private ArrayList<Binding> bindingListSerial;
	private ArrayList<Binding> bindingListParallel;
	private int nextUriId = 0;

	private String randomUri() {
		return "http://foo#" + nextUriId++;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BindingGroupFactory factory = new BindingGroupFactory();
		bindings = factory.generateBindings(0.0, 1000);
		bSame = factory.generateBindings(0.0, 0);
		bReverse = factory.generateBindings(0.0, -1000);
		processSerial = TestRnrmProcessFactory.createSerialProcess(3);
		processParallel = TestRnrmProcessFactory.createParallelProcess();

		nullActivity = new Activity(0, "http://null");

		Date early = new Date(0);
		Date late = new Date(3600000); // one hour.

		Observation nashObsEarly = new Observation(randomUri(), null, NASHVILLE_LAT,
				NASHVILLE_LON, early);
		Observation nashObsLate = new Observation(randomUri(), null, NASHVILLE_LAT,
				NASHVILLE_LON, late);
		Observation laxObsEarly = new Observation(randomUri(), null, LAX_LAT, LAX_LON,
				early);
		Observation laxObsLate = new Observation(randomUri(), null, LAX_LAT, LAX_LON,
				late);

		nashvilleEarly = new Binding(nullActivity, nashObsEarly, 0);
		nashvilleLate = new Binding(nullActivity, nashObsLate, 0);
		laxEarly = new Binding(nullActivity, laxObsEarly, 0);
		laxLate = new Binding(nullActivity, laxObsLate, 0);

		nullObsBinding = new Binding(new Activity(0, randomUri()), null, 0);

		Solution solutionSerial = SolutionFactory
				.createOrderedColocatedSolution(processSerial);
		Collection<Binding> bindingsSerial = solutionSerial
				.getNonNullBindings();
		bindingListSerial = new ArrayList<Binding>(bindingsSerial);

		Solution solutionParallel = SolutionFactory
				.createOrderedColocatedSolution(processParallel);
		Collection<Binding> bindingsParallel = solutionParallel
				.getNonNullBindings();
		bindingListParallel = new ArrayList<Binding>(bindingsParallel);

	}

	public void testPartialOrderViolation() {
		boolean violation = false;
		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			violation = PairwiseConsistencyMetrics.partialOrderViolation(null,
					bindings.get(1), bindings.get(4));
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violation = PairwiseConsistencyMetrics.partialOrderViolation(
					processSerial, null, bindings.get(4));
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violation = PairwiseConsistencyMetrics.partialOrderViolation(
					processSerial, bindings.get(1), null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violation = PairwiseConsistencyMetrics.partialOrderViolation(
					processSerial, nullObsBinding, bindings.get(1));
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			violation = PairwiseConsistencyMetrics.partialOrderViolation(
					processSerial, bindings.get(1), nullObsBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// in order activities
		violation = PairwiseConsistencyMetrics.partialOrderViolation(
				processSerial, bindings.get(1), bindings.get(4));
		assertFalse(violation);

		// out of order activities
		violation = PairwiseConsistencyMetrics.partialOrderViolation(
				processSerial, bReverse.get(1), bReverse.get(4));
		assertTrue(violation);

		// concurrent
		violation = PairwiseConsistencyMetrics.partialOrderViolation(
				processSerial, bindings.get(1), bindings.get(4));
		assertFalse(violation);

		violation = PairwiseConsistencyMetrics.partialOrderViolation(
				processSerial, bindings.get(5), bindings.get(2));
		assertFalse(violation);
	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.PairwiseConsistencyMetrics#unordered(com.bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)}
	 * .
	 */
	public void testUnordered() {
		// fail("Not yet implemented");
		// Test cases
		// One activity before the other and reverse.
		// One doesn't happen before the other and reverse.

		boolean unordered;
		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			unordered = PairwiseConsistencyMetrics.unordered(null,
					bindingListSerial.get(0), bindingListSerial.get(1));
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);
		exceptionFired = false;
		try {
			unordered = PairwiseConsistencyMetrics.unordered(processSerial,
					null, bindingListSerial.get(1));
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			unordered = PairwiseConsistencyMetrics.unordered(processSerial,
					bindingListSerial.get(0), null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// test two serial bindings
		unordered = PairwiseConsistencyMetrics.unordered(processSerial,
				bindingListSerial.get(0), bindingListSerial.get(1));
		assertFalse(unordered);

		// reverse order of bindings
		unordered = PairwiseConsistencyMetrics.unordered(processSerial,
				bindingListSerial.get(1), bindingListSerial.get(0));
		assertFalse(unordered);

		// test binding with itself
		unordered = PairwiseConsistencyMetrics.unordered(processSerial,
				bindingListSerial.get(0), bindingListSerial.get(0));
		assertTrue(unordered);

		// test two serial bindings
		unordered = PairwiseConsistencyMetrics.unordered(processParallel,
				bindingListParallel.get(0), bindingListParallel.get(1));
		assertTrue(unordered);

		// reverse order of bindings
		unordered = PairwiseConsistencyMetrics.unordered(processParallel,
				bindingListParallel.get(1), bindingListParallel.get(2));
		assertTrue(unordered);

	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.PairwiseConsistencyMetrics#distance(com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)}
	 * .
	 */
	public void testDistance() {

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.distance(null, laxEarly);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.distance(laxEarly, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.distance(nullObsBinding, laxEarly);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.distance(laxEarly, nullObsBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// check the distance
		double distance = PairwiseConsistencyMetrics.distance(nashvilleEarly,
				laxEarly);
		assertTrue(Math.abs(ACTUAL_DISTANCE - distance) < DISTANCE_ERROR_ALLOWANCE);

		// check the other way as well
		double dist2 = PairwiseConsistencyMetrics.distance(laxEarly,
				nashvilleEarly);
		assertTrue(Math.abs(distance - dist2) < REVERSE_ERROR_ALLOWANCE);

		// check the 0 distance
		double dist0 = PairwiseConsistencyMetrics.distance(laxEarly, laxEarly);
		assertTrue(Math.abs(dist0) < REVERSE_ERROR_ALLOWANCE);
	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.PairwiseConsistencyMetrics#timeDifference(com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)}
	 * .
	 */
	public void testTimeDifference() {
		BindingGroupFactory factory = new BindingGroupFactory();

		// now
		Calendar now = Calendar.getInstance();
		IBinding a1 = factory.createBinding(0, 0, 0, 0, 0, now.getTime());

		// add an hour
		now.setTimeInMillis(now.getTimeInMillis() + 60 * 60 * 1000);
		IBinding a2 = factory.createBinding(0, 0, 0, 0, 0, now.getTime());

		// add another 4 hours (5 total)
		now.setTimeInMillis(now.getTimeInMillis() + 4 * 60 * 60 * 1000);
		IBinding a3 = factory.createBinding(0, 0, 0, 0, 0, now.getTime());

		// add another half hour (5.5 total)
		now.setTimeInMillis(now.getTimeInMillis() + 30 * 60 * 1000);
		IBinding a4 = factory.createBinding(0, 0, 0, 0, 0, now.getTime());

		assertEquals(1.0, PairwiseConsistencyMetrics.timeDifference(a1, a2));
		assertEquals(1.0, PairwiseConsistencyMetrics.timeDifference(a2, a1));
		assertEquals(5.0, PairwiseConsistencyMetrics.timeDifference(a1, a3));
		assertEquals(5.0, PairwiseConsistencyMetrics.timeDifference(a3, a1));
		assertEquals(4.0, PairwiseConsistencyMetrics.timeDifference(a2, a3));
		assertEquals(4.0, PairwiseConsistencyMetrics.timeDifference(a3, a2));

		assertEquals(0.5, PairwiseConsistencyMetrics.timeDifference(a3, a4));
		assertEquals(0.5, PairwiseConsistencyMetrics.timeDifference(a4, a3));

		// check the distance
		double difference;

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.timeDifference(null, laxEarly);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.timeDifference(laxEarly, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.timeDifference(nullObsBinding, laxEarly);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			PairwiseConsistencyMetrics.timeDifference(laxEarly, nullObsBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		difference = PairwiseConsistencyMetrics.timeDifference(laxLate,
				laxEarly);
		// System.out.println(difference);
		assertTrue(Math.abs(ACTUAL_DIFFERENCE - difference) < DIFFERENCE_ERROR_ALLOWANCE);

		// check the other way as well
		difference = PairwiseConsistencyMetrics.timeDifference(laxEarly,
				laxLate);
		// System.out.println(difference);
		assertTrue(Math.abs(ACTUAL_DIFFERENCE - difference) < DIFFERENCE_ERROR_ALLOWANCE);

		// check the 0 distance
		difference = PairwiseConsistencyMetrics.timeDifference(laxEarly,
				laxEarly);
		// System.out.println(difference);
		assertTrue(Math.abs(difference) < DIFFERENCE_ERROR_ALLOWANCE);

	}

}
