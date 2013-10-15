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

import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;

/**
 * Calculates violation ratios for a Binding within a Solution.
 * 
 * @author tself
 * 
 */
public class ViolationRatios {

	public static double evaluateTemporalValidRatio(Solution cs, Binding tca) {
		if (cs == null) {
			throw new IllegalArgumentException("Null Solution.");
		}
		if (tca == null) {
			throw new IllegalArgumentException("Null Binding.");
		}
		return temporalValidRatio(cs, tca);
	}

	public static double temporalValidRatio(Solution cs, Binding ca) {
		if (cs == null) {
			throw new IllegalArgumentException("Null Solution.");
		}
		if (ca == null) {
			throw new IllegalArgumentException("Null Binding.");
		}

		RnrmProcess p = cs.getProcess();
		Activity actToBind = p.getActivityFromId(ca.getActivityID());

		Set<Activity> happendBefore = p.getHappensBefore(actToBind);
		double beforeViol = TemporalViolationCalculator
				.beforeViolations(cs, ca);
		double before = happendBefore.size();

		Set<Activity> happendAfter = p.getHappensAfter(actToBind);
		double afterViol = TemporalViolationCalculator.afterViolations(cs, ca);
		double after = happendAfter.size();

		double total_ViolBeforeAfter = beforeViol + afterViol;
		double total_BeforeAfter = before + after;

		if (Math.abs(total_BeforeAfter) < 0.00001) {
			return 1.0;
		}

		return (total_BeforeAfter - total_ViolBeforeAfter) / total_BeforeAfter;
	}

}
