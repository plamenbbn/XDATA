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

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;

public class ActivityValidator {
	public static void validateUnorderedContents(String msg,
			String[] answerLabels, Collection<Activity> activities) {
		// compare size
		TestCase.assertEquals(msg + " (size)", answerLabels.length, activities
				.size());

		// compare contents
		for (int i = 0; i < answerLabels.length; i++) {
			boolean found = false;
			for (Activity a : activities) {
				if (answerLabels[i].equals(a.getLabel())) {
					found = true;
					break;
				}
			}
			TestCase.assertTrue(msg + "(contents)", found);
		}
	}

	/**
	 * Compares the complete order of the answerLabels to the {@link List} of
	 * {@link Activity} objects. This will not respect a partial order. Testing
	 * a partial order requires calling this method multiple times for the
	 * various correct permutations.
	 * 
	 * @param msg
	 *            String describing the current test
	 * @param possibleAnswers
	 *            Array of possible orders as String Arrays
	 * @param activities
	 *            {@link List} of {@link Activity} objects
	 */
	public static void validateOrderedContents(String msg,
			String[][] possibleAnswers, List<Activity> activities) {
		boolean foundMatch = false;
		for (int i = 0; i < possibleAnswers.length; i++) {
			// compare unordered contents first
			validateUnorderedContents(msg, possibleAnswers[i], activities);

			// contents are the same. Now check order
			boolean ordered = true;
			for (int j = 0; j < possibleAnswers[i].length; j++) {
				if (!possibleAnswers[i][j].equals(activities.get(j).getLabel())) {
					ordered = false;
					break;
				}
			}
			if (ordered) {
				foundMatch = true;
				break;
			}
		}
		TestCase.assertTrue(msg, foundMatch);
	}
}
