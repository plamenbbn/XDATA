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

public class ObservationTest extends TestCase {

	Observation o1;
	Observation o2;
	Observation o3;

	protected void setUp() throws Exception {
		super.setUp();
		o1 = this.createObservation("http://x");
		o2 = this.createObservation("http://x");
		o3 = this.createObservation("http://y");
	}

	public void testEqualsObject() {
		assertTrue(this.o1.equals(o2));
		assertEquals(o1.hashCode(), o2.hashCode());
		assertTrue(this.o2.equals(o1));
		assertFalse(this.o1.equals(o3));
		assertFalse(o1.hashCode() == o3.hashCode());
		assertFalse(this.o3.equals(o1));
		assertFalse(this.o3.equals(new Object()));
	}

	public void testGetLabel() throws Exception {
		o3.setLabel("Label Y");
		assertEquals("Label Y", o3.getLabel());
	}

	private Observation createObservation(String uri) {
		Date date = new Date();
		date.setTime(0);
		Observation obs = new Observation(uri, "", 0, 0, date);
		return obs;
	}

}
