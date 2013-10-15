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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Wraps the RDF representation of an RNRM Process and provides access to common
 * elements and methods for manipulating the topology
 * 
 * @author tself
 * 
 */
public class RdfProcessEditor {
	private final String NAMESPACE = "http://www.bbn.com/c2s2#";
	private RdfProcess process;
	protected static Logger logger = LoggerFactory
			.getLogger(RdfProcessEditor.class);

	public RdfProcessEditor(RdfProcess process) {
		this.process = process;
	}

	/**
	 * Creates a Start Element and adds it to this Process
	 * 
	 * @return
	 */
	public Resource createStartElement() {
		Resource rv = null;
		Model model = process.getModel();
		rv = model.createResource(String.format("%1$s%2$s%3$s", NAMESPACE,
				"START", UUID.randomUUID().toString()));
		model.add(rv, RDF.type, RNRM.JoinElement);
		model.add(process.getProcessResource(), RNRM.hasStartElement, rv);
		return rv;
	}

	/**
	 * Creates a Fork Element and adds it to this Process
	 * 
	 * @return
	 */
	public Resource createForkElement() {
		Resource rv = null;
		Model model = process.getModel();
		Resource processResource = process.getProcessResource();
		rv = model.createResource(String.format("%1$s%2$s%3$s", NAMESPACE,
				"FORK", UUID.randomUUID().toString()));
		model.add(rv, RDF.type, RNRM.ForkElement);
		model.add(processResource, RNRM.hasElement, rv);
		return rv;
	}

	/**
	 * Creates a Join Element and adds it to this Process
	 * 
	 * @return
	 */
	public Resource createJoinElement() {
		Resource rv = null;
		Model model = process.getModel();
		Resource processResource = process.getProcessResource();
		rv = model.createResource(String.format("%1$s%2$s%3$s", NAMESPACE,
				"JOIN", UUID.randomUUID().toString()));
		model.add(rv, RDF.type, RNRM.JoinElement);
		model.add(processResource, RNRM.hasElement, rv);
		return rv;
	}

	/**
	 * Creates an End Element and adds it to this Process
	 * 
	 * @return
	 */
	public Resource createEndElement() {
		Resource rv = null;
		Model model = process.getModel();
		Resource processResource = process.getProcessResource();
		rv = model.createResource(String.format("%1$s%2$s%3$s", NAMESPACE,
				"END", UUID.randomUUID().toString()));
		model.add(rv, RDF.type, RNRM.ForkElement);
		model.add(processResource, RNRM.hasEndElement, rv);
		return rv;
	}

	/**
	 * Returns the {@link RdfProcess} that wraps this RNRM Process
	 * 
	 * @return
	 */
	public RdfProcess getRdfProcess() {
		return process;
	}

	/**
	 * Inserts the given {@link Resource} into the rnrm:isFlowingTo path after
	 * the specified {@link Resource}
	 * 
	 * @param toInsert
	 *            Process element to insert
	 * @param after
	 *            Process element to insert after
	 */
	public void insertAfter(Resource toInsert, Resource after) {
		List<Statement> outgoing = process.getOutgoingEdges(after);
		Model model = process.getModel();
		if (outgoing.size() > 1) {
			throw new IllegalArgumentException(
					"Cannot insert after nodes with multiple outgoing links.");
		}
		model.add(after, RNRM.isFlowingTo, toInsert);
		if (outgoing.size() > 0) {
			Statement stmt = outgoing.get(0);
			Resource next = stmt.getResource();
			model.remove(stmt);
			model.add(toInsert, RNRM.isFlowingTo, next);
		}
	}

	/**
	 * Inserts the given {@link Resource} into the rnrm:isFlowingTo path before
	 * the specified {@link Resource}
	 * 
	 * @param toInsert
	 *            Process element to insert
	 * @param before
	 *            Process element to insert before
	 */
	public void insertBefore(Resource toInsert, Resource before) {
		List<Statement> incoming = process.getIncomingEdges(before);
		Model model = process.getModel();
		if (incoming.size() > 1) {
			throw new IllegalArgumentException(
					"Cannot insert before nodes with multiple incoming links.");
		}
		model.add(toInsert, RNRM.isFlowingTo, before);
		if (incoming.size() > 0) {
			Statement stmt = incoming.get(0);
			Resource prior = stmt.getSubject();
			model.remove(stmt);
			model.add(prior, RNRM.isFlowingTo, toInsert);
		}
	}

	/**
	 * Deletes the given process element from the rnrm:isFlowingTo linked list
	 * without breaking the path. The element is also removed from the process.
	 * 
	 * @param processElement
	 */
	public void deleteFromPath(Resource processElement) {
		Model model = process.getModel();
		List<Statement> incoming = process.getIncomingEdges(processElement);
		List<Statement> outgoing = process.getOutgoingEdges(processElement);
		if (incoming.size() > 1 || outgoing.size() > 1) {
			throw new IllegalArgumentException(
					"Error deleting element. Elements with more than 1 outgoing or incoming links cannot be deleted.");
		}
		Resource before = null;
		Resource after = null;
		Statement stmt = null;
		if (incoming.size() > 0) {
			stmt = incoming.get(0);
			before = stmt.getSubject();
			model.remove(stmt);
		}
		if (outgoing.size() > 0) {
			stmt = outgoing.get(0);
			after = stmt.getResource();
			model.remove(stmt);
		}
		if (null != before && null != after) {
			model.add(before, RNRM.isFlowingTo, after);
		}

		// now disassociate this element from the process
		removeFromProcess(processElement);
	}

	/**
	 * Eliminates the rnrm:hasElement link (and subproperties) between the given
	 * element and the process.
	 * 
	 * @param procElement
	 */
	public void removeFromProcess(Resource procElement) {
		process.getModel().removeAll(process.getProcessResource(),
				(Property) null, procElement);
	}

	/**
	 * Removes all {@link Statement}s representing an incoming rnrm:isFlowingTo
	 * edge.
	 * 
	 * @param procElement
	 */
	public void removeIncomingLinks(Resource procElement) {
		List<Statement> toRemove = process.getIncomingEdges(procElement);
		Statement[] stmts = new Statement[toRemove.size()];
		process.getModel().remove(toRemove.toArray(stmts));
	}

	/**
	 * Removes all {@link Statement}s representing an outgoing rnrm:isFlowingTo
	 * edge.
	 * 
	 * @param procElement
	 */
	public void removeOutgoingLinks(Resource procElement) {
		List<Statement> toRemove = process.getOutgoingEdges(procElement);
		Statement[] stmts = new Statement[toRemove.size()];
		process.getModel().remove(toRemove.toArray(stmts));
	}

	/**
	 * Removes an outgoing rnrm:isFlowingTo edge from the specified element such
	 * that a path from the element to the end of the process still exists.
	 * 
	 * @param procElement
	 *            Element to remove an outgoing edge from
	 * @return True if an edge was safely removed. False if no edges could be
	 *         removed without eliminating the path.
	 */
	public boolean removeNonBreakingOutgoingArc(Resource procElement) {
		boolean foundSafeRemoval = false;
		if (process.completePathExists()) {
			List<Statement> outgoingArcs = process
					.getOutgoingEdges(procElement);
			for (Statement stmt : outgoingArcs) {
				process.getModel().remove(stmt);
				if (process.pathExists(procElement, process.getEndElement())) {
					foundSafeRemoval = true;
					break;
				} else {
					process.getModel().add(stmt);
				}
			}
		}
		return foundSafeRemoval;
	}

	/**
	 * Removes an incoming rnrm:isFlowingTo edge from the specified element such
	 * that a path from the start element to the specified element still exists.
	 * 
	 * @param procElement
	 *            Element to remove an incoming edge from
	 * @return True if an edge was safely removed. False if no edges could be
	 *         removed without eliminating the path.
	 */
	public boolean removeNonBreakingIncomingArc(Resource procElement) {
		boolean foundSafeRemoval = false;
		if (process.completePathExists()) {
			List<Statement> incomingArcs = process
					.getIncomingEdges(procElement);
			for (Statement stmt : incomingArcs) {
				process.getModel().remove(stmt);
				if (process.pathExists(process.getStartElement(), procElement)) {
					foundSafeRemoval = true;
					break;
				} else {
					process.getModel().add(stmt);
				}
			}
		}
		return foundSafeRemoval;
	}

	/**
	 * Removes all incoming and outgoing rnrm:isFlowingTo edges from the
	 * specified element. If this is a fork, join, decision, or merge and it's
	 * not the start or end, then it will just be deleted since you cannot
	 * recover it.
	 * 
	 * @param procElement
	 */
	public void isolateElement(Resource procElement) {
		removeOutgoingLinks(procElement);
		removeIncomingLinks(procElement);
		// if this is something other than an ActivityElement and isn't the
		// start or end, then remove it from the model
		if (!RDFHelper.hasRdfType(process.getModel(), procElement,
				RNRM.ActivityElement)
				&& !procElement.equals(process.getStartElement())
				&& !procElement.equals(process.getEndElement())) {
			removeFromProcess(procElement);
		}
	}

	/**
	 * Converts the given process into an unordered set of ActivityElements
	 * 
	 * @param processResource
	 */
	public void removeProcessOrder() {
		if (logger.isWarnEnabled()) {
			logger
					.warn(String
							.format(
									"Giving up all attempts to repair process, %1$s. Treating it as an unordered set of Activities.",
									process.getLabel()));
		}
		Resource startElement = process.getStartElement();
		Resource endElement = process.getEndElement();
		// make sure start element is ready to go with no arcs
		if (null == startElement) {
			// create a start element
			startElement = createStartElement();
		} else {
			isolateElement(startElement);
		}

		// make sure end element is ready to go with no arcs
		if (null == endElement) {
			// create an end element
			endElement = createEndElement();
		} else {
			isolateElement(endElement);
		}
		// isolate activity elements
		Set<Resource> processElements = process.getProcessElements();
		for (Resource r : processElements) {
			// isolate will remove in/out links and delete the element if it's
			// not Activity, Start, or End
			isolateElement(r);
		}

		finalizeProcess();
	}

	/**
	 * Cleans up the process by ensuring that any isolated ActivityElements are
	 * added as unordered paths.
	 */
	public void finalizeProcess() {
		List<Resource> isolatedActivities = process.getIsolatedActivities();
		if (isolatedActivities.size() > 0) {
			Model model = process.getModel();
			// TODO: Fix this so that it doesn't keep stacking outer fork/joins
			// when they aren't necessary

			// create fork/join for all elements
			Resource fork = createForkElement();
			Resource join = createJoinElement();

			Resource start = process.getStartElement();
			Resource end = process.getEndElement();
			// insert fork after the start
			insertAfter(fork, start);

			// insert join before the finish
			insertBefore(join, end);

			// add any isolated activities
			for (Resource r : isolatedActivities) {
				model.add(fork, RNRM.isFlowingTo, r);
				model.add(r, RNRM.isFlowingTo, join);
			}
		}
	}

	/**
	 * Creates an Observable and includes it in the model
	 * 
	 * @param uri
	 *            URI of the Observable to create
	 * @return
	 */
	public Resource createObservable(String uri) {
		Resource rv = null;
		Model model = process.getModel();
		rv = model.createResource(uri);
		model.add(rv, RDF.type, RNRM.Observable);
		return rv;
	}

	/**
	 * Adds the given Observable to the given Activity
	 * 
	 * @param activity
	 *            Resource of the Activity
	 * @param observable
	 *            Resource of the Observable
	 */
	public void addObservable(Resource activity, Resource observable) {
		process.getModel().add(activity, RNRM.hasObservable, observable);
	}

	/**
	 * Adds a random Observable to any Activities that are missing Observables
	 */
	public void fillInObservables() {
		for (Resource r : process.getActivities()) {
			if (process.getObservables(r).size() < 1) {
				String obsUri = String.format("%1$s_OBSERVABLE", r.getURI());
				addObservable(r, createObservable(obsUri));
			}
		}
	}
}
