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
import java.util.Properties;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author tself
 * 
 */
public class ProcessFinderTest extends TestCase {

	private RnrmProcess testProcess;
	private BindingGroup testBg;
	private PintConfiguration testConfig;

	protected void setUp() throws Exception {
		super.setUp();
		testConfig = new PintConfiguration(new Properties());
		testProcess = TestRnrmProcessFactory.createSerialProcess();
		testBg = new BindingGroup(SolutionFactory
				.createOrderedColocatedSolution(testProcess).getBindings());
	}

	public void testProcessFinder() {
		// valid params
		ProcessFinder pf = null;
		try {
			pf = new ProcessFinder(testProcess, testBg, testConfig);
		} catch (InvalidProcessException ipe) {
			fail(String.format(
					"Exception occurred during valid constructor test.%n%1$s",
					ipe.getMessage()));
		}
		assertNotNull("Valid Constructor Params", pf);

		// null process
		boolean caught = false;
		try {
			pf = new ProcessFinder(null, testBg, testConfig);
		} catch (InvalidProcessException ipe) {
			caught = true;
		}
		assertTrue("Invalid Process Exception Test", caught);

		// null bindings (should work)
		caught = false;
		try {
			pf = new ProcessFinder(testProcess, null, testConfig);
		} catch (InvalidProcessException ipe) {
			caught = true;
		}
		assertFalse("Null BindingGroup", caught);
	}

	public void testFind() {
		ProcessFinder pf = null;
		try {
			pf = new ProcessFinder(testProcess, testBg, testConfig);
		} catch (InvalidProcessException ipe) {
			fail(String.format(
					"Exception occurred during valid constructor.%n%1$s", ipe
							.getMessage()));
		}
		List<SolutionReport> reports = pf.find();
		assertNotNull("Reports not null", reports);
		assertTrue("Reports not empty", reports.size() > 0);
	}

}
