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

package com.bbn.c2s2.pint.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.pf.PartialOrderBuilder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Wraps a Jena Model holding a Red Nodal Reference Model and provides access to
 * commonly used components.
 * 
 * @author tself
 * 
 */
public class RnrmWrapper {
	private Model model;
	private List<Resource> processResources;
	private Map<Resource, RdfProcess> rdfProcesses;
	private List<RnrmProcess> processes;
	private static Logger logger = LoggerFactory.getLogger(RnrmWrapper.class);

	public RnrmWrapper(Model m) {
		model = m;
	}

	public Model getModel() {
		return model;
	}

	public List<Resource> getProcessResources() {
		if (null == processResources) {
			processResources = new ArrayList<Resource>();
			StmtIterator it = model.listStatements((Resource) null, RDF.type,
					RNRM.Process);
			while (it.hasNext()) {
				Resource p = it.nextStatement().getSubject();
				processResources.add(p);
			}
		}
		return processResources;
	}
	
	public Collection<RdfProcess> getRdfProcesses() {
		if(null == rdfProcesses) {
			List<Resource> procResources = getProcessResources();
			rdfProcesses = new HashMap<Resource, RdfProcess>(procResources.size());
			for(Resource pr : procResources) {
				rdfProcesses.put(pr, RdfProcess.create(pr));
			}
		}
		return rdfProcesses.values();
	}

	public RdfProcess getRdfProcess(String processUri) {
		Resource r = model.createResource(processUri);
		return getRdfProcess(r);
	}
	
	public RdfProcess getRdfProcess(Resource procResource) {
		if(null == rdfProcesses) {
			getRdfProcesses();
		}
		return rdfProcesses.get(procResource);
	}
	
	public List<RnrmProcess> getProcesses() {
		if (null == processes) {
			processes = loadProcesses();
		}
		return processes;
	}

	private List<RnrmProcess> loadProcesses() {
		List<RnrmProcess> rv = new ArrayList<RnrmProcess>();
		List<Resource> procResources = getProcessResources();
		for (Resource pResource : procResources) {

			RdfProcess rdfProc = RdfProcess.create(pResource);
			if (rdfProc.hasAnyObservables()) {
				RnrmProcess proc = null;
				try {
					proc = RnrmProcessFactory.createProcess(rdfProc);
				} catch (InvalidProcessException ipe) {
					logger
							.warn(
									String
											.format(
													"Encountered an invalid Process (%1$s) while multiplexing.",
													RDFHelper.getLabel(model,
															pResource.getURI())),
									ipe);
				} catch (StackOverflowError soe) {
					logger
							.warn(
									String
											.format(
													"Encountered a cycle in Process %1$s. Removing order and trying again.",
													RDFHelper.getLabel(model,
															pResource.getURI())),
									soe);
				} catch (Exception e) {
					logger.warn("Unexpected exception caught while creating RnrmProcess object.", e);
				}

				// This is where I would normally filter out processes that do
				// not imply
				// a partial order. But now that's not guaranteed. I'll just
				// make sure they
				// have at least 2 activities instead.
				if (proc.getActivities().size() > 1) {
					rv.add(proc);
				} else {
					logger
							.warn(String
									.format(
											"Ignoring process (%1$s) because it has less than 2 Activities",
											proc.getLabel()));
				}
			}
		}
		return rv;
	}
}
