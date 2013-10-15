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

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.testdata.ProcessModelFactory;
import com.bbn.c2s2.pint.testdata.Processes;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class RnrmProcessTest extends TestCase {

	private RnrmProcess procEmpty;
	private RnrmProcess procSingleActivity;
	private RnrmProcess procSerial;
	private RnrmProcess procParallel;
	private RnrmProcess procForkJoin;
	private RnrmProcess procBackToBackForkJoin;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		procEmpty = TestRnrmProcessFactory.createEmptyProcess();
		procSingleActivity = TestRnrmProcessFactory.createSingleActivityProcess();
		procSerial = TestRnrmProcessFactory.createSerialProcess();
		procParallel = TestRnrmProcessFactory.createParallelProcess();
		procForkJoin = TestRnrmProcessFactory.createForkJoinProcess();
		procBackToBackForkJoin = TestRnrmProcessFactory
				.createDoubleForkJoinProcess();
	}

	public void testConstructor() {
		RnrmProcess p = new RnrmProcess();
		assertNotNull("Default Constructor", p);
	}

	public void testToString() {
		String testUri = "http://test.domain.foo/ignored#InferredLabel";
		String testLabel = "Process";
		// test with an established label
		Map<Activity, Set<Activity>> hb = TestRnrmProcessFactory
				.createHappensBeforeMapFromStringArray(Processes.SERIAL);
		RnrmProcess p = new RnrmProcess(hb, null, null, testUri, testLabel);
		assertEquals("ToString Test", testLabel, p.toString());

		// test with a URI-based label
		p = new RnrmProcess(hb, null, null, testUri, null);
		assertEquals("ToString Test", testUri, p.toString());
	}

	public void testGetLabel() {
		String testUri = "http://test.domain.foo/ignored#InferredLabel";
		String testLabel = "Process";
		// test with an established label
		Map<Activity, Set<Activity>> hb = TestRnrmProcessFactory
				.createHappensBeforeMapFromStringArray(Processes.SERIAL);
		RnrmProcess p = new RnrmProcess(hb, null, null, testUri, testLabel);
		assertEquals("getLabel Test", testLabel, p.getLabel());

		// test with a URI-based label
		p = new RnrmProcess(hb, null, null, testUri, null);
		assertNull("getLabel Test", p.getLabel());

	}

	public void testIsOrderConstrained() {
		assertFalse("Empty Process", procEmpty.isOrderConstrained());
		assertFalse("Single-Activity Process", procSingleActivity
				.isOrderConstrained());
		assertTrue("Serial Process", procSerial.isOrderConstrained());
		assertTrue("Forked Process", procForkJoin.isOrderConstrained());
	}

	public void testGetActivityFromId() {
		assertNull("Empty Process", procEmpty.getActivityFromId(1));
		Activity a = procForkJoin.getActivityFromId(1);
		assertEquals("First Activity", 1, a.getID());
		a = procForkJoin.getActivityFromId(4);
		assertEquals("Last Activity", 4, a.getID());
		assertNull("Non-existent Activity", procForkJoin.getActivityFromId(25));
	}

	public void testGetActivities() {
		// empty process
		String[] answers = new String[] {};
		ActivityValidator.validateUnorderedContents("Empty Process", answers,
				procEmpty.getActivities());

		// single-activity process
		answers = new String[] { "A" };
		ActivityValidator.validateUnorderedContents("Single-Activity Process",
				answers, procSingleActivity.getActivities());

		// multi-activity process
		answers = new String[] { "A", "B", "C", "D", };
		ActivityValidator.validateUnorderedContents("Multi-Activity Process",
				answers, procForkJoin.getActivities());
	}

	public void testGetOrderedActivities() {
		// serial process - only 1 possible order
		String[][] answers = new String[][] { { "A", "B", "C" }, };
		ActivityValidator.validateOrderedContents("Serial Process", answers,
				procSerial.getOrderedActivities());

		// single fork/join process - 2 possible orders
		answers = new String[][] { { "A", "B", "C", "D" },
				{ "A", "C", "B", "D" }, };
		ActivityValidator.validateOrderedContents("Single Fork/Join Process",
				answers, procForkJoin.getOrderedActivities());

		// multi fork/join process - 4 possible orders
		answers = new String[][] { { "A", "B", "C", "D", "E", "F" },
				{ "A", "B", "C", "E", "D", "F" },
				{ "A", "C", "B", "D", "E", "F" },
				{ "A", "C", "B", "E", "D", "F" }, };
		ActivityValidator.validateOrderedContents("Multi Fork/Join Process",
				answers, procBackToBackForkJoin.getOrderedActivities());
	}

	public void testGetHappensBeforeMap() {
		Map<Activity, Set<Activity>> map;
		map = procEmpty.getHappensBeforeMap();
		assertEquals("Empty Process", 0, map.size());

		map = procSerial.getHappensBeforeMap();
		assertEquals("3-Activity Process", 3, map.size());
	}

	public void testGetHappensBefore() {
		// test with first activity
		Activity act = procSerial.getActivityFromId(1);
		Set<Activity> result = procSerial.getHappensBefore(act);
		assertEquals("First Activity", 0, result.size());

		// test with last
		act = procBackToBackForkJoin.getActivityFromId(6);
		result = procBackToBackForkJoin.getHappensBefore(act);
		assertEquals("Last Activity", 5, result.size());

		// test with non-existent activity
		result = procForkJoin.getHappensBefore(act);
		assertNull("Non-existent Activity", result);
	}

	public void testGetHappensAfter() {
		// test with first activity
		Activity act = procBackToBackForkJoin.getActivityFromId(1);
		Set<Activity> result = procBackToBackForkJoin.getHappensAfter(act);
		assertEquals("First Activity", 5, result.size());

		// test with last
		act = procBackToBackForkJoin.getActivityFromId(6);
		result = procBackToBackForkJoin.getHappensAfter(act);
		assertEquals("Last Activity", 0, result.size());

		// test with non-existent activity
		result = procForkJoin.getHappensAfter(act);
		assertNull("Non-existent Activity", result);
	}

	public void testSize() {
		assertEquals(0, procEmpty.size());
		assertEquals(1, procSingleActivity.size());
		assertEquals(3, procSerial.size());
		assertEquals(3, procParallel.size());
		assertEquals(4, procForkJoin.size());
		assertEquals(6, procBackToBackForkJoin.size());
	}

	public void testGet() {
		// test first
		Activity act = procBackToBackForkJoin.get(0);
		assertEquals("First Activity", 1, act.getID());

		// test last
		act = procBackToBackForkJoin.get(5);
		assertEquals("Last Activity", 6, act.getID());
	}

	public void testCreateProcess() throws Exception {
		// this method calls directly into ProcessExtractor, which
		// is already tested heavily. Just test a couple models
		// and make sure the labels work

		// empty process
		Map<String, Set<String>> answer;
		Resource procResource = ProcessModelFactory.createEmptyProcess();
		RnrmProcess result = null;
		try {
			result = RnrmProcessFactory.createProcess(procResource.getModel(),
					procResource.getURI());
		} catch (InvalidProcessException ipe) {
			fail(String.format("Create Empty Process%n%1$s", ipe.getMessage()));
		}
		answer = PartialOrderValidator.generateAnswer(Processes.EMPTY);
		PartialOrderValidator.validateResults("Empty Process", result
				.getHappensBeforeMap(), answer);
		assertTrue("Process Label", result.getLabel().startsWith("Process "));

		// forked
		procResource = ProcessModelFactory.createForkedProcess();
		answer = PartialOrderValidator.generateAnswer(Processes.FORK_JOIN);
		try {
			result = RnrmProcessFactory.createProcess(procResource.getModel(),
					procResource.getURI());
		} catch (InvalidProcessException ipe) {
			fail(String.format("Create Forked Process%n%1$s", ipe.getMessage()));
		}
		PartialOrderValidator.validateResults("Forked Process", result
				.getHappensBeforeMap(), answer);
		assertTrue("Process Label", result.getLabel().startsWith("Process"));

		// test non-existent URI
		// Should still create a process with 0 activities
		try {
			result = RnrmProcessFactory.createProcess(procResource.getModel(),
					"http://does.not.exist#seriously");
		} catch (InvalidProcessException ipe) {
			fail("Bad URI caused InvalidProcessException when it shouldn't have.");
		}
		assertEquals("Non-existent Process", 0, result.getActivities().size());
	}
}
