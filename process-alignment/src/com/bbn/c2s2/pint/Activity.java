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
 * Representation of an activity element from the RNRM.
 * 
 * @author jsherman
 * 
 */

public class Activity implements Serializable {

	private static final long serialVersionUID = 7578770123698603070L;
	private final int _hashCode;
	private int _id;
	private String _activityUri;
	private String _label = "";

	/**
	 * Sets the label for this activity. The label is meant to be a shorter,
	 * more intelligible, identifier than the activity's URI.
	 * 
	 * @param label
	 */

	public void setLabel(String label) {
		_label = label;
	}

	/**
	 * Gets the label for this activity. The label is meant to be a shorter,
	 * more intelligible, identifier than the activity's URI.
	 * 
	 * @return
	 */

	public String getLabel() {
		return this._label;
	}

	/**
	 * Creates a new activity.
	 * 
	 * @param id
	 *            a unique identifier for this activity
	 * @param activityUri
	 */
	public Activity(int id, String activityUri) {
		this(id, activityUri, activityUri);
	}

	public Activity(int id, String activityUri, String activityLabel) {
		if(null == activityUri) {
			throw new IllegalArgumentException("Cannot create an Activity with a null URI.");
		}
		_id = id;
		_activityUri = activityUri;
		_label = activityLabel;
		_hashCode = calculateHash(_id, _activityUri);
	}

	/**
	 * Generates the hashcode for this Activity. The hashcode is based on a
	 * combination of the URI and the ID. The hashcodes of the values are
	 * multiplied by prime numbers in order to spread the hashcodes of various
	 * Activity objects across the integer range. This approach only works
	 * because this object is immutable.
	 * 
	 * @param id
	 *            Integer ID of this Activity
	 * @param uri
	 *            URI of this Activity
	 * @return Hash code for this Activity
	 */
	private int calculateHash(int id, String uri) {
		return uri.hashCode() * Constants.HASH_PRIMES[0]
				+ Integer.toString(id).hashCode() * Constants.HASH_PRIMES[1];
	}

	/**
	 * 
	 * @return the id of this activity
	 */

	public int getID() {
		return _id;
	}

	/**
	 * 
	 * @return the URI of this activity
	 */

	public String getActivityURI() {
		return _activityUri;
	}

	@Override
	public boolean equals(Object o) {
		return null != o && o instanceof Activity
				&& ((Activity) o).getID() == _id
				&& ((Activity) o).getActivityURI().equals(_activityUri);
	}

	@Override
	public int hashCode() {
		return _hashCode;
	}

	@Override
	public String toString() {
		return String.format("Activity {%1$s|%2$s}", _id, _activityUri);
	}

}
