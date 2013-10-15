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

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.RnrmProcess;

/**
 * Helper class with some utility methods for calculating various values
 * and violations.
 * @author reblace
 *
 */
public class PairwiseConsistencyMetrics {

	private static final double CONVERT_TO_HOURS = 1000 * 60 * 60;

	/**
	 * Calculates whether or not there is a partial order violation. 
	 * This checks to see if there are any occurrences of {@link Observation}
	 * objects that bind to {@link Activity} that are out of order with respect
	 * to the provided {@link RnrmProcess} and the timestamps of the bindings. 
	 * The order in which the {@link Binding} objects are passed is 
	 * not relevant to this calculation
	 * @param process The process to use as a reference for order
	 * @param bindingA One binding to check
	 * @param bindingB The other binding to check
	 * @return true if the bindings occur in order, false if not
	 */
	public static boolean partialOrderViolation(RnrmProcess process,
			Binding bindingA, Binding bindingB) {

		if (process == null) {
			throw new IllegalArgumentException("process cannot be null.");
		}
		if ((bindingA == null) || (bindingB == null)) {
			throw new IllegalArgumentException("Bindings cannot be null.");
		}

		Activity activityA = process.getActivityFromId(bindingA.getActivityID());
		Activity activityB = process.getActivityFromId(bindingB
				.getActivityID());

		IObservation observationA = bindingA.getObservation();
		IObservation observationB = bindingB.getObservation();

		if ((observationA == null) || (observationB == null)) {
			throw new IllegalArgumentException(
					"Bound observations cannot be null.");
		}

		Date dateA = observationA.getTimestamp();
		Date dateB = observationB.getTimestamp();

		// a2 is earlier than a1 in the order
		if (process.getHappensBefore(activityA).contains(activityB)) {
			int compareTo = dateA.compareTo(dateB);
			if (compareTo < 0) {
				return true;
			}
		} else if (process.getHappensBefore(activityB).contains(activityA)) {
			int compareTo = dateB.compareTo(dateA);
			if (compareTo < 0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean opposes(RnrmProcess process, Binding bindingA, Binding bindingB) {
		if (process.opposes(bindingA.getActivity(), bindingB.getActivity()))
			return true;
		else 
			return false;
	}

	/**
	 * Indicates whether the {@link Activity} objects to which the 
	 * {@link Binding} objects correspond are unordered with respect to the 
	 * {@link RnrmProcess}. The order in which the bindings are passed is not
	 * relevant to the calculation.
	 * @param process the RnrmProcess to use as a reference
	 * @param bindingA One binding
	 * @param bindingB Another binding
	 * @return true if the bindings are unordered
	 */
	public static boolean unordered(RnrmProcess process, Binding bindingA,
			Binding bindingB) {

		if (process == null) {
			throw new IllegalArgumentException("Process cannot be null.");
		}
		if ((bindingA == null) || (bindingB == null)) {
			throw new IllegalArgumentException("Binding cannot be null.");
		}

		Activity a1 = process.getActivityFromId(bindingA.getActivityID());
		Activity a2 = process.getActivityFromId(bindingB.getActivityID());
		if (process.getHappensBefore(a1).contains(a2)
				|| process.getHappensBefore(a2).contains(a1))
			return false;
		else
			return true;
	}

	/**
	 * Calculates the distance between two {@link Binding} objects
	 * @param bindingA one binding
	 * @param bindingB the other binding
	 * @return the distance in km
	 */
	public static double distance(IBinding bindingA, IBinding bindingB) {

		if ((bindingA == null) || (bindingB == null)) {
			throw new IllegalArgumentException("Bindings cannot be null.");
		}

		IObservation aObs = bindingA.getObservation();
		IObservation anotherObs = bindingB.getObservation();

		if ((aObs == null) || (anotherObs == null)) {
			throw new IllegalArgumentException(
					"Binding observations cannot be null.");
		}

		return DistanceCalculator.getDistance(aObs.getLat(), aObs.getLon(),
				anotherObs.getLat(), anotherObs.getLon());
	}

	/**
	 * Calculates the time difference between two binding timestamps in hours
	 * @param bindingA One binding
	 * @param bindingB The other binding
	 * @return The time difference between the two bindings in hours
	 */
	public static double timeDifference(IBinding bindingA, IBinding bindingB) {
		double diff = -1;

		if ((bindingA == null) || (bindingB == null)) {
			throw new IllegalArgumentException("Bindings cannot be null.");
		}

		IObservation aObs = bindingA.getObservation();
		IObservation anotherObs = bindingB.getObservation();

		if ((aObs == null) || (anotherObs == null)) {
			throw new IllegalArgumentException(
					"Binding observations cannot be null.");
		}

		diff = anotherObs.getTimestamp().getTime()
				- aObs.getTimestamp().getTime();
		diff = diff / CONVERT_TO_HOURS;

		return Math.abs(diff);
	}

}
