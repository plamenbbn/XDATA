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

/**
 * @author krohloff
 * 
 */
public class ContinuousSpatialViolationCalculatorTest extends TestCase {

	double[] nashville = new double[2];
	double[] lax = new double[2];
	double[] zero = new double[2];

	double[] negLon = new double[2];
	double[] bigLon = new double[2];
	double[] negLat = new double[2];
	double[] bigLat = new double[2];
	double[] smallCenter = new double[1];
	double[] bigCenter = new double[3];

	IBinding nashBinding;
	IBinding laxBinding;
	IBinding nullBinding;

	double lax_nash_quality;

	/**
	 * known distance between LAX and Nashville airport
	 */

	final double ERROR_ALLOWANCE = 0.01;
	final double ACTUAL_DISTANCE = 2886.45;
	final static double _distance_lambda = 40.0;

	final double NASHVILLE_LAT = 36.12;
	final double NASHVILLE_LON = -86.67;
	final double LAX_LAT = 33.94;
	final double LAX_LON = -118.40;

	protected void setUp() throws Exception {
		super.setUp();

		nashville[0] = NASHVILLE_LAT;
		nashville[1] = NASHVILLE_LON;

		lax[0] = LAX_LAT;
		lax[1] = LAX_LON;

		zero[0] = 0.0;
		zero[1] = 0.0;

		smallCenter[0] = 0.0;

		bigCenter[0] = 0.0;
		bigCenter[1] = 0.0;
		bigCenter[2] = 0.0;

		negLat[0] = -91;
		negLat[1] = LAX_LON;

		bigLat[0] = 91;
		bigLat[1] = LAX_LON;

		negLon[0] = LAX_LAT;
		negLon[1] = -181.0;

		bigLon[0] = LAX_LAT;
		bigLon[1] = 181.0;

		lax_nash_quality = 1.0 / (1.0 + ACTUAL_DISTANCE / _distance_lambda);

		// Can't have null activity in binding.

		Activity act = new Activity(0, "http://act");

		nashBinding = new Binding(act, new Observation("http://nash", null,
				NASHVILLE_LAT, NASHVILLE_LON, new Date(0)), 0);
		laxBinding = new Binding(act, new Observation("http://lax", null, LAX_LAT,
				LAX_LON, new Date(0)), 0);
		nullBinding = new Binding(act, null, 0);
	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.util.ContinuousSpatialViolationCalculator#evaluateBinding(double[], com.bbn.c2s2.pint.Binding)}
	 * .
	 */
	public void testEvaluateBinding() {

		// tests of interest:
		// dist is 0 then distance quality is 1,

		double quality;

		boolean exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(smallCenter,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(bigCenter,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(negLat,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(bigLat,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(negLon,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(bigLon,
					laxBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(lax, null);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			ContinuousSpatialViolationCalculator.evaluateBinding(lax,
					nullBinding);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// Make sure distance of zero is evaluated to be of value 1.0;
		quality = ContinuousSpatialViolationCalculator.evaluateBinding(lax,
				laxBinding);
		// System.out.println(quality);
		assertEquals(1.0, quality);

		// Make sure that lax, nashville have correct quality
		quality = ContinuousSpatialViolationCalculator.evaluateBinding(lax,
				nashBinding);
		// System.out.println(quality);
		assertTrue(Math.abs(quality - lax_nash_quality) < ERROR_ALLOWANCE);

	}

}
