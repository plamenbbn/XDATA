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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.ActivityComparator;

/**
 * Representation of a Red Nodal Reference Model Process. This is a partial
 * ordering of {@link Activity} objects along with a process URI and label.
 * 
 * The expected way to create a {@link RnrmProcess} is to pass in a map that
 * defines the happens-before relationships for a partial ordering of
 * activities.
 * 
 * @author reblace
 * 
 */
public class RnrmProcess implements Serializable {

	private static final long serialVersionUID = 7396759495797440513L;

	private String processUri;
	private String processLabel;
	private List<Activity> activities;
	private Map<Activity, Set<Activity>> happensBefore;
	private Map<Activity, Set<Activity>> happensAfter;
	private Map<Activity, Set<Activity>> opposes;
	private Map<Integer, Activity> idToAct = new HashMap<Integer, Activity>();

	/**
	 * Returns the {@link Set} of {@link Activity} in this {@link RnrmProcess}
	 * 
	 * @return Uunordered {@link Set} of {@link Activity} involved in this
	 *         process
	 */
	public Set<Activity> getActivities() {
		return this.happensBefore.keySet();
	}

	public RnrmProcess() {
	}

	/**
	 * Creates a new {@link RnrmProcess} initialized with its internal caches
	 * 
	 * @param happensBefore
	 *            The partial ordering of the {@link Activity} involved in this
	 *            process
	 * @param uri
	 *            The Uri of the process
	 * @param label
	 *            The label of the process
	 */
	public RnrmProcess(Map<Activity, Set<Activity>> happensBefore,
			Map<Activity, Set<Activity>> happensAfter,
			Map<Activity, Set<Activity>> opposes, String uri,
			String label) {

		processUri = uri;
		processLabel = label;

		// generate the id to activity map
		for (Activity a : happensBefore.keySet()) {
			this.idToAct.put(a.getID(), a);
		}

		this.happensBefore = happensBefore;
		this.happensAfter = happensAfter;
		this.opposes = opposes;

		// generate the sorted activities list
		this.activities = new ArrayList<Activity>(this.happensBefore.keySet());
		ActivityComparator a = new ActivityComparator(this.happensBefore);
		Collections.sort(this.activities, a);
		
	}

	/**
	 * States whether or not this {@link Process} imposes ordering constraints
	 * on its {@link Activity} objects. Example: single-activity processes and
	 * parallel processes do not
	 * 
	 * @return True if this process is order-constrained. False otherwise.
	 */
	public boolean isOrderConstrained() {
		boolean orderConstrained = true;
		// single activity
		if (size() < 2) {
			orderConstrained = false;
		}

		// parallel
		if (orderConstrained) {
			int maxBucket = 0;
			for (Activity a : this.getActivities()) {
				Set<Activity> priors = getHappensBefore(a);
				maxBucket = Math.max(maxBucket, priors.size());
			}
			orderConstrained = maxBucket > 0;
		}
		return orderConstrained;
	}

	/**
	 * @return The Uri of the process
	 */
	public String getProcessUri() {
		return processUri;
	}

	/**
	 * @return The label of the process
	 */
	public String getLabel() {
		return processLabel;
	}

	@Override
	public String toString() {
		return (processLabel == null) ? processUri : processLabel;
	}

	/**
	 * @param id
	 *            The id of the {@link Activity} to return
	 * @return the {@link Activity} that corresponds to the id, or null if the
	 *         id is not found
	 */
	public Activity getActivityFromId(int id) {
		return this.idToAct.get(id);
	}

	/**
	 * @return The ordered {@link List} of {@link Activity} in this process.
	 *         Elements of this list are ordered according to the
	 *         {@link ActivityComparator}
	 */
	public List<Activity> getOrderedActivities() {
		return this.activities;
	}

	/**
	 * @return The {@link Map} that encodes the partial ordering of the
	 *         {@link Activity} in this process
	 */
	public Map<Activity, Set<Activity>> getHappensBeforeMap() {
		return happensBefore;
	}
	
	public Map<Activity, Set<Activity>> getOpposesMap() {
		return this.opposes;
	}

	/**
	 * @param a
	 *            The {@link Activity} of interest
	 * @return The {@link Set} of {@link Activity} that are unordered wrt or
	 *         before the provided {@link Activity}, or null if the
	 *         {@link Activity} is not found
	 */
	public Set<Activity> getHappensBefore(Activity a) {
		return this.happensBefore.get(a);
	}

	/**
	 * @param a
	 *            The {@link Activity} of interest
	 * @return The {@link Set} of {@link Activity} that are unordered wrt or
	 *         after the provided {@link Activity}, or null if the
	 *         {@link Activity} is not found
	 */
	public Set<Activity> getHappensAfter(Activity a) {
		return this.happensAfter.get(a);
	}

	/**
	 * @return The number of {@link Activity} that are involved in this process
	 */
	public int size() {
		return this.activities.size();
	}

	/**
	 * @param index
	 *            The index designating the {@link Activity} of interest in the
	 *            process
	 * @return The {@link Activity} corresponding to the index in the partial
	 *         ordering of the process
	 */
	public Activity get(int index) {
		// TODO: Does this even make sense with a partial order?
		return this.activities.get(index);
	}
	
	public boolean opposes(Activity act, Activity anotherAct) {
		if (this.opposes.get(act).contains(anotherAct))
			return true;
		if (this.opposes.get(anotherAct).contains(act))
			return true;
		return false;
		
	}
	
	public String toCsvString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append( "Process.URI," + this.getProcessUri() + "\n" );
		sb.append( "Rocess.label," + this.getLabel() + "\n" );
		
		for( Activity a : activities ) {
			sb.append( a.getLabel() + "," );
			sb.append( a.toString() + "," );
			sb.append( "before:{" );
			if( happensBefore.get(a) != null ) {
				boolean first = true;
				for( Activity ab : happensBefore.get(a) ) {
					if( first ) {
						sb.append( ab.getLabel() );
						first = false;
					}
					else {
						sb.append( "," );
						sb.append( ab.getLabel() );
					}
				}			
			}
			sb.append("},");
			
			sb.append( "after:{" );
			if( happensAfter.get(a) != null ) {
				boolean first = true;
				for( Activity a2 : happensAfter.get(a) ) {
					if( first ) {
						sb.append( a2.getLabel() );
						first = false;
					}
					else {
						sb.append( "," );
						sb.append( a2.getLabel() );
					}
				}			
			}
			sb.append("},");
			
			sb.append( "opposes:{" );
			if( opposes.get(a) != null ) {
				boolean first = true;
				for( Activity a2 : opposes.get(a) ) {
					if( first ) {
						sb.append( a2.getLabel() );
						first = false;
					}
					else {
						sb.append( "," );
						sb.append( a2.getLabel() );
					}
				}			
			}
			sb.append("}\n");
		}
		
		return sb.toString();
	}

}
