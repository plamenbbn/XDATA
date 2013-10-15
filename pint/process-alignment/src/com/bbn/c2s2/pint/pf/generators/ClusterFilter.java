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
package com.bbn.c2s2.pint.pf.generators;

import java.util.List;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;
import com.bbn.c2s2.pint.pf.heuristics.FilterHeuristic;

/**
 * This class provides a way to easily iterate through a filtered subset
 * of a list of {@link Cluster}'s such that only 'valid' clusters are 
 * returned. A valid cluster is one where the number of {@link Binding}'s, 
 * number of unique {@link Activity}'s, and number of unique 
 * {@link Observation}'s each exceed the minimum cluster size which is 
 * (processSize * percentFilled) as specified by the input 
 * {@link RnrmProcess} and the {@link PintConfiguration} 
 * 
 * @author reblace
 *
 */
public class ClusterFilter  {

	private List<Cluster> clusters = null;
	private int clusterIndex;
	private FilterHeuristic filter;

	/**
	 * Create a new {@link ClusterFilter} that will provide an easy way
	 * to iterate through a list of {@link Cluster}'s, returning only those
	 * that are valid.
	 * @param clusters The list of clusters through which to iterate
	 * @param process The process by which to filter
	 * @param config The configuration for the filter
	 */
	public ClusterFilter(List<Cluster> clusters, FilterHeuristic filter) {
		this.clusters = clusters;
		this.filter = filter;
		reset();
	}

	/**
	 * Resets the iterator behavior of the filter such that the next 
	 * valid {@link Cluster} to be returned will be the earliest valid 
	 * cluster in the list
	 */
	public void reset() {
		reset(0);
	}
	
	/**
	 * Resets the cursor on the list of valid {@link Cluster}'s to the
	 * specific index
	 * @param i The index to which to set the cursor
	 */
	private void reset(int i){
		clusterIndex = i;
	}

	/**
	 * Gets the total number of valid {@link Cluster}'s in the list of 
	 * clusters. This method leaves the underlying cursor unchanged. 
	 * @return The number of valid {@link Cluster}'s in the list
	 */
	public int validClusterCount() {
		int cursor = clusterIndex;
		reset();
		int count = 0;
		while (next() != null) {
			count++;
		}
		reset(cursor);
		
		return count;
	}

	/**
	 * @return The next valid {@link Clusterer} in the list of clusters
	 */
	public Cluster next() {
		boolean done = false;
		Cluster toReturn = null;
		while (!done) {
			if (clusterIndex >= clusters.size()) {
				done = true;
			} else if (this.filter.isValid((clusters.get(clusterIndex)))) {
				done = true;
				toReturn = clusters.get(clusterIndex);
			}
			clusterIndex++;
		}
		return toReturn;
	}

}
