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

import java.util.Collection;

import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;

/**
 * Calculates the center of a set of points using a simple average.
 * @author reblace
 *
 */
public class SimpleComCalculator {

	/**
	 * Calculates the center of the set of {@link Observation} objects
	 * in the solution using the average of the lats and longs
	 * @param solution The solution for which to calculate the center
	 * @return The center point [lat,lon]
	 */
	public static double[] getCenter(Collection<IObservation> solution) {
		
		if(solution == null){
			throw new IllegalArgumentException("Observations cannot be null.");
		}
		
		double initLat = 0.0;
		double initLon = 0.0;
		double[] center = new double[] { 0.0, 0.0 };
		for (IObservation obs : solution) {
			initLat += obs.getLat();
			initLon += obs.getLon();
		}
		if (solution.size() > 0) {
			center[0] = initLat / solution.size();
			center[1] = initLon / solution.size();
		}
		return center;
	}
}
