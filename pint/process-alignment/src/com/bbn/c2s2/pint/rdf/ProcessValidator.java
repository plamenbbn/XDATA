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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.pf.PartialOrderBuilder;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Searches the RDF representation of an RNRM Process for cycles
 * 
 * @author tself
 * 
 */
public class ProcessValidator {
	private static Logger logger = LoggerFactory
			.getLogger(ProcessValidator.class);

	private static final int MAX_CARD_VIOLATION_LEVEL = 4;

	protected enum CardinalityViolationType {
		LEAF_ACTIVITY(1), NONTERMINATED_START(1), NONTERMINATED_END(1), OVERPOPULATED_ACTIVITY(
				2), LEAF_PROCESS_ELEMENT(3), OVERPOPULATED_OPENING(4), OVERPOPULATED_CLOSING(
				4), UNNECESSARY_ELEMENT(10);

		protected int priority = Integer.MAX_VALUE;

		private CardinalityViolationType(int i) {
			priority = i;
		}
	};

	/**
	 * Searches the given RdfProcess for a cycle
	 * 
	 * @param process
	 * @return True if there is a cycle; False if there are no cycles
	 */
	public static boolean containsCycle(RdfProcess process) {
		boolean rv = false;
		PartialOrderBuilder pob = new PartialOrderBuilder(process);
		rv = pob.hasCycle();
		return rv;
	}

	/**
	 * Checks whether all of the ActivityElements in the given RdfProcess are
	 * part of the directed graph from start to end
	 * 
	 * @return True if all ActivityElements are connected; False otherwise
	 */
	public static boolean hasContiguousActivities(RdfProcess process) {
		boolean rv = false;
		// create a set of ActivityElements, then start removing them while
		// traversing the graph
		Set<Resource> activities = new HashSet<Resource>(process
				.getActivityElements());
		Resource start = process.getStartElement();
		if (null != start) {
			traverseAndRemoveActivities(start, activities, process);
			rv = activities.size() < 1;
		}
		return rv;
	}

	private static void traverseAndRemoveActivities(Resource current,
			Set<Resource> activities, RdfProcess process) {
		activities.remove(current);
		List<Resource> children = process.getChildren(current);
		for (Resource child : children) {
			traverseAndRemoveActivities(child, activities, process);
		}
	}

	/**
	 * Checks the given process for cardinality violations on the
	 * rnrm:isFlowingTo arcs.
	 * 
	 * <pre>
	 * Violations include:
	 *    Leaf Process Elements (0 incoming or outgoing edges)
	 *    Overloaded Activity Elements (More than 1 incoming or outgoing edge)
	 *    Overloaded Fork/Decisions (More than 1 incoming edge)
	 *    Overloaded Join/Merge (More than 1 outgoing edge)
	 * </pre>
	 * 
	 * @param process
	 * @return
	 */
	public static boolean violatesCardinality(RdfProcess process) {
		boolean rv = false;
		CardinalityViolation violation = getNextCardinalityViolation(process);
		rv = (null != violation && violation.violation.priority <= MAX_CARD_VIOLATION_LEVEL);
		return rv;
	}

	protected static CardinalityViolation getNextCardinalityViolation(
			RdfProcess process) {
		CardinalityViolation rv = null;
		List<CardinalityViolation> violations = new ArrayList<CardinalityViolation>();

		// start should have no incoming
		Resource start = process.getStartElement();
		if (process.getIncomingEdges(start).size() > 0) {
			violations.add(new CardinalityViolation(start,
					CardinalityViolationType.NONTERMINATED_START));
		}

		// end should have no outgoing
		Resource end = process.getEndElement();
		if (process.getOutgoingEdges(end).size() > 0) {
			violations.add(new CardinalityViolation(end,
					CardinalityViolationType.NONTERMINATED_END));
		}

		Set<Resource> elements = process.getProcessElements();
		for (Resource elem : elements) {
			if (elem.equals(start) || elem.equals(end)) {
				// already took care of start/end
				continue;
			}
			int incoming = process.getIncomingEdges(elem).size();
			int outgoing = process.getOutgoingEdges(elem).size();

			// deal with leaf elements
			if ((incoming == 0 && outgoing > 0)
					|| (incoming > 0 && outgoing == 0)) {
				if (RDFHelper.hasRdfType(process.getModel(), elem,
						RNRM.ActivityElement)) {
					violations.add(new CardinalityViolation(elem,
							CardinalityViolationType.LEAF_ACTIVITY));
				} else {
					violations.add(new CardinalityViolation(elem,
							CardinalityViolationType.LEAF_PROCESS_ELEMENT));
				}
			}
			// overloaded activities
			else if ((incoming > 1 || outgoing > 1)
					&& RDFHelper.hasRdfType(process.getModel(), elem,
							RNRM.ActivityElement)) {
				violations.add(new CardinalityViolation(elem,
						CardinalityViolationType.OVERPOPULATED_ACTIVITY));
			}

			// overloaded opening elements
			else if (incoming > 1
					&& (RDFHelper.hasRdfType(process.getModel(), elem,
							RNRM.ForkElement) || RDFHelper.hasRdfType(process
							.getModel(), elem, RNRM.DecisionElement))) {
				violations.add(new CardinalityViolation(elem,
						CardinalityViolationType.OVERPOPULATED_OPENING));
			}

			// overloaded closing elements
			else if (outgoing > 1
					&& (RDFHelper.hasRdfType(process.getModel(), elem,
							RNRM.JoinElement) || RDFHelper.hasRdfType(process
							.getModel(), elem, RNRM.MergeElement))) {
				violations.add(new CardinalityViolation(elem,
						CardinalityViolationType.OVERPOPULATED_CLOSING));
			}

			// unnecessary open/close elements
			else if ((outgoing == 1 && incoming == 1)
					&& !RDFHelper.hasRdfType(process.getModel(), elem,
							RNRM.ActivityElement)) {
				violations.add(new CardinalityViolation(elem,
						CardinalityViolationType.UNNECESSARY_ELEMENT));
			}
		}
		if (violations.size() > 0) {
			Collections.sort(violations);
			if (logger.isWarnEnabled()) {
				for (CardinalityViolation v : violations) {
					logger.warn(String.format(
							"Cardinality Violation in process (%1$s): %2$s",
							process.getLabel(), v));
				}
			}
			rv = violations.get(0);
		}
		return rv;
	}

	protected static class CardinalityViolation implements
			Comparable<CardinalityViolation> {
		protected Resource offendingProcessElement;
		protected CardinalityViolationType violation;

		protected CardinalityViolation(Resource r) {
			offendingProcessElement = r;
		}

		protected CardinalityViolation(Resource r, CardinalityViolationType type) {
			this(r);
			violation = type;
		}

		@Override
		public int compareTo(CardinalityViolation other) {
			int rv = 0;
			if (violation.priority < other.violation.priority) {
				rv = -1;
			} else if (violation.priority > other.violation.priority) {
				rv = 1;
			} else {
				rv = offendingProcessElement.getURI().compareTo(
						other.offendingProcessElement.getURI());
			}
			return rv;
		}

		@Override
		public String toString() {
			return String.format("Violation (%1$s), Element (%2$s)", violation,
					offendingProcessElement);
		}
	}
}
