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
package com.bbn.c2s2.pint.pf.util;

import java.util.Date;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;

/**
 * Counts temporal violations within a Solution.
 * 
 * @author tself
 * 
 */
public class TemporalViolationCalculator {

	public TemporalViolationCalculator() {
	}

	public static int temporalViolations(Solution cs, Binding ca) {
		return beforeViolations(cs, ca) + afterViolations(cs, ca);
	}

	public static int temporalViolations(Solution s) {
		if (s == null) {
			throw new IllegalArgumentException("Null Solution.");
		}
		RnrmProcess p = s.getProcess();
		if (p == null) {
			throw new IllegalArgumentException("Null Process in Solution.");
		}
		int count = 0;
		for (Activity a : p.getActivities()) {
			IBinding b1 = s.getBinding(a);
			if (b1.getObservation() != null) {
				for (Activity hb : p.getHappensBefore(a)) {
					IBinding b2 = s.getBinding(hb);
					if (b2.getObservation() != null) {
						Date t1 = b1.getObservation().getTimestamp();
						Date t2 = b2.getObservation().getTimestamp();
						if (t2.getTime() > t1.getTime())
							count++;
					}
				}
			}
		}
		return count;
	}

	public static int beforeViolations(Solution cs, Binding ca) {

		if (cs == null) {
			throw new IllegalArgumentException("Null Solution.");
		}

		if (ca == null) {
			throw new IllegalArgumentException("Null Binding.");
		}

		RnrmProcess p = cs.getProcess();
		if (p == null) {
			throw new IllegalArgumentException("Null Process in Solution.");
		}
		Activity actToBind = p.getActivityFromId(ca.getActivityID());
		Set<Activity> happendBefore = p.getHappensBefore(actToBind);
		int index = p.getOrderedActivities().indexOf(actToBind);
		long biggerTime = ca.getObservation().getTimestamp().getTime();
		int violations = 0;
		int step = 1;
		index--;
		while (index >= 0) {
			IBinding next = null;
			// TODO: Remove usage of the get(i) method on RnrmProcess.
			// replace with iterator over the p.getOrderedActivities()
			if ((next = cs.getBinding(p.get(index))).getObservation() == null) {
				if (happendBefore.contains(p.get(index)))
					step++;
			} else {
				if (happendBefore.contains(p.get(index))) {
					long smallerTime = next.getObservation().getTimestamp()
							.getTime();
					if (smallerTime > biggerTime) {
						violations += step;
						step = 1;
					} else {
						step = 1;
					}
				}
			}
			index--;
		}
		return violations;
	}

	public static int afterViolations(Solution cs, Binding ca) {

		if (cs == null) {
			throw new IllegalArgumentException("Null Solution.");
		}

		if (ca == null) {
			throw new IllegalArgumentException("Null Binding.");
		}

		RnrmProcess p = cs.getProcess();
		if (p == null) {
			throw new IllegalArgumentException("Null Process in Solution.");
		}
		Activity actToBind = p.getActivityFromId(ca.getActivityID());
		Set<Activity> happendAfter = p.getHappensAfter(actToBind);
		int index = p.getOrderedActivities().indexOf(actToBind);
		long smallerTime = ca.getObservation().getTimestamp().getTime();
		int violations = 0;
		int step = 1;
		index++;
		while (index < p.size()) {
			IBinding next = null;
			if ((next = cs.getBinding(p.get(index))).getObservation() == null) {
				if (happendAfter.contains(p.get(index)))
					step++;
			} else {
				if (happendAfter.contains(p.get(index))) {
					long biggerTime = next.getObservation().getTimestamp()
							.getTime();

					// I think this is the error.
					// if (smallerTime > biggerTime) {
					if (smallerTime > biggerTime) {
						violations += step;
						step = 1;
					} else {
						step = 1;
					}
				}
			}
			index++;
		}
		return violations;
	}

}
