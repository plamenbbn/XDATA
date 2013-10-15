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

import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.Solution;

/**
 * Calculates violations by returning the maximum score when there are any
 * {@link Observation} objects whose center is further from the center of 
 * all the observations than the upper bound for radial distance in km's
 * @author reblace
 *
 */
public class RingBasedSpatialViolationCalculator {

	/**
	 * Calculates the violation score for the {@link Solution} by checking to 
	 * see if any of the {@link Observation} objects in the solution 
	 * are outside of the upper bound for radial distance in km
	 * @param solution The solution for which to evaluate the binding
	 * @param binding The binding to evaluate
	 * @param max_rad_km the upper bound for radial distance in km
	 * @param ideal_rad_km the lower bound for radial distance in km
	 * @return
	 */
	public static int evaluateBinding(Solution solution,
			IBinding binding, double max_rad_km, double ideal_rad_km) {

		if (null == solution) {
			throw new IllegalArgumentException("Solution cannot be null.");
		}

		if (null == binding) {
			throw new IllegalArgumentException("Binding cannot be null.");
		}

		IObservation obs = binding.getObservation();

		if (null == obs) {
			throw new IllegalArgumentException(
					"Binding observation cannot be null.");
		}

		Collection<IObservation> observations = solution
				.getObservations();
		observations.add(obs);
		return evaluateSolution(observations, max_rad_km, ideal_rad_km);
	}

	/**
	 * Evaluate a solution given the radial distance bounds. This actually
	 * checks the {@link Observation} collection to see if any individual
	 * observation is further from the center of all observations than the
	 * upper bound for radial distance
	 * @param solution The solution to evaluate
	 * @param max_rad_km the upper bound for radial distance in km
	 * @param ideal_rad_km the lower bound for radial distance in km
	 * @return The score of the solution
	 */
	public static int evaluateSolution(Solution solution,
			double max_rad_km, double ideal_rad_km) {

		if (solution == null) {
			throw new IllegalArgumentException("Solution cannot be null.");
		}

		Collection<IObservation> observations = solution
				.getObservations();
		return evaluateSolution(observations, max_rad_km, ideal_rad_km);
	}

	/**
	 * Evaluate the collection of {@link Observation} objects. If any in the set
	 * have a distance from the group's center point that is greater than the 
	 * upper bound for radial distance, the score will be its maximum value.
	 * If none have that condition, the score is 0. 
	 * @param observations The collection of observations to evaluate
	 * @param max_rad_km The upper bound for radial distance in km
	 * @param ideal_rad_km The lower bound for radial distance in km
	 * @return The score for the set of observations;
	 */
	private static int evaluateSolution(Collection<IObservation> observations,
			double max_rad_km, double ideal_rad_km){

		if (observations == null) {
			throw new IllegalArgumentException("Observation set cannot be null.");
		}

		double[] center = SimpleComCalculator.getCenter(observations);
		for (IObservation obs : observations) {
			if (max_rad_km < DistanceCalculator.getDistance(center[0],
					center[1], obs.getLat(), obs.getLon())) {
				return Integer.MAX_VALUE;
			}
		}
		return 0;
	}

}
