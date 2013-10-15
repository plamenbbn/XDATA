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

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.testdata.BindingGroupFactory;

public class ActivityIndicationMapTest extends TestCase {

	private List<Binding> bindings;

	protected void setUp() throws Exception {
		super.setUp();
		BindingGroupFactory factory = new BindingGroupFactory();
		bindings = factory.generateBindings();
	}

	private void validateContents(String msg, int[] answers, Set<Integer> result) {
		// verify size
		assertEquals(msg, answers.length, result.size());
		// check contents
		for (int ans : answers) {
			boolean found = false;
			for (int res : result) {
				if (ans == res) {
					found = true;
					break;
				}
			}
			assertTrue(msg, found);
		}
	}

	public void testCreate() {
		ActivityIndicationMap map = ActivityIndicationMap
				.create(new BindingGroup(bindings));
		// verify contents of map
		int[] answerLow = { 1, 3 };
		int[] answerHigh = { 2, 3 };
		for (int obsId = 1; obsId <= 8; obsId++) {
			Set<Integer> activities = map.getActivitySet(obsId);
			if (obsId <= 4) {
				validateContents("Complete Mappings", answerLow, activities);
			} else {
				validateContents("Complete Mappings", answerHigh, activities);
			}
		}
	}

	public void testGetActivitySet() {
		ActivityIndicationMap map = ActivityIndicationMap
				.create(new BindingGroup(bindings));
		// check existing mapping
		Set<Integer> activities = map.getActivitySet(1);
		validateContents("Correct Mapping", new int[] { 1, 3 }, activities);

		// check non-existent mapping
		activities = map.getActivitySet(0);
		assertNull("Non-existent Observation Check", activities);
	}
}
