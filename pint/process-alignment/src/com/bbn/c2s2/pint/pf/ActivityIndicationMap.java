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
package com.bbn.c2s2.pint.pf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;

/**
 * A map between {@link Observation} IDs and the {@link Activity} IDs that they
 * indicate
 * 
 * @author reblace
 * 
 */
public class ActivityIndicationMap {

	private Map<Integer, Set<Integer>> obsToActs = new HashMap<Integer, Set<Integer>>();

	private ActivityIndicationMap(Map<Integer, Set<Integer>> obsIdToActIdMap) {
		obsToActs = obsIdToActIdMap;
	}

	/**
	 * Creates a Map between {@link Observation} IDs and the {@link Activity}
	 * IDs that they indicate
	 * 
	 * @param bindings
	 *            {@link BindingGroup} populated with {@link Binding} objects
	 * @return SufficesForMap
	 */
	public static ActivityIndicationMap create(BindingGroup bindings) {
		ActivityIndicationMap rv = null;
		Map<Integer, Set<Integer>> obsToActs = new HashMap<Integer, Set<Integer>>();
		for (Activity a : bindings.getActivities()) {
			for (Binding b : bindings.getBindings(a)) {
				Set<Integer> acts = null;
				if ((acts = obsToActs.get(b.getObservationID())) != null) {
					acts.add(a.getID());
					obsToActs.put(b.getObservationID(), acts);
				} else {
					acts = new TreeSet<Integer>();
					acts.add(a.getID());
					obsToActs.put(b.getObservationID(), acts);
				}
			}
		}
		rv = new ActivityIndicationMap(obsToActs);
		return rv;
	}

	/**
	 * Returns the set of {@link Activity} IDs that are indicated by the given
	 * {@link Observation} ID
	 * 
	 * @param observationID
	 *            Integer ID of the {@link Observation}
	 * @return Set of Integer IDs for the indicated {@link Activity} objects
	 */
	public Set<Integer> getActivitySet(int observationID) {
		return this.obsToActs.get(observationID);
	}
}
