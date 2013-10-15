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

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Returns one of many valid orderings of the nodes based on a topological sort
 * of the process graph. That is, if node1 is greater than node2, it is because
 * it is either unordered wrt or later than node2. If node1 is greater than
 * node2, it will never be the case that node1 occurs strictly before node2.
 * 
 * @author reblace
 * 
 */
public class ActivityComparator implements Comparator<Activity> {
	private Map<Activity, Set<Activity>> happensBefore;

	public ActivityComparator(Map<Activity, Set<Activity>> happensBefore) {
		this.happensBefore = happensBefore;
	}

	@Override
	public int compare(Activity o1, Activity o2) {
		int toReturn = 0;
		if (happensBefore.get(o1).size() < happensBefore.get(o2).size()) {
			toReturn = -1;
		} else if (happensBefore.get(o1).size() > happensBefore.get(o2).size()) {
			toReturn = 1;
		}

		return toReturn;
	}
}
