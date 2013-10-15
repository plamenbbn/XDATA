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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;

public class Evaluator {
	
	/**
	 * Calculates a score (0.0 - 1.0) for the given solution.
	 * @param s Solution to evaluate
	 * @return Score between 0.0 and 1.0
	 */
	public static double score(Solution s) {
		RnrmProcess p = s.getProcess();
		
		Collection<Binding> bound = s.getNonNullBindings();
		Set<Activity> opposers = new HashSet<Activity>();
		for (Binding b : bound) {
			Set<Activity> op = p.getOpposesMap().get(b.getActivity());
			if(null != op) {
				opposers.addAll(op);
			}
		}
		double totalActs = p.getActivities().size();
		double boundActs = bound.size();
		double totalValidActs = totalActs - opposers.size();
		double score =  boundActs / totalValidActs;
		return score;
	}
}
