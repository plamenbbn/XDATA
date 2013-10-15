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

import junit.framework.TestCase;

public class ActivityTest extends TestCase {

	public void testEqualsObject() {

		int activity1Id = 0;
		int activity2Id = 1;
		int activity3Id = activity1Id;
		String activity1Uri = "http://x";
		String activity2Uri = "http://y";
		String activity3Uri = activity1Uri;
		Activity activity1 = new Activity(activity1Id, activity1Uri);
		Activity activity2 = new Activity(activity2Id, activity2Uri);
		Activity activity3 = new Activity(activity3Id, activity3Uri);
		assertFalse(activity1.equals(activity2));
		assertFalse(activity2.equals(activity3));
		assertTrue(activity1.equals(activity3));
		assertEquals(activity1.hashCode(), activity3.hashCode());

		int activity4Id = 0;
		int activity5Id = activity4Id;
		String activity4Uri = "http://x";
		String activity5Uri = "http://y";
		Activity activity4 = new Activity(activity4Id, activity4Uri);
		Activity activity5 = new Activity(activity5Id, activity5Uri);
		assertFalse(activity4.equals(activity5));
		assertFalse(activity4.hashCode() == activity5.hashCode());

		int activity6Id = 1;
		String activity6Uri = "http://x";
		Activity activity6 = new Activity(activity6Id, activity6Uri);
		assertFalse(activity6.equals(activity5));

		Object notAnActivity = new Object();
		assertFalse(activity1.equals(notAnActivity));

	}

	public void testToString() throws Exception {
		Activity a = new Activity(0, "http://x");
		String result = a.toString();

		assertNotNull(result);
		assertTrue(result.length() > 0);
	}

}
