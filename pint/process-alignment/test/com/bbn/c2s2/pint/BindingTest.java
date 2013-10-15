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

import java.util.Date;

import junit.framework.TestCase;

public class BindingTest extends TestCase {

	IBinding b1;
	IBinding b2;
	IBinding b3;
	IBinding b4;
	IBinding b5;
	IBinding b6;
	IBinding b7;
	IBinding b8;
	IBinding b9;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// b1 is (0,0) at (0,0,0)
		b1 = new Binding(this.createActivity(0), this
				.createObservation(0, 0, 0), 0);
		// b2 is (0,0) at (0,0,0)
		b2 = new Binding(this.createActivity(0), this
				.createObservation(0, 0, 0), 0);
		// b3 is (0,0) at (0,0,1)
		b3 = new Binding(this.createActivity(0), this
				.createObservation(0, 0, 1), 0);
		// b4 is (0,0) at (0,1,0)
		b4 = new Binding(this.createActivity(0), this
				.createObservation(0, 0, 2), 0);
		// b5 is (1,0) at (0,0,0)
		b5 = new Binding(this.createActivity(1), this
				.createObservation(0, 0, 0), 0);
		// b6 is (0,1) at (0,0,0)
		b6 = new Binding(this.createActivity(0), this
				.createObservation(0, 0, 0), 1);
		// b7 is a binding with a null observation and activity id 0
		b7 = new Binding(this.createActivity(0), null, 0);
		// b8 is a binding with a null observation and activity id 1
		b8 = new Binding(this.createActivity(1), null, 0);
		// b9 is a binding with a null observation and activity id 1
		b9 = new Binding(this.createActivity(1), null, 0);
	}

	public void testEqualsObject() {
		assertTrue(b1.equals(b2));
		assertTrue(b7.equals(b7));
		assertFalse(b7.equals(b8));
		assertFalse(b1.equals(b5));
		assertFalse(b1.equals(b6));
		assertFalse(b5.equals(b6));
		assertFalse(b5.equals(new Object()));
	}

	private Activity createActivity(int id) {
		Activity a = new Activity(id, "");
		return a;
	}

	private Observation createObservation(int time, double lat, double lon) {
		Date date = new Date();
		date.setTime(time);
		Observation obs = new Observation("", "", lat, lon, date);
		return obs;
	}

	public void testHashCode() throws Exception {
		assertTrue(b1.hashCode() == b2.hashCode());
		assertTrue(b8.hashCode() == b9.hashCode());
		assertFalse(b1.hashCode() == b9.hashCode());
	}

	public void testToString() throws Exception {

		String obsUri = "http://c2s2.bbn.com#a_b_c_d_e";
		IBinding b = new Binding(createActivity(0), new Observation(obsUri, "",
				0, 0, new Date()), 0);

		assertNotNull(b1.toString());
		assertNotNull(b2.toString());
		assertNotNull(b7.toString());
		assertNotNull(b8.toString());
		assertNotNull(b.toString());
	}

}
