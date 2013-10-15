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

import junit.framework.TestCase;

import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

public class EvaluatedSolutionTest extends TestCase {

	private Solution sln;
	private final double A = 0.5;
	private final double B = 0.8;
	private EvaluatedSolution evalSolutionA;
	private EvaluatedSolution evalSolutionA2;
	private EvaluatedSolution evalSolutionB;

	protected void setUp() throws Exception {
		super.setUp();
		sln = SolutionFactory.createOrderedColocatedSolution(TestRnrmProcessFactory
				.createSerialProcess());
		evalSolutionA = new EvaluatedSolution(sln, A);
		evalSolutionA2 = new EvaluatedSolution(sln, A);
		evalSolutionB = new EvaluatedSolution(sln, B);
	}

	public void testEvaluatedSolution() {
		// test construction
		EvaluatedSolution es = null;
		es = new EvaluatedSolution(sln, 0.5);
		assertNotNull("EvaluatedSolution Constructor", es);

		// test invalid construction
		boolean caught = false;
		try {
			es = new EvaluatedSolution(sln, -0.5);
		} catch (IllegalArgumentException isse) {
			caught = true;
		}
		assertTrue("Exception Test", caught);
	}

	public void testGetScore() {
		assertEquals("getScore", A, evalSolutionA.getScore());
		assertEquals("getScore", B, evalSolutionB.getScore());
	}

	public void testCompareTo() {
		// equal
		assertEquals("Equal scores", 0, evalSolutionA.compareTo(evalSolutionA2));

		// less
		assertEquals("Lower score", -1, evalSolutionB.compareTo(evalSolutionA));

		// greater
		assertEquals("Higher score", 1, evalSolutionA.compareTo(evalSolutionB));
	}

}
