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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;

/**
 * A set of bindings for a process that represent a possible solution. This
 * class represents a solution before it has been evaluated and assigned a
 * score.
 * 
 * @author reblace
 * 
 */
public class Solution {

	private static final long serialVersionUID = 4979898193862136122L;
	private RnrmProcess process;
	private Map<Integer, Binding> boundActs = new HashMap<Integer, Binding>();
	//private Collection<Binding> boundActs = new ArrayList<Binding>();
	private HashMap<Integer, Binding> actIdToBinding = new HashMap<Integer, Binding>();

	/**
	 * Create a new solution copying from the provided solution
	 * 
	 * @param solution
	 *            The solution to copy
	 */
	public Solution(Solution solution) {
		this.process = solution.process;
		this.boundActs = solution.boundActs;
		this.actIdToBinding = solution.actIdToBinding;
	}

	/**
	 * Create a new solution for the provided {@link RnrmProcess}
	 * 
	 * @param process
	 *            The {@link RnrmProcess} for this solution
	 */
	public Solution(RnrmProcess process) {
		this.process = process;
		for (Activity a : this.process.getOrderedActivities()) {
			this.actIdToBinding.put(a.getID(), new Binding(a));
		}
	}

	/**
	 * @return A live copy of the activity id to binding map
	 */
	public HashMap<Integer, Binding> getActivityToBindingMap() {
		return this.actIdToBinding;
	}

	/**
	 * @return A live copy of the collection of non-null bindings
	 */
	public Collection<Binding> getNonNullBindings() {
		return boundActs.values();
	}

	/**
	 * @return A copy of the collection of {@link IObservation} objects
	 *         referenced in bindings in this solution
	 */
	public Collection<IObservation> getObservations() {
		ArrayList<IObservation> ret = new ArrayList<IObservation>(this.boundActs
				.size());
		for (IBinding b : boundActs.values()) {
			ret.add(b.getObservation());
		}
		return ret;
	}

	/**
	 * @return A copy of the ordered list of {@link Binding} objects
	 */
	public List<Binding> getOrderedBindings() {
		List<Binding> out = new ArrayList<Binding>(this.actIdToBinding.size());
		for (Activity a : this.process.getOrderedActivities()) {
			out.add(this.actIdToBinding.get(a.getID()));
		}
		return out;
	}

	/**
	 * @return The number of activities in the process for this solution
	 */
	public int size() {
		return this.actIdToBinding.size();
	}

	/**
	 * @return A live list of {@link IBinding} objects for this solution
	 */
	public Collection<Binding> getBindings() {
		return this.actIdToBinding.values();
	}

	/**
	 * @return The {@link RnrmProcess} for this solution
	 */
	public RnrmProcess getProcess() {
		return this.process;
	}

	/**
	 * @param a
	 *            The {@link Activity} of interest
	 * @return The {@link Binding} for the provided {@link Activity}
	 */
	public Binding getBinding(Activity a) {
		return this.actIdToBinding.get(a.getID());
	}

	/**
	 * Adds the new {@link Binding} to the solution
	 * 
	 * @param ca
	 *            The binding to add
	 */
	public void addBinding(Binding ca) {
		// check that this binding is to a valid activity
		if (!actIdToBinding.containsKey(ca.getActivityID())) {
			throw new IllegalArgumentException(
					"Cannot add Binding. Associated Activity is not part of this Process.");
		}
		this.actIdToBinding.put(ca.getActivityID(), ca);
		boundActs.put(ca.getActivityID(), ca);
	}

	@Override
	public String toString() {
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter);

		final int INDEX_LABEL = 0, INDEX_ID = 1, INDEX_TIME = 2, INDEX_LATLON = 3;
		final int STRING_SIZE = 4;
		String format = "|    %1$-30s|   %2$-30s|    %3$-30s|    %4$-30s|\n";
		writer.format(format, "----ACTIVITY----", "----OBSERVABLE----",
				"----TIME STAMP----", "----LOCATION----");
		for (Activity a : this.process.getOrderedActivities()) {
			String[] out = new String[STRING_SIZE];
			out[INDEX_LABEL] = a.getLabel();
			if (this.actIdToBinding.get(a.getID()).getObservation() == null) {
				out[INDEX_ID] = "";
				out[INDEX_TIME] = "";
				out[INDEX_LATLON] = "";
			} else {
				out[INDEX_ID] = this.actIdToBinding.get(a.getID()).toString();
				out[INDEX_TIME] = this.actIdToBinding.get(a.getID())
						.getObservation().getTimestamp().toString();
				out[INDEX_LATLON] = this.actIdToBinding.get(a.getID())
						.getObservation().getLat()
						+ ", "
						+ this.actIdToBinding.get(a.getID()).getObservation()
								.getLon();
			}
			writer.format(String.format(format, (Object[]) out));
		}
		writer.close();
		return strWriter.toString();
	}

}
