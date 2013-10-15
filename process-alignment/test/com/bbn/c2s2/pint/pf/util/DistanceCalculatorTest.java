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

import junit.framework.TestCase;

public class DistanceCalculatorTest extends TestCase {

	double[] nashville = new double[2];
	double[] lax = new double[2];

	/**
	 * known distance between LAX and Nashville airport
	 */
	final double ACTUAL_DISTANCE = 2886.45;
	final double DISTANCE_ERROR_ALLOWANCE = 5.0;
	final double REVERSE_ERROR_ALLOWANCE = 0.01;

	final double NASHVILLE_LAT = 36.12;
	final double NASHVILLE_LON = -86.67;
	final double LAX_LAT = 33.94;
	final double LAX_LON = -118.40;

	double[] negLon = new double[2];
	double[] bigLon = new double[2];
	double[] negLat = new double[2];
	double[] bigLat = new double[2];
	double[] smallCenter = new double[1];
	double[] bigCenter = new double[3];

	protected void setUp() throws Exception {
		super.setUp();
		nashville[0] = NASHVILLE_LAT;
		nashville[1] = NASHVILLE_LON;
		lax[0] = LAX_LAT;
		lax[1] = LAX_LON;

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
	}

	public void testGetDistance() {

		boolean exceptionFired = false;

		exceptionFired = false;
		try {
			DistanceCalculator
					.getDistance(negLat[0], negLat[1], lax[0], lax[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			DistanceCalculator
					.getDistance(lax[0], lax[1], negLat[0], negLat[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);
		try {
			DistanceCalculator
					.getDistance(negLon[0], negLon[1], lax[0], lax[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			DistanceCalculator
					.getDistance(lax[0], lax[1], negLon[0], negLon[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);
		try {
			DistanceCalculator
					.getDistance(bigLat[0], bigLat[1], lax[0], lax[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			DistanceCalculator
					.getDistance(lax[0], lax[1], bigLat[0], bigLat[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);
		try {
			DistanceCalculator
					.getDistance(bigLon[0], bigLon[1], lax[0], lax[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		exceptionFired = false;
		try {
			DistanceCalculator
					.getDistance(lax[0], lax[1], bigLon[0], bigLon[1]);
		} catch (IllegalArgumentException e) {
			exceptionFired = true;
		}
		assertTrue("Test Exception", exceptionFired);

		// check the distance
		double distance = DistanceCalculator.getDistance(nashville[0],
				nashville[1], lax[0], lax[1]);
		assertTrue(Math.abs(ACTUAL_DISTANCE - distance) < DISTANCE_ERROR_ALLOWANCE);

		// check the other way as well
		double dist2 = DistanceCalculator.getDistance(lax[0], lax[1],
				nashville[0], nashville[1]);
		assertTrue(Math.abs(distance - dist2) < REVERSE_ERROR_ALLOWANCE);

		// check the 0 distance
		double dist0 = DistanceCalculator.getDistance(lax[0], lax[1], lax[0],
				lax[1]);
		assertTrue(Math.abs(dist0) < REVERSE_ERROR_ALLOWANCE);
	}

}
