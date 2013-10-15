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
package com.bbn.c2s2.pint.testdata;

import java.util.Calendar;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;

public class SolutionFactory {

	public static final String OBSERVATION_URI = "http://c2s2.bbn.com/test#observation";
	public static final String OBSERVABLE_URI = "http://c2s2.bbn.com/test#observable";

	public static Solution createUnboundSolution(RnrmProcess process) {
		return new Solution(process);
	}

	public static Solution createReverseColocatedSolution(RnrmProcess process) {
		return createSolution(process, -100, 0);
	}

	public static Solution createOrderedColocatedSolution(RnrmProcess process) {
		return createSolution(process, 100, 0);
	}

	public static Solution createReverseDistributedSolution(RnrmProcess process) {
		return createSolution(process, -100, 0.01);
	}

	public static Solution createOrderedDistributedSolution(RnrmProcess process) {
		return createSolution(process, 100, 0.01);
	}

	/**
	 * Creates a correct solution with one Activity left unbound.
	 * 
	 * @param process
	 *            RnrmProcess to base the solution on.
	 * @param itemToSkip
	 *            0-based index to skip. -1 means bind all. Indexes out of range
	 *            will be ignored and everything will be bound.
	 * @return
	 */
	public static Solution createOrderedSolutionWithOneUnbound(
			RnrmProcess process, int itemToSkip) {
		return createSolution(process, 100, 0, itemToSkip);
	}

	private static Solution createSolution(RnrmProcess process,
			long temporalDirection, double spatialVariance) {
		return createSolution(process, temporalDirection, spatialVariance, -1);
	}

	/**
	 * Creates a Solution using the given characteristics.
	 * 
	 * @param process
	 * @param temporalDirection
	 * @param spatialVariance
	 * @param itemToSkip
	 *            Skips the i'th Activity (0-based) when binding. -1 means bind
	 *            all. Indexes out of range will be ignored and everything will
	 *            be bound.
	 * @return
	 */
	private static Solution createSolution(RnrmProcess process,
			long temporalDirection, double spatialVariance, int itemToSkip) {
		int id = 0;
		Solution solution = new Solution(process);

		long timeMillis = 0;
		Calendar calendar = Calendar.getInstance();
		double lat = 0, lon = 0;
		for (Activity a : process.getOrderedActivities()) {

			timeMillis += temporalDirection;
			lat += spatialVariance;
			lon += spatialVariance;
			calendar.setTimeInMillis(timeMillis);

			Observation observation = new Observation(OBSERVATION_URI + id,
					OBSERVABLE_URI + id, lat, lon, calendar.getTime());
			// leave this unbound if it's the randomly chosen one to skip
			if (id == itemToSkip) {
				// skip!
			} else {
				solution.addBinding(new Binding(a, observation, id));
			}
			id++;
		}

		return solution;
	}
}
