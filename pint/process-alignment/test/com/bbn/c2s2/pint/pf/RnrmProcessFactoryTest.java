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
import com.hp.hpl.jena.rdf.model.Resource;

public class RnrmProcessFactoryTest extends TestCase {

	private Resource procEmpty;
	private Resource procSingleActivity;
	private Resource procSerial;
	private Resource procParallel;
	private Resource procForkJoin;
	private Resource procBackToBackForkJoin;
	private Resource procAsymmetric;

	protected void setUp() throws Exception {
		super.setUp();
		procEmpty = ProcessModelFactory.createEmptyProcess();
		procSingleActivity = ProcessModelFactory.createSingleActivityProcess();
		procSerial = ProcessModelFactory.createSerialProcess();
		procParallel = ProcessModelFactory.createParallelProcess();
		procForkJoin = ProcessModelFactory.createForkedProcess();
		procBackToBackForkJoin = ProcessModelFactory
				.createBackToBackForkedProcess();
		procAsymmetric = ProcessModelFactory.createAsymmetricForkProcess();
	}

	public void testGetPartialOrderOnActivities() throws Exception {
		// test with empty process
		String[][] strAnswer = Processes.EMPTY;
		Map<String, Set<String>> answer = PartialOrderValidator
				.generateAnswer(strAnswer);
		Resource process = procEmpty;
		Map<Activity, Set<Activity>> result = null;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Empty Process%n%1$s", ipe.getMessage()));
		}
		PartialOrderValidator.validateResults("Empty Process", result, answer);

		// test with single activity process
		strAnswer = Processes.SINGLE_ACTIVITY;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procSingleActivity;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Single-Activity Process%n%1$s", ipe
					.getMessage()));
		}
		PartialOrderValidator.validateResults("Single-Activity Process",
				result, answer);

		// test with serial process
		strAnswer = Processes.SERIAL;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procSerial;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Serial Process%n%1$s", ipe.getMessage()));
		}
		PartialOrderValidator.validateResults("Serial Process", result, answer);

		// test with parallel process
		strAnswer = Processes.PARALLEL;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procParallel;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String
					.format("Parse Parallel Process%n%1$s", ipe.getMessage()));
		}
		PartialOrderValidator.validateResults("Parallel Process", result,
				answer);

		// test with fork/join process
		strAnswer = Processes.FORK_JOIN;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procForkJoin;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Fork/Join Process%n%1$s", ipe
					.getMessage()));
		}
		PartialOrderValidator.validateResults("Fork/Join Process", result,
				answer);

		// test with back-to-back fork/joins
		strAnswer = Processes.BACK_TO_BACK_FORK_JOIN;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procBackToBackForkJoin;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Back-to-Back Fork/Join Process%n%1$s",
					ipe.getMessage()));
		}
		PartialOrderValidator.validateResults("Back-to-Back Fork/Join Process",
				result, answer);

		// test with asymmetric fork process
		strAnswer = Processes.ASYMMETRIC_FORK;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = procAsymmetric;
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Asymmetric Fork Process%n%1$s", ipe
					.getMessage()));
		}
		PartialOrderValidator.validateResults("Asymmetric Fork Process",
				result, answer);

		// test with overlapping fork
		strAnswer = Processes.OVERLAPPING_FORK;
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		process = ProcessModelFactory.createOverlappingForkProcess();
		try {
			RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
			result = rp.getHappensBeforeMap();
		} catch (InvalidProcessException ipe) {
			fail(String.format("Parse Overlapping Fork Process%n%1$s", ipe
					.getMessage()));
		}
		PartialOrderValidator.validateResults("Overlapping Fork Process",
				result, answer);
	}
	
	public void testGetInversePartialOrderOnActivities() throws Exception {
		Map<Activity, Set<Activity>> happensAfter = null;
		String[][] strAnswer = null;
		Map<String, Set<String>> answer = null;

		/*
		 * ========================== Test an empty process
		 * ==========================
		 */
		Resource process = procEmpty;
		RnrmProcess rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there are no activities
		strAnswer = new String[][] {};

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Empty process", happensAfter,
				answer);

		/*
		 * =============================== Test a single activity process
		 * ===============================
		 */
		process = procSingleActivity;
		rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there is only a single activity
		// TODO: Replace these with the constants in Processes
		strAnswer = new String[][] { { "A()", "()" } };

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Single activity", happensAfter,
				answer);

		/*
		 * =============================== Test a serial process
		 * ===============================
		 */
		process = procSerial;
		rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there is only a single activity
		strAnswer = new String[][] { { "A()", "(B,C)" }, { "B()", "(C)" },
				{ "C()", "()" } };

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Serial process", happensAfter,
				answer);

		/*
		 * =============================== Test a parallel process
		 * ===============================
		 */
		process = procParallel;
		rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there is only a single activity
		strAnswer = new String[][] { { "A()", "()" }, { "B()", "()" },
				{ "C()", "()" } };

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Parallel process", happensAfter,
				answer);

		/*
		 * =============================== Test a fork join
		 * ===============================
		 */
		process = procForkJoin;
		rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there is only a single activity
		strAnswer = new String[][] { { "A()", "(B,C,D)" }, { "B()", "(D)" },
				{ "C()", "(D)" }, { "D()", "()" } };

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Fork Join process",
				happensAfter, answer);

		/*
		 * =============================== Test a Back to Back Fork Join
		 * ===============================
		 */
		process = procBackToBackForkJoin;
		rp = RnrmProcessFactory.createProcess(process.getModel(), process.getURI());
		happensAfter = RnrmProcessFactory.getInversePartialOrderOnActivities(rp.getHappensBeforeMap());

		// there is only a single activity
		strAnswer = new String[][] { { "A()", "(B,C,D,E,F)" },
				{ "B()", "(D,E,F)" }, { "C()", "(D,E,F)" }, { "D()", "(F)" },
				{ "E()", "(F)" }, { "F()", "()" } };

		// validate
		answer = PartialOrderValidator.generateAnswer(strAnswer);
		PartialOrderValidator.validateResults("Back to Back Fork Join",
				happensAfter, answer);

	}
}
