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
package com.bbn.c2s2.pint.pf.heuristics;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.configuration.Constants;
import com.bbn.c2s2.pint.configuration.PintConfigurable;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.ActivityIndicationMap;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.util.PairwiseConsistencyMetrics;

/**
 * 
 * @author reblace
 *
 */
public class EdgeWeightHeuristic extends PintConfigurable implements
		IEdgeWeightHeuristic {

	private ActivityIndicationMap obsToActs;
	private double maxDistanceKm;
	private double maxTimespan;
	private RnrmProcess process;

	public EdgeWeightHeuristic(RnrmProcess process, 
			PintConfiguration config, ActivityIndicationMap map) {
		super(config);
		this.maxDistanceKm = getConfig().getDouble(
				Constants.KEY_PROCESS_MAX_DISTANCE_KM,
				Constants.DEFAULT_PROCESS_MAX_DISTANCE_KM);
		this.maxTimespan = getConfig().getDouble(
				Constants.KEY_PROCESS_MAX_TIMESPAN_MS,
				Constants.DEFAULT_PROCESS_MAX_TIMESPAN);
		this.obsToActs = map;
		this.process = process;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic#linked(com.bbn.c2s2
	 * .pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding,
	 * com.bbn.c2s2.pint.Binding)
	 */
	public boolean linked(Binding bindingA,
			Binding bindingB) {
		return this.getLinkWeight(bindingA, bindingB) > 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic#getLinkWeight(com
	 * .bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding,
	 * com.bbn.c2s2.pint.Binding)
	 */
	public double getLinkWeight(Binding bindingA,
			Binding bindingB) {

		double rv = 0;
		// two bindings do not agree if they have the same activity id
		if (bindingA.getActivityID() == bindingB.getActivityID()) {
			rv = 0;
		}
		// two bindings do not agree if they have the same observation id
		else if (bindingA.getObservationID() == bindingB.getObservationID()) {
			rv = 0;
			// two bindings do not agree if the distance between them is greater
			// than the max allowed
		} else if (PairwiseConsistencyMetrics.distance(bindingA, bindingB) > maxDistanceKm) {

			rv = 0;
			// two bindings do not agree if the timespan between them is greater
			// than the max allowed
		} else if (PairwiseConsistencyMetrics
				.timeDifference(bindingA, bindingB) > maxTimespan) {
			rv = 0;
		}
		// else if (PairwiseConsistencyMetrics.unordered(process, aBinding,
		// anotherBinding)) {
		// return 0;
		// }
		else if (PairwiseConsistencyMetrics.partialOrderViolation(process,
				bindingA, bindingB)) {
			rv = 0;
		} else {
			double d1 = this.obsToActs.getActivitySet(
					bindingA.getObservationID()).size();
			double d2 = this.obsToActs.getActivitySet(
					bindingB.getObservationID()).size();
			rv = (1 / d1) * (1 / d2);
		}

		return rv;

	}

}
