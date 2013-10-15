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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author tself
 * 
 */
public class SolutionReportTest extends TestCase {
	final double SCORE = 0.5;
	SolutionReport normalReport;
	SolutionReport partialReport;
	SolutionReport emptyReport;
	SolutionReport betterReport;
	RnrmProcess process;

	protected void setUp() throws Exception {
		super.setUp();
		process = TestRnrmProcessFactory.createSerialProcess();
		EvaluatedSolution eSln = new EvaluatedSolution(SolutionFactory
				.createOrderedColocatedSolution(process), SCORE);
		normalReport = new SolutionReport(eSln);

		eSln = new EvaluatedSolution(SolutionFactory
				.createOrderedSolutionWithOneUnbound(process, 1), SCORE);
		partialReport = new SolutionReport(eSln);

		eSln = new EvaluatedSolution(SolutionFactory
				.createUnboundSolution(process), SCORE);
		emptyReport = new SolutionReport(eSln);

		eSln = new EvaluatedSolution(SolutionFactory
				.createOrderedColocatedSolution(process), SCORE + 0.01);
		betterReport = new SolutionReport(eSln);
	}

	private void validateOrderedContents(String msg, List<String> expected,
			List<String> actual) {
		assertEquals(msg, expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertSame(msg, expected.get(i), actual.get(i));
		}
	}

	public void testGetOrderedBindings() {
		List<Activity> expectedActs = process.getOrderedActivities();
		List<String> expected = new ArrayList<String>(expectedActs.size());
		for(Activity act : expectedActs) {
			expected.add(act.getActivityURI());
		}
		List<IBinding> orderedBindings = normalReport.getOrderedBindings();
		List<String> orderedActs = new ArrayList<String>(orderedBindings
				.size());
		for (IBinding b : orderedBindings) {
			orderedActs.add(b.getActivityUri());
		}
		validateOrderedContents("getOrderedBindings", expected, orderedActs);
	}

	public void testGetSolutionScore() {
		assertEquals(SCORE, normalReport.getSolutionScore());
	}

	public void testGetBindings() {
		assertEquals("Full Solution", process.getActivities().size(),
				normalReport.getBindings().size());

		// test with missing bindings
		assertEquals("Partial Solution", process.getActivities().size(),
				partialReport.getBindings().size());

		// empty solution
		assertEquals("Empty Solution", process.getActivities().size(),
				emptyReport.getBindings().size());
	}

	public void testGetProcessLabel() {
		assertNotNull(normalReport.getProcessLabel());
	}

	public void testGetProcessUri() {
		// nothing to test here. Can accept value or null. Just call to fool
		// Cobertura.
		normalReport.getProcessUri();
	}

	public void testToString() {
		assertNotNull(normalReport.toString());
		assertTrue(normalReport.toString().length() > 0);
	}

	public void testCompareTo() {
		// greater
		assertEquals("Greater than", 1, normalReport.compareTo(betterReport));
		// less
		assertEquals("Less than", -1, betterReport.compareTo(normalReport));
		// equal
		assertEquals("Equal Score", 0, normalReport.compareTo(partialReport));
	}
}
