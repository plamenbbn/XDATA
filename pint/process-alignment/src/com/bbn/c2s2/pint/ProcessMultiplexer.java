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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Matches subsets of the given Observations to the RnrmProcess objects that
 * they might bind with.
 * 
 * @author tself
 * 
 */
public class ProcessMultiplexer {

	protected RnrmWrapper rnrm;
	protected boolean initialized = false;
	protected static Logger logger = LoggerFactory
			.getLogger(ProcessMultiplexer.class);

	public ProcessMultiplexer(Model rnrmModel) {
		rnrm = new RnrmWrapper(rnrmModel);
	}

	/**
	 * Initializes the RNRM by cleaning it and parsing each Process
	 * 
	 * @throws Exception
	 */

	public void initialize() {
		// force the RnrmWrapper to clean and parse the model
		rnrm.getProcesses();
		initialized = true;
	}

	/**
	 * Creates a set of bindings wrapped in a ProcessAssignment.
	 * 
	 * @param observations
	 * @return
	 * @throws Exception
	 */

	public Collection<ProcessAssignment> getProcessAssignments(
			Collection<Observation> observations) {
		if (!initialized) {
			initialize();
		}

		List<ProcessAssignment> rv = new ArrayList<ProcessAssignment>();

		List<RnrmProcess> processes = rnrm.getProcesses();
		for (RnrmProcess p : processes) {
			BindingGroup candidates = new BindingGroup(BindingGroup
					.createBindingList(p, observations, rnrm.getRdfProcess(p.getProcessUri())));
			if (candidates.bindingCount() > 0) {
				rv.add(new ProcessAssignment(p, candidates));
			}
		}

		return rv;
	}

	/**
	 * Returns the list of RNRM Processes extracted
	 * 
	 * @return List of RNRMProcess objects; empty if there are none
	 */
	public List<RnrmProcess> getProcesses() {
		return rnrm.getProcesses();
	}
}
