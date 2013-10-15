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
package com.bbn.c2s2.pint.testdata;

import java.util.ArrayList;
import java.util.List;

public class Processes {

	/**
	 * Process with 0 Activities
	 */
	public static final String[][] EMPTY = new String[][] {

	};

	/**
	 * Process with a single Activity with 1 observable
	 * 
	 * <pre>
	 * A
	 * </pre>
	 */
	public static final String[][] SINGLE_ACTIVITY = new String[][] { { "A(1)",
			"()" }, };

	/**
	 * Process with 3 serial Activities (no forks) and 3 unique observables
	 * 
	 * <pre>
	 * A | B | C
	 * </pre>
	 */
	public static final String[][] SERIAL = new String[][] { { "A(1)", "()" },
			{ "B(2)", "(A)" }, { "C(3)", "(A,B)" } };

	/**
	 * Process with 3 parallel Activities and unique observables
	 * 
	 * <pre>
	 *    / | \
	 *   A  B  C
	 * </pre>
	 */
	public static final String[][] PARALLEL = new String[][] {
			{ "A(1)", "()" }, { "B(2)", "()" }, { "C(3)", "()" }, };

	/**
	 * Process with with a forked path and unique obervables.
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C
	 *     \ /
	 *      D
	 * </pre>
	 */
	public static final String[][] FORK_JOIN = new String[][] {
			{ "A(1)", "()" }, 
			{ "B(2)", "(A)" }, 
			{ "C(3)", "(A)" },
			{ "D(4)", "(A,B,C)" },
		};
	
	/**
	 * Process with 6 Activities with 2 back-back fork/joins and unique
	 * observables.
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C
	 *     \ /
	 *     / \
	 *    D   E
	 *     \ /
	 *      F
	 * </pre>
	 */
	public static final String[][] BACK_TO_BACK_FORK_JOIN = new String[][] {
			{ "A(1)", "()" }, { "B(2)", "(A)" }, { "C(3)", "(A)" },
			{ "D(4)", "(A,B,C)" }, { "E(5)", "(A,B,C)" },
			{ "F(6)", "(A,B,C,D,E)" }, };

	/**
	 * Process with 6 Activities with an asymmetric forked path and unique
	 * observables
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   |
	 *    |   F
	 *    C   |
	 *     \ /
	 *      D
	 *      |
	 *      E
	 * </pre>
	 */
	public static final String[][] ASYMMETRIC_FORK = new String[][] {
			{ "A(1)", "()" }, { "B(2)", "(A)" }, { "C(3)", "(A,B)" },
			{ "D(4)", "(A,B,C,F)" }, { "E(5)", "(A,B,C,F,D)" },
			{ "F(6)", "(A)" }, };

	/**
	 * Process with 7 Activities with overlapping forks
	 * 
	 * <pre>
	 *      A
	 *      |
	 *      B
	 *     / \
	 *    |   |
	 *    G   C
	 *     \ / \
	 *      D   F
	 *      |   |
	 *       \ /
	 *        E
	 *        |
	 *        H
	 * </pre>
	 */
	public static final String[][] OVERLAPPING_FORK = new String[][] {
			{ "A(1)", "()" },
			{ "B(2)", "(A)" },
			{ "C(3)", "(A,B)" }
			,
			{ "D(4)", "(A,B,C,G)" },
			{ "E(5)", "(A,B,C,G,D,F)" },			
			{ "F(6)", "(A,B,C)" },
			{ "G(7)", "(A,B)" },
			{ "H(8)", "(A,B,C,G,D,F,E)" },
			};

	/**
	 * Process with 4 Activities in a cycle enabled through too many links on an Activity
	 * <pre>
	 *       A
	 *       |
	 *       B
	 *     / |
	 *    |  C
	 *    |_/ \
	 *         D
	 * </pre>
	 */
	public static final String[][] CYCLIC_PROCESS_A = new String[][] {
		{ "A(1)", "()" },
		{ "B(2)", "(A)" },
		{ "C(3)", "(A,B)" },
		{ "D(4)", "(A,B,C)" },
	};
	
	/**
	 * Process with 6 activities and an invalid path that jumps forks.
	 * <pre>
	 *      A
	 *      |
	 *     ---
	 *     | |
	 *     B  \
	 *     |   \
	 *     --- |
	 *      |  |
	 *      C  F
	 *      |  |
	 *     --- |
	 *     |   |
	 *     D  /
	 *     | |
	 *     ---
	 *      |
	 *      E
	 * </pre>

	 */
	public static final String[][] INVALID_CARDINALITY = new String[][] {
		{ "A(1)", "()" },
		{ "B(2)", "(A)" },
		{ "C(3)", "(A,B)" },
		{ "D(4)", "(A,B,C)" },
		{ "E(5)", "(A,B,C,D,F)" },
		{ "F(6)", "(A)" },
	};
	
	/**
	 * Process with 4 Activities where a cycle is enabled using fork/join
	 * 
	 * <pre>
	 *      A
	 *   /\ |
	 *  | ----
	 *  |   |
	 *  D   B
	 *  |   |
	 *  | ----
	 *   \/ |
	 *      C
	 * </pre>	 * 
	 */
	public static final String[][] CYCLIC_PROCESS_B = new String[][] {
		{ "A(1)", "()" },
		{ "B(2)", "(A)" },
		{ "C(3)", "(A,B)" },
		{ "D(4)", "()" },
	};
	
	/**
	 * Extracts the Activity label from the given activityString E.g. "A(1,2)"
	 * returns "A"
	 * 
	 * @param activityString
	 *            Example: A(1,2)
	 * @return String label for the Activity
	 */
	public static String getActivityLabel(String activityString) {
		return activityString.substring(0, activityString.indexOf('('));
	}

	/**
	 * Extracts the list of Observable strings in the provided Activity string
	 * E.g. "A(1,2)" returns {"1", "2"}
	 * 
	 * @param activityString
	 *            Example: A(1,2)
	 * @return String array with Observable labels
	 */
	public static String[] getObservablesList(String activityString) {
		String obsString = activityString
				.substring(activityString.indexOf('('));
		return getParensCommaSepList(obsString);
	}

	/**
	 * Parses a comma-separated list surrounded by parentheses E.g. (A,B,C)
	 * 
	 * @param s
	 *            String to parse, such as (A,B,C)
	 * @return Array of String values that were in the list
	 */
	public static String[] getParensCommaSepList(String s) {
		// remove opening parens
		String commaString = s.substring(1);
		// remove closing parens
		commaString = commaString.substring(0, commaString.length() - 1);
		String[] rv = commaString.split(",");
		for (int i = 0; i < rv.length; i++) {
			rv[i] = rv[i].trim();
		}
		// split will always return at least 1 string even if the input was
		// empty
		// If the result is just a single empty string, then return an empty
		// array
		if (rv.length == 1 && rv[0].length() < 1) {
			rv = new String[0];
		}
		return rv;
	}

	/**
	 * Returns all of the test Process definitions in a list.
	 * 
	 * @return List of happensBefore hashes
	 */
	public static List<String[][]> getTestProcesses() {
		ArrayList<String[][]> rv = new ArrayList<String[][]>();
		rv.add(EMPTY);
		rv.add(SINGLE_ACTIVITY);
		rv.add(SERIAL);
		rv.add(PARALLEL);
		rv.add(FORK_JOIN);
		rv.add(BACK_TO_BACK_FORK_JOIN);
		rv.add(ASYMMETRIC_FORK);
		return rv;
	}
}
