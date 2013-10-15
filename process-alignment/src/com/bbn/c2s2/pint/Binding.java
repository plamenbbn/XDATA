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

import com.bbn.c2s2.pint.configuration.Constants;

/**
 * Representation of a potential Activity indicated by an Observation. For
 * convenience, a binding of observation o to activity a is written as (a,o)
 * through the remainder of this document.
 * 
 * @author tself
 * 
 */
public class Binding implements Serializable, IBinding {

	private static final long serialVersionUID = -2508307011758998609L;
	private int[] inconsistentObservationIds = new int[2];
	private Activity _activity;
	private Observation _observation;
	private int _activityID;
	private int _observationID;
	private final int _hashcode;
	
	
	public void setInconsistentObservationIds(int id1, int id2) {
		this.inconsistentObservationIds[0] = id1;
		this.inconsistentObservationIds[1] = id2;
	}
	
	public int[] getInconsistentObservationIds() {
		return this.inconsistentObservationIds;
	}

	/**
	 * Creates an empty binding (activity, null). The member _observationID is
	 * set to -1.
	 * 
	 * @param activity
	 */

	public Binding(Activity activity) {
		this(activity, null, -1);
	}

	/**
	 * Creates a binding (activity, observation).
	 * 
	 * @param activity
	 * @param observation
	 * @param observationID
	 */

	public Binding(Activity activity, Observation observation, int observationID) {
		_activity = activity;
		_observation = observation;
		_observationID = observationID;
		_activityID = activity.getID();
		_hashcode = calculateHash();
	}

	/**
	 * Calculates a hash code using the Activity ID and the Observation ID. This can be
	 * done up front to improve performance because this object is immutable.
	 * 
	 * @return Hash code
	 */
	private int calculateHash() {
		return Integer.toString(_activityID).hashCode() * Constants.HASH_PRIMES[0]
				+ Integer.toString(_observationID).hashCode()
				* Constants.HASH_PRIMES[1];
	}

	/**
	 * 
	 * @return the id assigned to the observation used in this binding. If the
	 *         binding contains a null observation, this method returns -1.
	 */

	public int getObservationID() {
		return _observationID;
	}

	/**
	 * 
	 * @return the id of the activity in this binding.
	 */

	public int getActivityID() {
		return _activityID;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IBinding#getObservation()
	 */

	public IObservation getObservation() {
		return _observation;
	}
	
	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IBinding#getActivityUri()
	 */
	public String getActivityUri() {
		return (null != _activity) ? _activity.getActivityURI() : null;
	}

	/**
	 * 
	 * @return the activity in this binding.
	 */

	public Activity getActivity() {
		return _activity;
	}

	@Override
	public boolean equals(Object obj) {
		boolean rv = false;
		if (null != obj && obj instanceof Binding) {
			Binding c = (Binding) obj;
			return (this._observationID == c.getObservationID()
					&& this.getActivityID() == c.getActivityID());
		}
		return rv;
	}

	@Override
	public int hashCode() {
		return _hashcode;
	}

	@Override
	public String toString() {
		if (this._observation == null)
			return String.format("%1$s {%2$d, %3$s}", "NONE", _activityID,
					"null");
		else
			return String.format("%1$s {%2$d, %3$d}", _observation.getLabel(),
					_activityID, _observationID);
	}

	public String toCsvString() {
		if (this._observation == null)
			return String.format("%1$s,%2$s", "NO-OBS", _activity.toString() );
		else
			return String.format("%1$s,%2$s", _observation.getLabel(), _activity.toString() );
	}

	
}
