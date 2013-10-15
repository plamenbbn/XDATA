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

import java.util.HashSet;
import java.util.Set;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.configuration.PintConfigurable;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;

/**
 * Heuristic used to filter out clusters that need not be analyzed.
 * 
 * @author tself
 * 
 */
public class FilterHeuristic extends PintConfigurable {

	private static final String CONFIG_PREFIX = "pf.generators.clusterfilter.";
	private static final String KEY_PERCENT_FILLED = CONFIG_PREFIX
			+ "percent-filled";
	private final double DEFAULT_PERCENT_FILLED = 0.4;
	private int minClusterSize;

	public FilterHeuristic(RnrmProcess process, PintConfiguration config) {
		super(config);
		double percentFilled = getConfig().getDouble(KEY_PERCENT_FILLED,
				DEFAULT_PERCENT_FILLED);
		int actsInProc = process.size();
		minClusterSize = (int) Math.round(actsInProc * percentFilled);

	}

	public boolean isValid(Cluster cluster) {
		if (cluster.getBindings().size() < this.minClusterSize) {
			return false;
		}

		Set<Integer> uniqueActivities = new HashSet<Integer>();
		Set<Integer> uniqueObservations = new HashSet<Integer>();
		for (Binding b : cluster.getBindings()) {
			uniqueActivities.add(b.getActivityID());
			uniqueObservations.add(b.getObservationID());
		}

		if (uniqueActivities.size() < this.minClusterSize) {
			return false;
		} else if (uniqueObservations.size() < this.minClusterSize) {
			return false;
		}

		return true;
	}

}
