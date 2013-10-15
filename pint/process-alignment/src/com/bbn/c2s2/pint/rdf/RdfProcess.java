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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Wraps the RDF representation of an RNRM Process and provides access to common
 * elements
 * 
 * @author tself
 * 
 */
public class RdfProcess {
	private Resource processResource;
	private Model model;
	private Resource startElement;
	private Resource endElement;
	private Set<Resource> processElements;
	private String label;
	protected static Logger logger = LoggerFactory.getLogger(RdfProcess.class);
	boolean isValid = false;
	boolean initialized = false;

	private RdfProcess(Resource processResource) {
		this.processResource = processResource;
		model = processResource.getModel();
		if (null == model) {
			throw new IllegalArgumentException(
					"The Process Resource must have an associated Model.");
		}
		processElements = new HashSet<Resource>();
	}

	/**
	 * Creates and validates the RNRM process witht he given Resource
	 * 
	 * @param processResource
	 *            Resource that represents and RNRM Process
	 * 
	 * @return Initialized RdfProcess
	 */
	public static RdfProcess create(Resource processResource) {
		RdfProcess rv = new RdfProcess(processResource);
		rv.initialize();
		return rv;
	}

	public void initialize() {
		processElements = new HashSet<Resource>();
		StmtIterator it = model.listStatements(processResource,
				RNRM.hasElement, (RDFNode) null);
		while (it.hasNext()) {
			processElements.add(it.nextStatement().getResource());
		}
		label = RDFHelper.getLabel(model, processResource.getURI());

		// validate the process

		// check the start element
		establishStartElement();
		isValid = (startElement != null);
		if (!isValid) {
			if (logger.isWarnEnabled()) {
				logger
						.warn(String
								.format(
										"INVALID PROCESS (%1$s). No Start element can be identified.",
										getLabel()));
			}
		} else {
			// add the start element to the list of elements
			if (!processElements.contains(startElement)) {
				processElements.add(startElement);
			}
		}

		if (isValid) {
			// check the end element
			establishEndElement();
			isValid = (endElement != null);
			if (!isValid) {
				if (logger.isWarnEnabled()) {
					logger
							.warn(String
									.format(
											"INVALID PROCESS (%1$s). No End element can be identified.",
											getLabel()));
				}
			} else {
				// add the end element to the list of elements
				if (!processElements.contains(endElement)) {
					processElements.add(endElement);
				}
			}
		}

		if (isValid) {
			// check for bad cardinality violations
			isValid = !ProcessValidator.violatesCardinality(this);
			if (!isValid) {
				if (logger.isWarnEnabled()) {
					logger
							.warn(String
									.format(
											"INVALID PROCESS (%1$s). Contains cardinality violations on isFlowingTo arcs.",
											getLabel()));
				}
			}
		}

		if (isValid) {
			// check for a cycle
			isValid = !ProcessValidator.containsCycle(this);
			if (!isValid) {
				if (logger.isWarnEnabled()) {
					logger.warn(String.format(
							"INVALID PROCESS (%1$s). Cycle detected.",
							getLabel()));
				}
			}
		}

		if (isValid) {
			// check that all Activities are connected
			isValid = ProcessValidator.hasContiguousActivities(this);
			if (!isValid) {
				if (logger.isWarnEnabled()) {
					logger
							.warn(String
									.format(
											"INVALID PROCESS (%1$s). Not all Activity Elements are part of the isFlowingTo network.",
											getLabel()));
				}
			}
		}

		initialized = true;
	}

	private void establishStartElement() {
		// check if there's a labeled one
		StmtIterator it = getModel().listStatements(getProcessResource(),
				RNRM.hasStartElement, (RDFNode) null);
		if (it.hasNext()) {
			startElement = it.nextStatement().getResource();
			if (!RDFHelper.hasRdfType(getModel(), startElement,
					RNRM.JoinElement)) {
				logger
						.warn(String
								.format(
										"The Start Element in Process %1$s is not of type rnrm:JoinElement as expected.",
										getLabel()));
			}
		} else {
			// none labeled. Try to find it.
			startElement = findStartElement();
		}
	}

	private Resource findStartElement() {
		Resource rv = null;
		// determine if there are any Join elements with 0 incoming edges
		List<Resource> joins = getJoinElements();
		int count = 0;
		for (Resource j : joins) {
			if (getIncomingEdges(j).size() < 1) {
				rv = j;
				count++;
			}
		}
		if (count > 1) {
			logger
					.error(String
							.format(
									"Process %1$s has multiple Join Elements with 0 incoming rnrn:isFlowingTo edges. Cannot determine which one is the start element.",
									getLabel()));
			rv = null;
		}
		return rv;
	}

	private void establishEndElement() {
		// check if there's a labeled one
		StmtIterator it = getModel().listStatements(getProcessResource(),
				RNRM.hasEndElement, (RDFNode) null);
		if (it.hasNext()) {
			endElement = it.nextStatement().getResource();
			if (!RDFHelper.hasRdfType(getModel(), endElement, RNRM.ForkElement)) {
				logger
						.warn(String
								.format(
										"The End Element in Process (%1$s) is not of type rnrm:ForkElement as expected. End Element: %2$s.",
										getLabel(), RDFHelper
												.getLabel(getModel(),
														endElement.getURI())));
			}
		} else {
			// none labeled. Try to find it.
			endElement = findEndElement();
		}
	}

	private Resource findEndElement() {
		Resource rv = null;
		// determine if there are any Join elements with 0 incoming edges
		List<Resource> forks = getForkElements();
		int count = 0;
		for (Resource f : forks) {
			if (getOutgoingEdges(f).size() < 1) {
				rv = f;
				count++;
			}
		}
		if (count > 1) {
			logger
					.error(String
							.format(
									"Process %1$s has multiple Fork Elements with 0 outgoing rnrn:isFlowingTo edges. Cannot determine which one is the end element.",
									getLabel()));
			rv = null;
		}
		return rv;
	}

	public boolean isValid() {
		return isValid;
	}

	/**
	 * Returns the {@link Resource} that represents this RNRM Process
	 * 
	 * @return
	 */
	public Resource getProcessResource() {
		return processResource;
	}

	/**
	 * Returns the {@link Model} containing this RNRM process
	 * 
	 * @return
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Returns the {@link Resource} of the start element for this process
	 * 
	 * @return Resource if exists; null if it cannot be determined
	 */
	public Resource getStartElement() {
		return startElement;
	}

	/**
	 * Returns the {@link Resource} of the end element for this process
	 * 
	 * @return Resource if exists; null if it cannot be determined
	 */
	public Resource getEndElement() {
		return endElement;
	}

	/**
	 * Returns the {@link Resource}s for the process elements within this RNRM
	 * Process. This list may or may not include the start and end elements,
	 * depending on how the process was authored.
	 * 
	 * @return {@link Set} of {@link Resource} objects representing the process
	 *         elements
	 */
	public Set<Resource> getProcessElements() {
		return processElements;
	}

	/**
	 * Returns the label for this process.
	 * 
	 * @return Label as defined in the RNRM or its URI if no label is defined
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the URI of this RNRM Process
	 * 
	 * @return
	 */
	public String getProcessUri() {
		return processResource.getURI();
	}

	/**
	 * Returns the {@link Statement}s for each outgoing rnrm:isFlowingTo edge.
	 * 
	 * @param procElement
	 * @return
	 */
	public List<Statement> getOutgoingEdges(Resource procElement) {
		if (null == procElement) {
			throw new IllegalArgumentException(
					"Error. Given process Resource is null");
		}
		List<Statement> rv = new ArrayList<Statement>();
		StmtIterator it = model.listStatements(procElement, RNRM.isFlowingTo,
				(RDFNode) null);
		while (it.hasNext()) {
			// ensure the statement leads to something in this process
			Statement stmt = it.nextStatement();
			if (containsElement(stmt.getResource())) {
				rv.add(stmt);
			}
		}
		return rv;
	}

	/**
	 * Returns the {@link Statement}s for each incoming rnrm:isFlowingTo edge.
	 * 
	 * @param procElement
	 * @return
	 */
	public List<Statement> getIncomingEdges(Resource procElement) {
		if (null == procElement) {
			throw new IllegalArgumentException(
					"Error. Given process Resource is null");
		}
		List<Statement> rv = new ArrayList<Statement>();
		StmtIterator it = model.listStatements((Resource) null,
				RNRM.isFlowingTo, procElement);
		while (it.hasNext()) {
			// ensure the statement leads from something in this process
			Statement stmt = it.nextStatement();
			if (containsElement(stmt.getSubject())) {
				rv.add(stmt);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for the next process element on each outgoing
	 * rnrm:isFlowingTo path.
	 * 
	 * @param procElement
	 * @return List of children or empty list if there are none
	 */
	public List<Resource> getChildren(Resource procElement) {
		List<Statement> stmts = getOutgoingEdges(procElement);
		List<Resource> rv = new ArrayList<Resource>(stmts.size());
		for (Statement s : stmts) {
			rv.add(s.getResource());
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for the prior process element on each incoming
	 * rnrm:isFlowingTo path.
	 * 
	 * @param procElement
	 * @return List of parent elements or empty list if there are none
	 */
	public List<Resource> getParents(Resource procElement) {
		List<Statement> stmts = getIncomingEdges(procElement);
		List<Resource> rv = new ArrayList<Resource>(stmts.size());
		for (Statement s : stmts) {
			rv.add(s.getSubject());
		}
		return rv;
	}

	/**
	 * Sets an ID for procElement in the underlying modeling. ID's should be
	 * unique.
	 * 
	 * @param procElement
	 */

	public void setId(Resource procElement, int id) {
		model.add(model.createStatement(procElement, model
				.createProperty("hasId"), String.valueOf(id)));
	}

	public Integer getId(Resource procElement) {
		StmtIterator it = model.listStatements(procElement, model
				.createProperty("hasId"), (String) null);
		if (!it.hasNext())
			return null;
		String id = it.nextStatement().getObject().toString();
		return Integer.valueOf(id);
	}

	/**
	 * Returns a {@link Resource} for each Activity Element in this Process
	 * 
	 * @return
	 */
	public List<Resource> getActivityElements() {
		List<Resource> rv = new ArrayList<Resource>();
		for (Resource pe : processElements) {
			if (RDFHelper.hasRdfType(getModel(), pe, RNRM.ActivityElement)) {
				rv.add(pe);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Fork Element in this Process
	 * 
	 * @return
	 */
	public List<Resource> getForkElements() {
		List<Resource> rv = new ArrayList<Resource>();
		for (Resource pe : processElements) {
			if (RDFHelper.hasRdfType(getModel(), pe, RNRM.ForkElement)) {
				rv.add(pe);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Join Element in this Process
	 * 
	 * @return
	 */
	public List<Resource> getJoinElements() {
		List<Resource> rv = new ArrayList<Resource>();
		for (Resource pe : processElements) {
			if (RDFHelper.hasRdfType(getModel(), pe, RNRM.JoinElement)) {
				rv.add(pe);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Decision Element in this Process
	 * 
	 * @return
	 */
	public List<Resource> getDecisionElements() {
		List<Resource> rv = new ArrayList<Resource>();
		for (Resource pe : processElements) {
			if (RDFHelper.hasRdfType(getModel(), pe, RNRM.DecisionElement)) {
				rv.add(pe);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Merge Element in this Process
	 * 
	 * @return
	 */
	public List<Resource> getMergeElements() {
		List<Resource> rv = new ArrayList<Resource>();
		for (Resource pe : processElements) {
			if (RDFHelper.hasRdfType(getModel(), pe, RNRM.MergeElement)) {
				rv.add(pe);
			}
		}
		return rv;
	}

	/**
	 * Returns the {@link Resource}s for any Activity Elements that have no
	 * incoming or outgoing rnrm:isFlowingTo edges.
	 * 
	 * @return
	 */
	public List<Resource> getIsolatedActivities() {
		List<Resource> rv = new ArrayList<Resource>();
		List<Resource> activityElements = getActivityElements();
		for (Resource r : activityElements) {
			if (isIsolated(r)) {
				rv.add(r);
			}
		}
		return rv;

	}

	/**
	 * Returns a {@link Resource} for each Observable attached to this process
	 * 
	 * @return
	 */
	public List<Resource> getObservables() {
		List<Resource> rv = new ArrayList<Resource>();
		List<Resource> activities = getActivities();
		for (Resource act : activities) {
			rv.addAll(getObservables(act));
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Activity Element in this process that
	 * has any Observables attached
	 * 
	 * @return
	 */
	public List<Resource> getActivitiesWithObservables() {
		List<Resource> rv = new ArrayList<Resource>();
		List<Resource> activities = getActivities();
		for (Resource act : activities) {
			if (getObservables(act).size() > 0) {
				rv.add(act);
			}
		}
		return rv;
	}

	/**
	 * Returns whether or not this process has at least 1 Observable for every
	 * Activity
	 * 
	 * @return
	 */
	public boolean hasObservablesForAllActivities() {
		boolean rv = true;
		List<Resource> activities = getActivities();
		if (activities.size() == 0)
			return false;
		for (Resource act : activities) {
			rv = getObservables(act).size() > 0;
			if (!rv) {
				break;
			}
		}
		return rv;
	}

	/**
	 * Returns whether or not this process has any Observables connected to it
	 * 
	 * @return
	 */
	public boolean hasAnyObservables() {
		boolean rv = false;
		List<Resource> activities = getActivities();
		for (Resource act : activities) {
			rv = getObservables(act).size() > 0;
			if (rv) {
				break;
			}
		}
		return rv;
	}

	/**
	 * Returns the URI for each Observable linked to the given {@link Activity}
	 * 
	 * @param act
	 * @return
	 */
	public List<String> getObservableUris(Activity act) {
		Resource actResource = getModel().createResource(act.getActivityURI());
		List<Resource> resources = getObservables(actResource);
		List<String> rv = new ArrayList<String>(resources.size());
		for (Resource r : resources) {
			rv.add(r.getURI());
		}
		return rv;
	}

	/**
	 * Returns the rdfs:label for the {@link Resource} or its URI if it has no
	 * label
	 * 
	 * @param rdfResource
	 *            Resource object
	 * @return String representing the label
	 */
	public String getElementLabel(Resource rdfResource) {
		String rv = RDFHelper.getLabel(getModel(), rdfResource.getURI());
		return rv;
	}

	/**
	 * Returns a {@link Resource} for each Observable linked to the given
	 * Activity resource
	 * 
	 * @param activity
	 * @return List of Resources or an empty list if there are none
	 */
	public List<Resource> getObservables(Resource activity) {
		List<Resource> rv = new ArrayList<Resource>();
		// check that this is the actual activity and not the activity element
		Resource actNode = activity;
		if (RDFHelper.hasRdfType(getModel(), activity, RNRM.ActivityElement)) {
			actNode = getActivity(activity);
		}
		StmtIterator it = getModel().listStatements(actNode,
				RNRM.hasObservable, (RDFNode) null);
		while (it.hasNext()) {
			rv.add(it.nextStatement().getResource());
		}
		return rv;
	}

	public List<Resource> getActivities() {
		List<Resource> rv = new ArrayList<Resource>();
		List<Resource> elems = getActivityElements();
		for (Resource ae : elems) {
			Resource act = getActivity(ae);
			if (null != act) {
				rv.add(act);
			}
		}
		return rv;
	}

	/**
	 * Returns a {@link Resource} for the Activity attached to the given
	 * Activity Element
	 * 
	 * @param activityElement
	 * @return Activity Resource or null if none exists
	 */
	public Resource getActivity(Resource activityElement) {
		Resource rv = null;
		StmtIterator it = getModel().listStatements(activityElement,
				RNRM.representsActivity, (RDFNode) null);
		if (it.hasNext()) {
			rv = it.nextStatement().getResource();
		}
		return rv;
	}

	/**
	 * Tests whether an rnrm:isFlowingTo path exists from one process element to
	 * another without going outside of this Process
	 * 
	 * @param u
	 *            The {@link Resource} of the source process element
	 * @param v
	 *            The {@link Resource} of the sink process element
	 * @return
	 */
	public boolean pathExists(Resource u, Resource v) {
		boolean rv = false;
		if (null != u && null != v) {
			if (u.equals(v)) {
				rv = true;
			} else {
				Set<Resource> visited = new HashSet<Resource>();
				rv = dfsFindPathTo(u, v, visited);
			}
		}
		return rv;

	}

	private boolean dfsFindPathTo(Resource next, Resource toFind,
			Set<Resource> visited) {
		boolean rv = false;
		if (!visited.contains(next)) {
			visited.add(next);
			if (next.equals(toFind)) {
				rv = true;
			} else {
				List<Resource> children = getChildren(next);
				for (Resource child : children) {
					rv = dfsFindPathTo(child, toFind, visited);
					if (rv) {
						break;
					}
				}
			}
		}
		return rv;
	}

	/**
	 * Tests whether an rnrm:isFlowingTo path exists from the start element to
	 * the end element
	 * 
	 * @return True if a path exists; false if not.
	 */
	public boolean completePathExists() {
		return pathExists(startElement, endElement);
	}

	/**
	 * Tests whether the specified resource has any incoming or outgoing
	 * rnrm:isFlowingTo edges.
	 * 
	 * @param r
	 * @return True if there are 0 incoming and 0 outgoing edges; false if any
	 *         edges exist.
	 */
	public boolean isIsolated(Resource procElement) {
		return getIncomingEdges(procElement).size() < 1
				&& getOutgoingEdges(procElement).size() < 1;
	}

	/**
	 * Determines whether the given process element is a member of this process
	 * 
	 * @param procElement
	 * @return True if the element is within this process; false otherwise
	 */
	public boolean containsElement(Resource procElement) {
		boolean rv = false;
		rv = processElements.contains(procElement);
		return rv;
	}
}
