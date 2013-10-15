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

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;

/**
 * Calculates a violation score for a binding given a center point using a 
 * continuous function defined as 1/(1 + distance/lamda) where lamda is a 
 * constant that specifies the shape of the decay function.
 * 
 * @author reblace
 *
 */
public class ContinuousSpatialViolationCalculator {

	private final static double DISTANCE_LAMDA = 40.0;

	/**
	 * Evalute the {@link Binding}'s position relative to the center point
	 * @param center The center point to which to compare the binding's location. 
	 * Is expected to be [latitude][longitude]
	 * @param binding The binding to evalute
	 * @return The score of the binding with respect to the center point
	 */
	public static double evaluateBinding(double[] center, IBinding binding) {
		// check that this binding is to a valid activity
		
		if(null == center){
			throw new IllegalArgumentException("Center point cannot be null.");
		}
		if (center.length != 2) {
			throw new IllegalArgumentException(
					"Center must be length 2, it is length: " + center.length);
		}
		
		DistanceCalculator.validateLatLon(center[0], center[1]);
		
		if (binding == null) {
			throw new IllegalArgumentException("Binding cannot be null");
		}
		if (binding.getObservation() == null) {
			throw new IllegalArgumentException(
					"Binding observation cannot be null.");
		}
		
		IObservation obs = binding.getObservation();
		double dist = DistanceCalculator.getDistance(center[0], center[1], obs
				.getLat(), obs.getLon());
		return distanceQuality(dist, DISTANCE_LAMDA);
	}

	/**
	 * Calculates the quality of the distance calculation
	 * @param dist The distance to measure the quality of
	 * @param lambda The quality decay function
	 * @return the quality score of the distance
	 */
	private static double distanceQuality(double dist, double lamda) {
		if (dist < 0) {
			throw new IllegalArgumentException(dist + 
					" is an invalid distance, cannot be negative.");
		}
		if (lamda < 0) {
			throw new IllegalArgumentException(lamda + 
					" is an invalid lamda, cannot be negative.");
		}

		if (Math.abs(lamda) < 0.00001) {
			return 1.0;
		}
		return 1.0 / (1.0 + dist / lamda);
	}

}
