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
package com.bbn.c2s2.pint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;

/**
 * Representation of a collection of bindings.
 * 
 * @author jsherman
 * 
 */
public class BindingGroup implements Serializable {

	private static final long serialVersionUID = 2754604202309025596L;

	private Map<Activity, List<Binding>> candidates;

	// public static List<Binding> createFuzzyBindingList(List<Binding>
	// bindings, PintConfiguration config) {
	// int obsId = bindings.size() + 1; //maybe we don't need + 1 but just to be
	// sure...
	// int arraySize = bindings.size();
	// for (int i = 0; i < arraySize; i++) {
	// Long.parseLong(config.getString("foo", "0"));
	// }
	// return null;
	// }


	public static List<Binding> createBindingList( RnrmProcess process, Collection<Observation> observations, ObservableSet observables ) {
	
		List<Binding> bindings = new ArrayList<Binding>();

		int nextObsID = 0;
		for( Observation obs : observations ) {
			String observableUri = obs.getObservableUri();
			
			//Collection<Activity> activities = observableToActivities.get(observableUri); 
					
			// TODO: observables should really return multiple possible activities per observable URI...
			Activity act = observables.getActivityFor(observableUri);
			
//			if (null != activities) {
//				Iterator<Activity> actIt = activities.iterator();
//				while (actIt.hasNext()) {
//					Activity act = actIt.next();
					if( act != null  ) {
						Binding binding = new Binding( new Activity(act.getID(), act.getActivityURI()), obs, nextObsID );
						bindings.add(binding);
					}
//				}
				nextObsID++;
//			}
		}

		
		return bindings;
	}

	
	public static List<Binding> createBindingList( RnrmProcess process, Collection<Observation> obs, RdfProcess rdfProcess ) {
		
		// generate observable:activites map
		Map<String, Set<Activity>> observableToActivities = new HashMap<String, Set<Activity>>();
		Set<Activity> allActivities = process.getActivities();
		for (Activity act : allActivities) {
			List<String> observables = rdfProcess.getObservableUris(act);
			for (String observableUri : observables) {
				if (null == observableToActivities.get(observableUri)) {
					observableToActivities.put(observableUri,
							new HashSet<Activity>());
				}
				Set<Activity> acts = observableToActivities.get(observableUri);
				acts.add(act);
			}
		}

		List<Binding> bindings = new ArrayList<Binding>();
		int nextObsID = 0;
		for (Observation o : obs) {
			String observableUri = o.getObservableUri();
			Collection<Activity> activities = observableToActivities
					.get(observableUri);
			if (null != activities) {
				Iterator<Activity> actIt = activities.iterator();
				while (actIt.hasNext()) {
					Activity act = actIt.next();
					Binding binding = new Binding(new Activity(act.getID(), act
							.getActivityURI()), o, nextObsID);
					bindings.add(binding);
				}
				nextObsID++;
			}
		}
		return bindings;
	}

	public BindingGroup(Collection<Binding> bindings) {
		init(bindings);
	}

	private void init(Collection<Binding> bindings) {
		this.candidates = new HashMap<Activity, List<Binding>>();
		for (Binding b : bindings) {
			Activity a = b.getActivity();
			if (this.candidates.get(a) == null) {
				ArrayList<Binding> t = new ArrayList<Binding>();
				t.add(b);
				this.candidates.put(a, t);
			} else {
				this.candidates.get(a).add(b);
			}
		}
	}

	/**
	 * Gets all of the {@link Binding} objects in this group
	 * 
	 * @return All {@link Binding} objects
	 */
	public List<Binding> getBindings() {
		List<Binding> out = new ArrayList<Binding>();
		for (Activity a : this.candidates.keySet()) {
			out.addAll(this.candidates.get(a));
		}
		return out;
	}

	public int bindingCount() {
		int rv = 0;
		for (List<Binding> bag : candidates.values()) {
			rv += bag.size();
		}
		return rv;
	}

	public Set<Activity> getActivities() {
		return this.candidates.keySet();
	}

	public List<Binding> getBindings(Activity a) {
		return this.candidates.get(a);
	}

}
