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

import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.configuration.PintConfigurable;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.PairGraph;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.util.JaccardCalculator;

/**
 * Heuristic used to determine if two Bindings belong in the same cluster.
 * 
 * @author tself
 * 
 */
public class ClusterHeuristic extends PintConfigurable {

	private static final String CONFIG_PREFIX = "pf.clusterer.";
	private final String KEY_AGREEMENT_THRESHOLD = CONFIG_PREFIX
			+ "agreement-threshold";
	private final double DEFAULT_AGREEMENT_THRESHOLD = 0.4;
	private static final String KEY_PERCENT_FILLED = "pf.generators.clusterfilter.percent-filled";
	private final double DEFAULT_PERCENT_FILLED = 0.4;

	private int minClusterSize;
	private double agreementThreshold;

	public ClusterHeuristic(RnrmProcess process, PintConfiguration config) {
		super(config);

		agreementThreshold = getConfig().getDouble(KEY_AGREEMENT_THRESHOLD,
				DEFAULT_AGREEMENT_THRESHOLD);

		double percentFilled = getConfig().getDouble(KEY_PERCENT_FILLED,
				DEFAULT_PERCENT_FILLED);
		double actsInProcess = process.size();
		minClusterSize = (int) Math.round(actsInProcess * percentFilled);

	}

	public boolean isValidClusterHead(IBinding binding, PairGraph pairGraph) {
		int[][] nonZeroIndecies = pairGraph.getNonZeroCols();
		int edges = nonZeroIndecies[pairGraph.indexOf(binding)].length;
		if (edges >= this.minClusterSize)
			return true;
		else
			return false;

	}

	public boolean agreesWithCluster(IBinding candidate,
			IBinding clusterCenter, PairGraph pairGraph) {
		double agreementScore = JaccardCalculator.getJaccard(pairGraph,
				clusterCenter, candidate);
		if (agreementScore >= agreementThreshold)
			return true;
		else
			return false;
	}

}
