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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.bbn.c2s2.pint.exception.CyclicProcessException;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.hp.hpl.jena.rdf.model.Resource;

public class PartialOrderBuilder {
	
	private class PartialOrder {
		Map<Resource, Set<Resource>> partialOrder = new HashMap<Resource, Set<Resource>>();
		
		public Set<Resource> getPriors(Resource a) {
			return this.partialOrder.get(a);
		}
		
		public boolean addPriors(Resource key, Resource prior) {
			Set<Resource> priors = this.partialOrder.get(key);
			int currentSize = priors.size();
			priors.add(prior);
			Set<Resource> parentPriors = this.partialOrder.get(prior);
			priors.addAll(parentPriors);
			int newSize = priors.size();
			return (newSize > currentSize);
		}
		
		public void addResource(Resource r) {
			this.partialOrder.put(r, new HashSet<Resource>());
		}
	}
	
	private PartialOrder partialOrder = new PartialOrder();
	private boolean hasCycle = false;
	
	public PartialOrderBuilder(RdfProcess proc)  {
		
		if (!proc.isValid()) {
			this.turnIntoUnordered(proc, 0);
			return;
		}
		
		Queue<Resource> queue = new LinkedList<Resource>();
		queue.add(proc.getStartElement());
		Set<Resource> procEls = proc.getProcessElements();
		for (Resource r : procEls) {
			this.partialOrder.addResource(r);
		}
		
		int id = 0;
		while (!queue.isEmpty()) {
			Set<Resource> visited = new HashSet<Resource>();
			Resource root = queue.remove();
			if (proc.getId(root) == null) {
				proc.setId(root, id);
				id++;
			}
			try {
				this.dfs(root, root, proc, queue, visited, false);
			} catch (CyclicProcessException e) {
				turnIntoUnordered(proc, id);
				break;
			}
		}
	}
	
	private void turnIntoUnordered(RdfProcess proc, int currentId) {
		hasCycle = true;
		Set<Resource> procEls = proc.getProcessElements();
		for (Resource r : procEls) {
			if (proc.getId(r)==null) {
				proc.setId(r, currentId);
				currentId++;
			}
		}
		
		Set<Resource> emptySet = new HashSet<Resource>();
		for (Resource r : procEls) {
			this.partialOrder.partialOrder.put(r, emptySet);
		}
	}
	
	private void dfs(Resource root, 
			Resource cycleNode,
			RdfProcess proc,
			Queue<Resource> updatedNodes, 
			Set<Resource> visited, 
			boolean checkForCycle) throws CyclicProcessException {
		
		if (checkForCycle) {
			if (root.equals(cycleNode)) throw new CyclicProcessException("Cycle Detected.");
		}
		
		List<Resource> children = proc.getChildren(root);
		for (Resource c : children) {
			boolean priorsAdded = this.partialOrder.addPriors(c, root);
			boolean beenVisited = visited.contains(c);
			if (priorsAdded && !updatedNodes.contains(c)) {
				updatedNodes.add(c);
			}
			if (!beenVisited) {
				visited.add(c);
				this.dfs(c, cycleNode, proc, updatedNodes, visited, true);
			}
		}
	}
	
	public Map<Resource, Set<Resource>> getPartialOrder() {
		return this.partialOrder.partialOrder;
	}
	
	public boolean hasCycle() {
		return this.hasCycle;
	}
}
