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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;

public class TestRnrmProcessFactory {

	protected static final String PROCESS_URI = "http://c2s2.bbn.com/test#process";
	public static final String PROCESS_LABEL = "Process";
	protected static final String ACTIVITY_URI = "http://c2s2.bbn.com/test#activity";
	protected static int count = 0;

	public static RnrmProcess createEmptyProcess() {
		return createProcessFromStringArray(Processes.EMPTY);
	}
	
	/**
	 * Creates a Process with a single {@link Activity} attached A
	 * 
	 * @return {@link RnrmProcess} for a single-activity Process
	 */
	public static RnrmProcess createSingleActivityProcess() {
		return createProcessFromStringArray(Processes.SINGLE_ACTIVITY);
	}

	/**
	 * Create a process with 'size' activities that occur serially. 
	 * The activity IDs are ones indexed and go in order from 1 to size.
	 * @param size The size of the process
	 * @return The {@link RnrmProcess}
	 */
	public static RnrmProcess createSerialProcess(int size){
		List<String[]> processStr = new LinkedList<String[]>();
		String currList = ""; 
		for(int i=1; i<=size; i++){
			processStr.add(new String[]{
					i + "(" + i + ")", 
					"(" + currList + ")"});
			
			if(currList.length() == 0){
				currList = "" + (i);
			}else{
				currList = currList + ", " + (i) ;
			}
		}
		
		return createProcessFromStringArray(
				processStr.toArray(new String[size][2]));
	}
	
	/**
	 * Creates a Process that contains 3 {@link Activity} that can occur in
	 * parallel.
	 * 
	 * <pre>
	 *    / | \
	 *   A  B  C
	 * </pre>
	 * 
	 * @return {@link RnrmProcess} for the created Process
	 */
	public static RnrmProcess createParallelProcess() {
		return createProcessFromStringArray(Processes.PARALLEL);
	}

	/**
	 * Creates a Process with a serial list of processes (no forks)
	 * 
	 * <pre>
	 * A | B | C
	 * </pre>
	 * 
	 * @return {@link RnrmProcess} for a serial Process
	 */
	public static RnrmProcess createSerialProcess() {
		return createProcessFromStringArray(Processes.SERIAL);
	}

	/**
	 * Creates a Process with 4 {@link Activity} where 2 can occur in parallel
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C
	 *     \ /
	 *      D
	 * </pre>
	 * 
	 * @return {@link RnrmProcess} for the created Process
	 */
	public static RnrmProcess createForkJoinProcess() {
		return createProcessFromStringArray(Processes.FORK_JOIN);
	}

	/**
	 * Creates an {@link RnrmProcess} with asymmetric forked paths
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
	 * 
	 * @return
	 */
	public static RnrmProcess createAsymmetricForkedProcess() {
		return createProcessFromStringArray(Processes.ASYMMETRIC_FORK);
	}

	/**
	 * Creates a Process with 6 {@link Activity} with 2 back-back fork/joins
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
	 * 
	 * @return {@link RnrmProcess} for a single-activity Process
	 */
	public static RnrmProcess createDoubleForkJoinProcess() {
		return createProcessFromStringArray(Processes.BACK_TO_BACK_FORK_JOIN);

	}

	public static Map<Activity, Set<Activity>> createHappensBeforeMapFromStringArray(
			String[][] processString) {
		// create a happensBefore map
		Map<Activity, Set<Activity>> hb = new HashMap<Activity, Set<Activity>>(
				processString.length);

		// for each row of strings, create an Activity for the keys
		Map<String, Activity> acts = new HashMap<String, Activity>(
				processString.length);
		for (int i = 0; i < processString.length; i++) {
			String label = Processes.getActivityLabel(processString[i][0]);
			Activity a = new Activity(i + 1, ACTIVITY_URI + label, label);
			acts.put(label, a);
		}

		// for each row of strings, create the set of priors and add to the map
		for (int i = 0; i < processString.length; i++) {
			Activity key = acts.get(Processes
					.getActivityLabel(processString[i][0]));
			Set<Activity> priors = new HashSet<Activity>();
			String[] priorLabels = Processes
					.getParensCommaSepList(processString[i][1]);
			for (int j = 0; j < priorLabels.length; j++) {
				Activity a = acts.get(priorLabels[j]);
				priors.add(a);
			}
			hb.put(key, priors);
		}
		return hb;
	}

	public static RnrmProcess createProcessFromStringArray(
			String[][] processString) {
		RnrmProcess rv = null;
		Map<Activity, Set<Activity>> hb = createHappensBeforeMapFromStringArray(processString);
		Map<Activity, Set<Activity>> ha = RnrmProcessFactory.getInversePartialOrderOnActivities(hb);
		Map<Activity, Set<Activity>> op = new HashMap<Activity, Set<Activity>>();
		rv = new RnrmProcess(hb, ha, op, PROCESS_URI, PROCESS_LABEL + " " + count++);

		return rv;
	}
}
