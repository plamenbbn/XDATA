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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.Observation;

/**
 * @author reblace
 *
 */
public class BindingGroupFactory {
	
	public static final String ACTIVITY_URI = "http://c2s2.bbn.com/test#activity";
	public static final String OBSERVATION_URI = "http://c2s2.bbn.com/test#observation";
	public static final String OBSERVABLE_URI = "http://c2s2.bbn.com/test#observable";
	private int count = 0;

	/*
	 * {@link Binding} sets where each array of numbers specifies the 
	 * {@link Observable} set that indicates an activity. The first element 
	 * in each array is the {@link Activity} ID that is indicated 
	 * by the rest of the {@link Observable} IDs
	 */
	private final int[][] bindingInts = { 
			{ 1, 1, 2, 3, 4 }, 
			{ 2, 5, 6, 7, 8 },
			{ 3, 1, 2, 3, 4, 5, 6, 7, 8 }, };
	
	private final int[][] bindingFullOverlap = { 
			{ 1, 1, 2, 3, 4, 5, 6, 7, 8 }, 
			{ 2, 1, 2, 3, 4, 5, 6, 7, 8 },
			{ 3, 1, 2, 3, 4, 5, 6, 7, 8 }, };
	
	private final int[][] bindingNoOverlap = { 
			{ 1, 1, 2, 3 }, 
			{ 2, 4, 5, 6 },
			{ 3, 7, 8 }, };	
	
	/**
	 * Generate a list of {@link Binding} objects with the provided characteristics
	 * and based on a simple {@link Activity} to {@link Observable} map 
	 * involving two activities each with a set of 4 disjoint observables and
	 * a third activity with the union of the first two sets of observables
	 * @param latIncrement The latitude space between subsequent 
	 * {@link Observation} instances
	 * @param timeIncrement The temporal space between subsequent
	 * {@link Observation} instances (in ms)
	 * @return
	 */
	public List<Binding> generateBindings(double latIncrement, long timeIncrement){
		return generateBindings(bindingInts, latIncrement, timeIncrement);
	}
	
	/**
	 * Generates a simple list of {@link Binding} objects where {@link Activity} 1 
	 * and {@link Activity} 2 have no common {@link Observable} objects, but both 
	 * have all {@link Observable} in common with {@link Activity} 3 
	 * @return The binding list satisfying the conditions described
	 */
	public List<Binding> generateBindings(){
		return generateBindings(bindingInts);
	}
	
	/**
	 * Generates a list of {@link Binding} objects where {@link Activity} 1, 
	 * {@link Activity} 2, and {@link Activity} 3 share all of the same 
	 * indicating {@link Observable} objects 
	 * @return The binding list satisfying the conditions described
	 */
	public List<Binding> generateBindingsFullOverlap(){
		return generateBindings(bindingFullOverlap);
	}
	
	/**
	 * Generates a list of {@link Binding} objects where {@link Activity} 1, 
	 * {@link Activity} 2, and {@link Activity} 3 share none of the same 
	 * indicating {@link Observable} objects 
	 * @return The binding list satisfying the conditions described
	 */
	public List<Binding> generateBindingsNoOverlap(){
		return generateBindings(bindingNoOverlap);
	}
	
	/**
	 * Generate the {@link Binding} set that goes with the int array 
	 * that is passed in
	 * @param bindingInts The specification of the bindings map
	 * @return The list of {@link Binding} objects
	 */
	protected List<Binding> generateBindings(int[][] bindingInts){
		return generateBindings(bindingInts, 0, 0);
	}
	
	/**
	 * Generate the bindings that go with the int array that is passed in
	 * Increment the latitude by the passed in value, and the time by the 
	 * same to create a process spread across time and space
	 * @param bindingInts The specification of the bindings map 
	 * @param latIncrement how much to increment the lat for each binding
	 * @param timeIncrement how much to increment the time
	 * @return
	 */
	protected List<Binding> generateBindings(int[][] bindingInts,
			double latIncrement, long timeIncrement){
		Calendar now = Calendar.getInstance();
		double lat = 0.0;
		double lon = 0.0;
		
		List<Binding> bindings = new ArrayList<Binding>();
		for (int i = 0; i < bindingInts.length; i++) {
			int actID = bindingInts[i][0];
			for (int j = 1; j < bindingInts[i].length; j++) {
				bindings.add(createBinding(actID, bindingInts[i][j], count++,
						lat, lon, now.getTime()));
				lat += latIncrement;
				now.setTimeInMillis(now.getTimeInMillis() + timeIncrement);
			}
		}
		return bindings;
	}

	/**
	 * Creates a binding with the provided characteristics
	 * @param actID The {@link Activity} id that the {@link Observable} 
	 * for this binding indicates
	 * @param obsID The {@link Observable} ID
	 * @param count An integer used to generate the unique {@link Observable} URI
	 * @param lat the latitude value of the {@link Observation}
	 * @param lon the longitude value of the {@link Observation}
	 * @param time the time of the {@link Observation} for the binding
	 * @return A new {@link Binding} meeting the specified criteria
	 */
	public Binding createBinding(int actID, int obsID, int count,
			double lat, double lon, Date time) {
		Activity a = new Activity(actID, ACTIVITY_URI + count, "Activity"
				+ count);
		Observation o = new Observation(OBSERVATION_URI + count, OBSERVABLE_URI
				+ count, lat, lon, time);
		return new Binding(a, o, obsID);
	}
}
