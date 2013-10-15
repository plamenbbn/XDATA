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
package com.bbn.c2s2.util;

import java.util.ArrayList;
import java.util.Collection;

import com.bbn.c2s2.pint.Observation;

public class AplToPintConversionFactory {

	/**
	 * Convert a SimpleObservation object from the Web Service API to the
	 * SimpleObservation implementation of the Observation interface. This is a
	 * deep copy.
	 * 
	 * @param observations
	 *            The list of observations to convert
	 * @return The converted list of the observations in the same order
	 */
	public static Collection<Observation> convertObservations(
			Collection<edu.jhuapl.c2s2.pp.observation.Observation> observations) {

		// short circuit out if null
		if (null == observations) {
			return null;
		}

		Collection<Observation> toReturn = new ArrayList<Observation>(
				observations.size());

		for (edu.jhuapl.c2s2.pp.observation.Observation so : observations) {
			Observation o = new Observation(so.getUri(), so.getObservableUri(),
					so.getGeocode().getLatitude(), so.getGeocode()
							.getLongitude(), so.getObservationTimestamp()
							.getTime());
			toReturn.add(o);
		}

		return toReturn;
	}
}
