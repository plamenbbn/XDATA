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

/**
 * Simple Greatest Circle Distance calculator
 * @author reblace
 *
 */
public class DistanceCalculator {

	/**
	 * Caculates the distance between two points using the common greatest
	 * circle distance formula. Return values are in km.
	 * @param lat1 Lat of the first point
	 * @param lon1 Long of the first point
	 * @param lat2 Lat of the secont point
	 * @param lon2 Long of the second point
	 * @return The distance between the two points in KM
	 */
	public static double getDistance(double lat1, double lon1, double lat2,
			double lon2) {

		validateLatLon(lat1, lon1);
		validateLatLon(lat2, lon2);
		
		final int MIN_PER_DEGREE = 60;
		final double KM_PER_NAUTICAL_MILE = 1.852;

		double L1 = Math.toRadians(lat1);
		double L2 = Math.toRadians(lat2);
		double G1 = Math.toRadians(lon1);
		double G2 = Math.toRadians(lon2);

		double a = Math.pow(Math.sin((L2 - L1) / 2), 2) + Math.cos(L1)
				* Math.cos(L2) * Math.pow(Math.sin((G2 - G1) / 2), 2);

		// great circle distance in radians
		double angle2 = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
		// convert back to degrees
		angle2 = Math.toDegrees(angle2);
		// each degree on a great circle of Earth is 60 nautical miles
		double nautMiles = MIN_PER_DEGREE * angle2;

		// return in kilometers
		return KM_PER_NAUTICAL_MILE * nautMiles;
	}
	
	/**
	 * Validates the latitude and longitude provided. Checks their bounds.
	 * @param lat The latitude to check
	 * @param lon The longitude to check
	 */
	public static void validateLatLon(double lat, double lon){
		if ((lat > 90.0)||(lat < -90.0)) {
			throw new IllegalArgumentException( lat + 
					" is an invalid latitude. Must be in the range [-90, 90].");
		}
		if ((lon > 180.0)||(lon < -180.0)) {
			throw new IllegalArgumentException( lon + 
					" is an invalid longitude. Must be in the range [-180, 180].");
		}
	}
}
