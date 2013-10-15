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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.ActivityComparator;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Traverses a Red Nodal Process represented in RDF to build the happens-before
 * map that represents the partial-order of {@link Activity} objects imposed by
 * the process.
 * 
 * @author tself
 * 
 */
public class RnrmProcessFactory {
	protected static Logger logger = LoggerFactory
			.getLogger(RnrmProcessFactory.class);
	
	
	public static RnrmProcess createProcess(Model m, String processUri) throws Exception {
		RdfProcess rdfProc = RdfProcess.create((RDFHelper.getResource(m, processUri)));
		return createProcess(rdfProc);
	}

	/**
	 * Creates an {@link RnrmProcess} from an RDF-encoded process that is
	 * contained in the provided model and identified by the provided Uri
	 * 
	 * @param rnrmModel
	 *            A {@link Model} containing the process
	 * @param processUri
	 *            The Uri of the process
	 * @return
	 * @throws Exception 
	 */
	public static RnrmProcess createProcess(RdfProcess proc)
			throws Exception {
		RnrmProcess rv = null;
		PartialOrderBuilder pob = new PartialOrderBuilder(proc);
		Map<Resource, Set<Resource>> partialOrder = pob.getPartialOrder();
		Map<Activity, Set<Activity>> happensBeforeMap = getPartialOrderOnActivities(partialOrder, proc);
		Map<Activity, Set<Activity>> happensAfter = getInversePartialOrderOnActivities(happensBeforeMap);
		Map<Activity, Set<Activity>> opposes = getActivityOppositionMap(proc, happensBeforeMap.keySet(), partialOrder);
		String processUri = proc.getProcessUri();
		String label = proc.getLabel();
		rv = new RnrmProcess(happensBeforeMap, happensAfter, opposes, processUri, label);
		return rv;
	}



	/**
	 * Extracts a happensBefore map of Activities from the happensBefore map of
	 * Process Element resources
	 * 
	 * <pre>
	 * Example happensBefore Map:
	 * Activity | Prior Activities
	 *        A | []
	 *        B | [A]
	 *        C | [A, B]
	 * </pre>
	 * 
	 * @param partialOrderMap
	 *            Map of process element to prior process elements
	 * @return happensBefore map of Activity to prior Activities
	 */
	protected static Map<Activity, Set<Activity>> getPartialOrderOnActivities(
			Map<Resource, Set<Resource>> partialOrderMap, RdfProcess proc)
			throws InvalidProcessException {
		Map<Activity, Set<Activity>> rv = new HashMap<Activity, Set<Activity>>();
		// get the partial order of process nodes
		// create Activity objects for each ActivityElement
		Map<Resource, Activity> actElemToActMap = new HashMap<Resource, Activity>();
		for (Resource res : partialOrderMap.keySet()) {
			Resource actResource = proc.getActivity(res);
			if (actResource != null) {
				//System.err.println(res.getURI());
				//System.err.println(proc.getId(res));
				Activity newAct = new Activity(proc.getId(res), actResource.getURI(), proc.getElementLabel(actResource));
				actElemToActMap.put(res, newAct);
			}
		}
		// create a new map with just Activity objects
		for (Resource actElem : actElemToActMap.keySet()) {
			Activity currentAct = actElemToActMap.get(actElem);
			Set<Activity> priorActs = new HashSet<Activity>();
			Set<Resource> priors = partialOrderMap.get(actElem);
			for (Resource prior : priors) {
				Activity priorAct = actElemToActMap.get(prior);
				if (null != priorAct) {
					priorActs.add(priorAct);
				}
			}
			rv.put(currentAct, priorActs);
		}
		return rv;
	}
	
	/**
	 * Creates the happens after map from a happens before map. This basically
	 * just creates a relationship map that is the complement of the input map.
	 * For all inputs and outputs, 'before' and 'after' simply means '!after'
	 * '!before'. ie. unordered or before
	 * 
	 * @param happensBefore
	 *            A map of activities to the set of activities that occur before
	 *            that activity
	 * @return A map of activities to the set of activities that occur after
	 *         that activity.
	 */
	public static Map<Activity, Set<Activity>> getInversePartialOrderOnActivities(
			Map<Activity, Set<Activity>> happensBefore) {
		Map<Activity, Set<Activity>> happensAfter = new HashMap<Activity, Set<Activity>>();
		List<Activity> activities = new ArrayList<Activity>(happensBefore
				.keySet());
		ActivityComparator a = new ActivityComparator(happensBefore);
		Collections.sort(activities, a);

		for (Activity target : activities) {
			Set<Activity> afterTarget = new HashSet<Activity>();
			happensAfter.put(target, afterTarget);

			for (Activity check : activities) {
				if (happensBefore.get(check).contains(target)) {
					afterTarget.add(check);
				}
			}
		}

		return happensAfter;
	}
	

	private class OpposesPair {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;

			result = prime * result + ((r1 == null) ? 0 : r1.hashCode());
			result = prime * result + ((r2 == null) ? 0 : r2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			boolean rv = false;
			OpposesPair other = (OpposesPair) obj;
			if (r1.equals(other.r1) && r2.equals(other.r2)) {
				rv = true;
			}
			return rv;
		}

		private Resource r1, r2;

		public OpposesPair(Resource r1, Resource r2) {
			if (r1.getURI().compareTo(r2.getURI()) < 0) {
				this.r1 = r1;
				this.r2 = r2;
			} else if (r1.getURI().compareTo(
					r2.getURI()) > 0) {
				this.r1 = r2;
				this.r2 = r1;
			}
		}

		public String toString() {
			String out = null;
			out = "(" + r1.getURI() + ", "
					+ r2.getURI() + ")";
			return out;
		}

	}

	private static List<OpposesPair> createOpposesSetForOpposesPair(OpposesPair o,
			Map<Resource, Set<Resource>> hb,
			RdfProcess proc) throws Exception {

		List<OpposesPair> out = new ArrayList<OpposesPair>();

		List<Resource> children1 = proc.getChildren(o.r1);
		List<Resource> children2 = proc.getChildren(o.r2);
		for (Resource child : children1) {
			if (!o.r2.equals(child)) {
				if (!hb.get(child).contains(o.r2)) {
					OpposesPair t = new RnrmProcessFactory().new OpposesPair(o.r2, child);
					out.add(t);
				}
			}
		}

		for (Resource child : children2) {
			if (!o.r1.equals(child)) {
				if (!hb.get(child).contains(o.r1)) {
					OpposesPair t = new RnrmProcessFactory().new OpposesPair(o.r1, child);
					out.add(t);
				}
			}
		}

		for (int i = 0; i < children1.size(); i++) {
			for (int j = 0; j < children2.size(); j++) {
				if (!children1.get(i).equals(children2.get(j))) {
					Resource child1 = children1.get(i);
					Resource child2 = children2.get(j);
					if (!ordered(child1, child2, hb)) {
							OpposesPair t = new RnrmProcessFactory().new OpposesPair(child1,child2);
							out.add(t);
					}
				}
			}
		}
		return out;
	}

	private static Map<Activity, Set<Activity>> getActivityOppositionMap(
			RdfProcess proc,
			Set<Activity> activities,
			Map<Resource, Set<Resource>> hb) throws Exception  {

		Map<Activity, Set<Activity>> rv = new HashMap<Activity, Set<Activity>>();
		
		for (Activity a : activities) {
			rv.put(a, new HashSet<Activity>());
		}
		
		Set<OpposesPair> visited = new HashSet<OpposesPair>();
		Queue<OpposesPair> queue = new LinkedList<OpposesPair>();

		for (Resource element : hb.keySet()) {
			boolean isDecsionElement = RDFHelper.hasRdfType(proc.getModel(), element, RNRM.DecisionElement);
			if (isDecsionElement) {
				List<Resource> children = proc.getChildren(element);
				for (int i = 0; i < children.size(); i++) {
					for (int j = i + 1; j < children.size(); j++) {
						Resource child1 = children.get(i);
						Resource child2 = children.get(j);
						if (!ordered(child1, child2, hb)) {
							OpposesPair o = new RnrmProcessFactory().new OpposesPair(child1,child2);
							queue.add(o);
						}
					}
				}
			}
		}

		while (!queue.isEmpty()) {
			OpposesPair head = queue.remove();
		
			if (!visited.contains(head)) {
				visited.add(head);
				Activity o1 = getActivityFromResource(head.r1, proc);
				Activity o2 = getActivityFromResource(head.r2, proc);
				if (o1 != null && o2 != null) {
					Set<Activity> acts1 = rv.get(o1);
					acts1.add(o2);
					rv.put(o1, acts1);
					Set<Activity> acts2 = rv.get(o2);
					acts2.add(o1);
					rv.put(o2, acts2);
				}
				for (OpposesPair op : createOpposesSetForOpposesPair(head,
						hb, proc)) {
					if (!visited.contains(op)) {
						queue.add(op);
					}
				}
			}
		}
		return rv;
	}
	
	private static boolean ordered(Resource act1, Resource act2, Map<Resource, Set<Resource>> hb) {
		boolean rv = false;
		if (hb.get(act1).contains(act2))
			rv = true;
		else if (hb.get(act2).contains(act1))
			rv = true;
		return rv;
	}
	
	private static Activity getActivityFromResource(Resource r, RdfProcess proc) {
		Resource actResource = proc.getActivity(r);
		if (actResource==null) 
			return null;
		else {
			return new Activity(proc.getId(r), actResource.getURI());
		}
	}

}
