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

import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

public class ActivityComparatorTest extends TestCase {

	private RnrmProcess procSerial;
	private RnrmProcess procParallel;
	private RnrmProcess procForkJoin;
	private RnrmProcess procBackToBackForkJoin;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		procSerial = TestRnrmProcessFactory.createSerialProcess();
		procParallel = TestRnrmProcessFactory.createParallelProcess();
		procForkJoin = TestRnrmProcessFactory.createForkJoinProcess();
		procBackToBackForkJoin = TestRnrmProcessFactory
				.createDoubleForkJoinProcess();
	}

	public void testCompare() {
		// equal activities
		ActivityComparator ac = new ActivityComparator(procSerial
				.getHappensBeforeMap());
		Activity a1 = procSerial.getActivityFromId(1);
		assertEquals("Equal Activities", 0, ac.compare(a1, a1));

		// ordered in serial process
		Activity a2 = procSerial.getActivityFromId(2);
		Activity a3 = procSerial.getActivityFromId(3);
		assertEquals("Ordered in Serial Process", -1, ac.compare(a1, a2));
		assertEquals("Ordered in Serial Process (reverse)", 1, ac.compare(a2,
				a1));
		assertEquals("Ordered in Serial Process", -1, ac.compare(a1, a3));
		assertEquals("Ordered in Serial Process (reverse)", 1, ac.compare(a3,
				a1));
		assertEquals("Ordered in Serial Process", -1, ac.compare(a2, a3));
		assertEquals("Ordered in Serial Process (reverse)", 1, ac.compare(a3,
				a2));

		// unordered
		ac = new ActivityComparator(procParallel.getHappensBeforeMap());
		a1 = procParallel.getActivityFromId(1);
		a2 = procParallel.getActivityFromId(2);
		a3 = procParallel.getActivityFromId(3);
		assertEquals("Unordered in Parallel Process", 0, ac.compare(a1, a2));
		assertEquals("Unordered in Parallel Process", 0, ac.compare(a2, a3));
		assertEquals("Unordered in Parallel Process", 0, ac.compare(a1, a3));
		ac = new ActivityComparator(procForkJoin.getHappensBeforeMap());
		a1 = procForkJoin.getActivityFromId(1);
		a2 = procForkJoin.getActivityFromId(2);
		a3 = procForkJoin.getActivityFromId(3);
		Activity a4 = procForkJoin.getActivityFromId(4);
		assertEquals("Unordered in Forked Process", 0, ac.compare(a2, a3));
		assertEquals("Unordered in Forked Process", 0, ac.compare(a3, a2));
		assertFalse("Unordered in Forked Process", ac.compare(a1, a2) == 0);
		assertFalse("Unordered in Forked Process", ac.compare(a3, a4) == 0);

		// ordered in mixed process
		ac = new ActivityComparator(procForkJoin.getHappensBeforeMap());
		a1 = procForkJoin.getActivityFromId(1);
		a2 = procForkJoin.getActivityFromId(2);
		a3 = procForkJoin.getActivityFromId(3);
		a4 = procForkJoin.getActivityFromId(4);
		assertEquals("Ordered in Forked Process", -1, ac.compare(a1, a2));
		assertEquals("Ordered in Forked Process (reverse)", 1, ac.compare(a2,
				a1));
		assertEquals("Ordered in Forked Process", -1, ac.compare(a2, a4));
		assertEquals("Ordered in Forked Process (reverse)", 1, ac.compare(a4,
				a1));
		assertEquals("Ordered in Forked Process", -1, ac.compare(a1, a4));
		assertEquals("Ordered in Forked Process (reverse)", 1, ac.compare(a4,
				a1));

		ac = new ActivityComparator(procBackToBackForkJoin
				.getHappensBeforeMap());
		a1 = procBackToBackForkJoin.getActivityFromId(1);
		a2 = procBackToBackForkJoin.getActivityFromId(2);
		a4 = procBackToBackForkJoin.getActivityFromId(4);
		Activity a6 = procBackToBackForkJoin.getActivityFromId(6);
		assertEquals("Ordered in 2 Separate Forks", -1, ac.compare(a2, a4));
		assertEquals("Ordered in 2 Separate Forks (reverse)", 1, ac.compare(a4,
				a2));
		assertEquals("Ordered in 2 Separate Forks", -1, ac.compare(a1, a4));
		assertEquals("Ordered in 2 Separate Forks (reverse)", 1, ac.compare(a4,
				a1));
		assertEquals("Ordered in 2 Separate Forks", -1, ac.compare(a4, a6));
		assertEquals("Ordered in 2 Separate Forks (reverse)", 1, ac.compare(a6,
				a4));
		assertEquals("Ordered in 2 Separate Forks", -1, ac.compare(a2, a6));
		assertEquals("Ordered in 2 Separate Forks (reverse)", 1, ac.compare(a6,
				a2));

	}

}
