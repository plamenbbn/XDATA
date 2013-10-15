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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.testdata.Processes;

public class PartialOrderValidator {

	/**
	 * Generates a Validation String map based on the array of String arrays.
	 * Eg. {{"A"}, {"B", "A"}} represents the map A -> {} B -> {A}
	 * 
	 * @param answer
	 *            An array of string arrays
	 * @return A map of Strings to sets of strings where the key corresponds to
	 *         the first element of each array in the input array of string
	 *         arrays
	 */
	public static Map<String, Set<String>> generateAnswer(String[][] answer) {
		Map<String, Set<String>> toReturn = new HashMap<String, Set<String>>();

		// loop over the arrays of string arrays
		for (String[] actEntry : answer) {
			// extract the activity key
			String key = Processes.getActivityLabel(actEntry[0]);
			// extract the prior activity labels
			String[] priors = Processes.getParensCommaSepList(actEntry[1]);
			Set<String> priorSet = new HashSet<String>(priors.length);
			for (String prior : priors) {
				priorSet.add(prior);
			}
			toReturn.put(key, priorSet);
		}

		return toReturn;
	}

	/**
	 * Validates that the {@link Activity} map and the {@link String}
	 * {@link Map} agree. That is, the strings correspond to the labels of the
	 * elements in the {@link Activity} {@link Map}.
	 * 
	 * @param map
	 *            The {@link Activity} {@link Map} that represents the actual
	 *            results
	 * @param answers
	 *            The expected results - the {@link Map} of labels for the
	 *            expected arrangement of the {@link Activity}'s involved
	 */
	public static void validateResults(String msg,
			Map<Activity, Set<Activity>> map, Map<String, Set<String>> answers) {

		// check to make sure they're the same size
		TestCase.assertEquals(msg, answers.size(), map.size());

		// verify keys match
		for (String key : answers.keySet()) {
			boolean found = false;
			for (Activity act : map.keySet()) {
				if (act.getLabel().equals(key)) {
					found = true;
					break;
				}
			}
			TestCase.assertTrue(msg, found);
		}

		// check contents of each hash
		for (Activity act : map.keySet()) {

			Set<Activity> acts = map.get(act);
			Set<String> priors = answers.get(act.getLabel());

			TestCase.assertNotNull(msg, priors);

			// test that they have the same size (this covers case with 0
			// priors)
			TestCase.assertEquals(msg, priors.size(), acts.size());

			// now compare the priors
			for (String s : priors) {
				boolean found = false;
				for (Activity priorAct : acts) {
					if (s.equals(priorAct.getLabel())) {
						found = true;
						break;
					}
				}
				TestCase.assertTrue(msg, found);
			}
		}
	}
}
