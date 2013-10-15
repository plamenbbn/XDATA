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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bbn.c2s2.pint.configuration.Constants;

/**
 * Representation of an observation.
 * 
 * @author jsherman
 * 
 */

public class Observation implements Serializable, IObservation {

	private static final long serialVersionUID = 5604313795975281562L;
	private String _observationLabel;
	private double _lat;
	private double _lon;
	private Date _timestamp;
	private String _observationUri;
	private String _observableUri;
	private final int _hashCode;

	/**
	 * Creates a new observation.
	 * 
	 * @param observationUri
	 * @param observableUri
	 * @param lat
	 * @param lon
	 * @param timestamp
	 */
	
	public static DateFormat DF = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );

	public Observation(String observationUri, String observableUri, double lat,
			double lon, Date timestamp) {
		if(null == observationUri) {
			throw new IllegalArgumentException("Cannot create an Observation with a null URI.");
		}
		if(null == timestamp) {
			throw new IllegalArgumentException("Cannot create an Observation with a null timestamp.");
		}
		_observationUri = observationUri;
		_observableUri = observableUri;
		_lat = lat;
		_lon = lon;
		_timestamp = timestamp;
		// This object is immutable so it's safe to calculate
		// the hash once and improve performance
		_hashCode = calculateHash();
	}

	/**
	 * Calculates a hash code using the URI, lat, lon and timestamp. This can be
	 * done up front to improve performance because this object is immutable.
	 * 
	 * @return Hash code
	 */
	private int calculateHash() {
		return _observationUri.hashCode() * Constants.HASH_PRIMES[0]
				+ Double.toString(_lat).hashCode() * Constants.HASH_PRIMES[1]
				+ Double.toString(_lon).hashCode() * Constants.HASH_PRIMES[2]
				+ _timestamp.hashCode() * Constants.HASH_PRIMES[3];
	}

	/**
	 * NOTE: this method is meant to be temporary. We expect RNRM observations
	 * to include labels in the future.
	 * 
	 * @param obsUri
	 * @return an abbreviated name for this observation.
	 */

	static String uriToName(String obsUri) {
		// 4 fields after the name, separated by underscores
		final int NUM_FIELDS = 4;

		String rv = null;
		int fragIndex = obsUri.indexOf('#');
		if (fragIndex > -1 && fragIndex + 1 < obsUri.length()) {
			rv = obsUri.substring(fragIndex + 1);
		}

		if (null != rv) {
			for (int i = 0; i < NUM_FIELDS; i++) {
				int endIndex = rv.lastIndexOf('_');
				if (endIndex > -1) {
					rv = rv.substring(0, endIndex);
				} else {
					break;
				}
			}
			rv = rv.replace('_', ' ');
		} else {
			rv = obsUri;
		}
		return rv;
	}

	/**
	 * Sets the label for this observation.
	 * 
	 * @param observationLabel
	 */

	public void setLabel(String observationLabel) {
		_observationLabel = observationLabel;
	}

	/**
	 * 
	 * @return the label for this observation.
	 */

	public String getLabel() {
		// if no label, generate one
		String rv = _observationLabel;
		if (null == rv) {
			rv = uriToName(_observationUri);
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IObservation#getUri()
	 */

	public String getUri() {
		return _observationUri;
	}

	/**
	 * 
	 * @return the observable URI that this observation maps to.
	 */

	public String getObservableUri() {
		return _observableUri;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IObservation#getLat()
	 */

	public double getLat() {
		return _lat;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IObservation#getLon()
	 */

	public double getLon() {
		return _lon;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.IObservation#getTimestamp()
	 */

	public Date getTimestamp() {
		return _timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		boolean rv = false;
		if (null != obj && obj instanceof Observation) {
			Observation c = (Observation) obj;
			rv = c.getUri().equals(_observationUri)
					&& c.getLat() == _lat && c.getLon() == _lon
					&& c.getTimestamp().equals(_timestamp);
		}
		return rv;
	}

	@Override
	public int hashCode() {
		return _hashCode;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public String toCsvString() {
		return getLabel() + "," + DF.format(getTimestamp()) + "," + getLon() + "," + getLat() + "," + getUri() + "," + getObservableUri();
	}

	
}
