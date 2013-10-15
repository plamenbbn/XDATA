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

import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ProcessModelFactory {

	static final String NS = "http://c2s2.bbn.com/test#";
	static int count = 0;
	static Resource observable1;
	static Resource observable2;

	protected static Model createModelWithObservables() {
		Model rv = ModelFactory.createDefaultModel();
		observable1 = rv.createResource(NS + "observable1");
		rv.add(observable1, RDF.type, RNRM.Observable);
		rv.add(observable1, RDFS.label, "Red Barrel");

		observable2 = rv.createResource(NS + "observable2");
		rv.add(observable2, RDF.type, RNRM.Observable);
		rv.add(observable2, RDFS.label, "Blue Barrel");

		return rv;
	}

	public static Resource createNewProcessWithoutObservables() {
		Model model = ModelFactory.createDefaultModel();
		Resource rv = model.createResource(NS + "process" + count++);
		model.add(rv, RDFS.label, "Process " + (count - 1));
		model.add(rv, RDF.type, RNRM.Process);
		return rv;
	}

	/**
	 * Creates an empty Process object within a Model
	 * 
	 * @return Resource for a Process with 0 Activities
	 */
	public static Resource createNewProcess() {
		Model model = createModelWithObservables();
		Resource rv = model.createResource(NS + "process" + count++);
		model.add(rv, RDFS.label, "Process " + (count - 1));
		model.add(rv, RDF.type, RNRM.Process);
		return rv;
	}

	public static Resource createEmptyProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();
		// add the initial Join Element
		Resource start = addStartElement(model, rv);

		// add the fork element to close the process
		Resource end = addEndElement(model, rv);

		model.add(start, RNRM.isFlowingTo, end);

		return rv;
	}

	/**
	 * Creates a Process with a single Activity attached
	 * 
	 * @return Resource for a single-activity Process
	 */
	public static Resource createSingleActivityProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act);
		model.add(init, RNRM.isFlowingTo, act);

		Resource close = addEndElement(model, rv);
		model.add(act, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with a single Activity which has a single observable.
	 * 
	 * @return Resource for a single-activity Process
	 */

	public static Resource createSingleActivityProcessWithObservable() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act);
		model.add(init, RNRM.isFlowingTo, act);

		Resource close = addEndElement(model, rv);
		model.add(act, RNRM.isFlowingTo, close);
		model.add(act, RNRM.hasObservable, createObservableResource(model));

		return rv;
	}

	/**
	 * Creates a Process with a serial list of processes (no forks)
	 * 
	 * <pre>
	 * A | B
	 * </pre>
	 * 
	 * @return Resource for a serial Process
	 */
	public static Resource createSerialProcessWithObservables() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(init, RNRM.isFlowingTo, act1);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(act1, RNRM.isFlowingTo, act2);

		Resource close = addEndElement(model, rv);
		model.add(act2, RNRM.isFlowingTo, close);

		model.add(act1, RNRM.hasObservable, createObservableResource(model));
		model.add(act2, RNRM.hasObservable, createObservableResource(model));
		return rv;
	}

	/**
	 * Creates a Process with a serial list of processes (no forks)
	 * 
	 * <pre>
	 * A | B | C
	 * </pre>
	 * 
	 * @return Resource for a serial Process
	 */
	public static Resource createSerialProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(init, RNRM.isFlowingTo, act1);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(act1, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(act2, RNRM.isFlowingTo, act3);

		Resource close = addEndElement(model, rv);
		model.add(act3, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process that contains 3 Activities that can occur in parallel.
	 * 
	 * <pre>
	 *    / | \
	 *   A  B  C
	 * </pre>
	 * 
	 * @return Resource for the created Process
	 */
	public static Resource createParallelProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(init, RNRM.isFlowingTo, fork);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(fork, RNRM.isFlowingTo, act1);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(fork, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(fork, RNRM.isFlowingTo, act3);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(act1, RNRM.isFlowingTo, join);
		model.add(act2, RNRM.isFlowingTo, join);
		model.add(act3, RNRM.isFlowingTo, join);

		Resource close = addEndElement(model, rv);
		model.add(join, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with 4 Activities where 2 can occur in parallel
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C
	 *     \ /
	 *      D
	 * </pre>
	 * 
	 * @return Resource for the created Process
	 */
	public static Resource createForkedProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(init, RNRM.isFlowingTo, act1);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(act1, RNRM.isFlowingTo, fork);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(fork, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(fork, RNRM.isFlowingTo, act3);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(act2, RNRM.isFlowingTo, join);
		model.add(act3, RNRM.isFlowingTo, join);

		Resource act4 = createActivityElementWithRedBarrel("D", rv);
		model.add(rv, RNRM.hasElement, act4);
		model.add(join, RNRM.isFlowingTo, act4);

		Resource close = addEndElement(model, rv);
		model.add(act4, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with a cycle that uses fork/join to enable the cycle
	 * 
	 * <pre>
	 *      A
	 *   /\ |
	 *  | ----
	 *  |   |
	 *  D   B
	 *  |   |
	 *  | ----
	 *   \/ |
	 *      C
	 * </pre>
	 * 
	 * @return
	 */
	public static Resource createCyclicProcessB() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource start = addStartElement(model, rv);

		Resource actA = createActivityElementWithActivity("A", rv);
		model.add(rv, RNRM.hasElement, actA);
		model.add(start, RNRM.isFlowingTo, actA);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(actA, RNRM.isFlowingTo, join);

		Resource actB = createActivityElementWithActivity("B", rv);
		model.add(rv, RNRM.hasElement, actB);
		model.add(join, RNRM.isFlowingTo, actB);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(actB, RNRM.isFlowingTo, fork);

		Resource actC = createActivityElementWithActivity("C", rv);
		model.add(rv, RNRM.hasElement, actC);
		model.add(fork, RNRM.isFlowingTo, actC);

		Resource actD = createActivityElementWithActivity("D", rv);
		model.add(rv, RNRM.hasElement, actD);
		model.add(fork, RNRM.isFlowingTo, actD);
		model.add(actD, RNRM.isFlowingTo, join);

		Resource end = addEndElement(model, rv);
		model.add(actC, RNRM.isFlowingTo, end);

		return rv;
	}

	/**
	 * Creates a Process with a cycle caused by overloading the links to an
	 * ActivityElement
	 * 
	 * <pre>
	 *       A
	 *       |
	 *       B
	 *     / |
	 *    |  C
	 *    |_/ \
	 *         D
	 * </pre>
	 * 
	 * @return
	 */
	public static Resource createCyclicProcessA() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(init, RNRM.isFlowingTo, act1);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(act1, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(act2, RNRM.isFlowingTo, act3);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(act3, RNRM.isFlowingTo, fork);
		model.add(fork, RNRM.isFlowingTo, act2);

		Resource act4 = createActivityElementWithRedBarrel("D", rv);
		model.add(rv, RNRM.hasElement, act4);
		model.add(fork, RNRM.isFlowingTo, act4);

		Resource close = addEndElement(model, rv);
		model.add(act4, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with 6 Activities with 2 back-back fork/joins
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
	 * @return {@link Resource} for the created process
	 */
	public static Resource createBackToBackForkedProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(init, RNRM.isFlowingTo, act1);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(act1, RNRM.isFlowingTo, fork);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(fork, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(fork, RNRM.isFlowingTo, act3);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(act2, RNRM.isFlowingTo, join);
		model.add(act3, RNRM.isFlowingTo, join);

		Resource fork2 = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork2);
		model.add(join, RNRM.isFlowingTo, fork2);

		Resource act4 = createActivityElementWithRedBarrel("D", rv);
		model.add(rv, RNRM.hasElement, act4);
		model.add(fork2, RNRM.isFlowingTo, act4);

		Resource act5 = createActivityElementWithBlueBarrel("E", rv);
		model.add(rv, RNRM.hasElement, act5);
		model.add(fork2, RNRM.isFlowingTo, act5);

		Resource join2 = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join2);
		model.add(act4, RNRM.isFlowingTo, join2);
		model.add(act5, RNRM.isFlowingTo, join2);

		Resource act6 = createActivityElementWithRedAndBlueBarrels("F", rv);
		model.add(rv, RNRM.hasElement, act6);
		model.add(join2, RNRM.isFlowingTo, act6);

		Resource close = addEndElement(model, rv);
		model.add(act6, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with 5 Activities with an asymmetric forked path
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
	 * @return {@link Resource} for the created process
	 */
	public static Resource createAsymmetricForkProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource actA = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, actA);
		model.add(init, RNRM.isFlowingTo, actA);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(actA, RNRM.isFlowingTo, fork);

		Resource actB = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, actB);
		model.add(fork, RNRM.isFlowingTo, actB);

		Resource actC = createActivityElementWithRedAndBlueBarrels("C", rv);
		model.add(rv, RNRM.hasElement, actC);
		model.add(actB, RNRM.isFlowingTo, actC);

		Resource actF = createActivityElementWithRedBarrel("F", rv);
		model.add(rv, RNRM.hasElement, actF);
		model.add(fork, RNRM.isFlowingTo, actF);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(actC, RNRM.isFlowingTo, join);
		model.add(actF, RNRM.isFlowingTo, join);

		Resource actD = createActivityElementWithRedAndBlueBarrels("D", rv);
		model.add(rv, RNRM.hasElement, actD);
		model.add(join, RNRM.isFlowingTo, actD);

		Resource actE = createActivityElementWithRedBarrel("E", rv);
		model.add(rv, RNRM.hasElement, actE);
		model.add(actD, RNRM.isFlowingTo, actE);

		Resource close = addEndElement(model, rv);
		model.add(actE, RNRM.isFlowingTo, close);

		return rv;

	}

	/**
	 * Creates a Process with 7 Activities with overlapping forked paths
	 * 
	 * <pre>
	 *      A
	 *      |
	 *      B
	 *     / \
	 *    |   |
	 *    G   C
	 *     \ / \
	 *      D   F
	 *      |   |
	 *       \ /
	 *        E
	 *        |
	 *        H
	 * </pre>
	 * 
	 * @return {@link Resource} for the created process
	 */
	public static Resource createOverlappingForkProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource init = addStartElement(model, rv);

		Resource actA = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, actA);
		model.add(init, RNRM.isFlowingTo, actA);

		Resource actB = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, actB);
		model.add(actA, RNRM.isFlowingTo, actB);

		Resource fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(actB, RNRM.isFlowingTo, fork);

		Resource actC = createActivityElementWithRedBarrel("C", rv);
		model.add(rv, RNRM.hasElement, actC);
		model.add(fork, RNRM.isFlowingTo, actC);

		Resource actG = createActivityElementWithBlueBarrel("G", rv);
		model.add(rv, RNRM.hasElement, actG);
		model.add(fork, RNRM.isFlowingTo, actG);

		fork = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork);
		model.add(actC, RNRM.isFlowingTo, fork);

		Resource join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(actG, RNRM.isFlowingTo, join);
		model.add(fork, RNRM.isFlowingTo, join);

		Resource actF = createActivityElementWithRedAndBlueBarrels("F", rv);
		model.add(rv, RNRM.hasElement, actF);
		model.add(fork, RNRM.isFlowingTo, actF);

		Resource actD = createActivityElementWithBlueBarrel("D", rv);
		model.add(rv, RNRM.hasElement, actD);
		model.add(join, RNRM.isFlowingTo, actD);

		join = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join);
		model.add(actD, RNRM.isFlowingTo, join);
		model.add(actF, RNRM.isFlowingTo, join);

		Resource actE = createActivityElementWithRedBarrel("E", rv);
		model.add(rv, RNRM.hasElement, actE);
		model.add(join, RNRM.isFlowingTo, actE);

		Resource actH = createActivityElementWithRedAndBlueBarrels("H", rv);
		model.add(rv, RNRM.hasElement, actH);
		model.add(actE, RNRM.isFlowingTo, actH);

		Resource close = addEndElement(model, rv);
		model.add(actH, RNRM.isFlowingTo, close);

		return rv;
	}

	/**
	 * Creates a Process with 6 Activities with invalid fork/joins
	 * 
	 * <pre>
	 *      A
	 *      |
	 *     ---
	 *     | |
	 *     B  \
	 *     |   \
	 *     --- |
	 *      |  |
	 *      C  F
	 *      |  |
	 *     --- |
	 *     |   |
	 *     D  /
	 *     | |
	 *     ---
	 *      |
	 *      E
	 * </pre>
	 * 
	 * @return {@link Resource} for the created process
	 */
	public static Resource createInvalidCardinalityProcess() {
		Resource rv = createNewProcess();
		Model model = rv.getModel();

		Resource start = addStartElement(model, rv);

		Resource act1 = createActivityElementWithRedBarrel("A", rv);
		model.add(rv, RNRM.hasElement, act1);
		model.add(start, RNRM.isFlowingTo, act1);

		Resource fork1 = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork1);
		model.add(act1, RNRM.isFlowingTo, fork1);

		Resource act2 = createActivityElementWithBlueBarrel("B", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(fork1, RNRM.isFlowingTo, act2);

		Resource join1 = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join1);
		model.add(act2, RNRM.isFlowingTo, join1);

		Resource act3 = createActivityElementWithBlueBarrel("C", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(join1, RNRM.isFlowingTo, act3);

		Resource fork2 = createForkElement(rv);
		model.add(rv, RNRM.hasElement, fork2);
		model.add(act3, RNRM.isFlowingTo, fork2);

		Resource act4 = createActivityElementWithRedBarrel("D", rv);
		model.add(rv, RNRM.hasElement, act4);
		model.add(fork2, RNRM.isFlowingTo, act4);

		Resource join2 = createJoinElement(rv);
		model.add(rv, RNRM.hasElement, join2);
		model.add(act4, RNRM.isFlowingTo, join2);

		Resource act5 = createActivityElementWithRedAndBlueBarrels("E", rv);
		model.add(rv, RNRM.hasElement, act5);
		model.add(join2, RNRM.isFlowingTo, act5);

		Resource end = addEndElement(model, rv);
		model.add(act5, RNRM.isFlowingTo, end);

		// add the erroneous path that skips joins
		Resource act6 = createActivityElementWithRedBarrel("F", rv);
		model.add(rv, RNRM.hasElement, act6);
		model.add(fork1, RNRM.isFlowingTo, act6);
		model.add(act6, RNRM.isFlowingTo, join2);

		return rv;
	}

	/**
	 * Creates a Process with 7 Activities where 1 is isolated
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C
	 *     \ /
	 *     / \     G
	 *    D   E
	 *     \ /
	 *      F
	 * </pre>
	 * 
	 * @return {@link Resource} for the created process
	 */
	public static Resource createProcessWithIsolatedActivity() {
		Resource rv = createBackToBackForkedProcess();
		Model model = rv.getModel();
		Resource isolatedActivity = createActivityElementWithActivity("G",
				rv);
		model.add(rv, RNRM.hasElement, isolatedActivity);
		return rv;
	}

	/**
	 * Creates a Process with 9 Activities, where 3 are in their own
	 * disconnected graph
	 * 
	 * <pre>
	 *      A
	 *     / \
	 *    B   C   G
	 *     \ /    |
	 *     / \    H
	 *    D   E   |
	 *     \ /    I
	 *      F
	 * </pre>
	 * 
	 * @return {@link Resource} for the created process
	 */
	public static Resource createProcessWithMultipleIsolatedActivities() {
		Resource rv = createBackToBackForkedProcess();
		Model model = rv.getModel();
		Resource act1 = createActivityElementWithActivity("G", rv);
		model.add(rv, RNRM.hasElement, act1);

		Resource act2 = createActivityElementWithActivity("H", rv);
		model.add(rv, RNRM.hasElement, act2);
		model.add(act1, RNRM.isFlowingTo, act2);

		Resource act3 = createActivityElementWithActivity("I", rv);
		model.add(rv, RNRM.hasElement, act3);
		model.add(act2, RNRM.isFlowingTo, act3);

		return rv;
	}

	/**
	 * Adds the given element to the given process
	 * 
	 * @param process
	 *            Process the element is being added to
	 * @param element
	 *            Element to add
	 */
	public static void addElement(Resource process, Resource element) {
		process.getModel().add(process, RNRM.hasElement, element);
	}

	/**
	 * Adds an rnrm:isFlowingTo edge from elem to nextElem
	 * 
	 * @param elem
	 * @param nextElem
	 */
	public static void addFlowsTo(Resource elem, Resource nextElem) {
		elem.getModel().add(elem, RNRM.isFlowingTo, nextElem);
	}

	/**
	 * Creates a start element and adds it to the given process
	 * 
	 * @param m
	 *            Model
	 * @param process
	 *            Resource for the process
	 * @return Resource of the start element
	 */
	public static Resource addStartElement(Model m, Resource process) {
		Resource rv = createJoinElement(process);
		m.add(process, RNRM.hasStartElement, rv);
		return rv;
	}

	/**
	 * Creates an end element and adds it to the given process
	 * 
	 * @param m
	 *            Model
	 * @param process
	 *            Resource for the process
	 * @return Resource of the end element
	 */
	public static Resource addEndElement(Model m, Resource process) {
		Resource rv = createForkElement(process);
		m.add(process, RNRM.hasEndElement, rv);
		return rv;
	}

	/**
	 * Creates a JoinElement resource
	 * 
	 * @return Resource for a new JoinElement
	 */
	public static Resource createJoinElement(Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "join" + count++);
		model.add(rv, RDF.type, RNRM.JoinElement);
		addElement(process, rv);
		return rv;
	}

	/**
	 * Creates a ForkElement resource
	 * 
	 * @return Resource for a new ForkElement
	 */
	public static Resource createForkElement(Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "fork" + count++);
		model.add(rv, RDF.type, RNRM.ForkElement);
		addElement(process, rv);
		return rv;
	}

	/**
	 * Creates a Decision Element resource
	 * 
	 * @param process
	 * @return Resource for the new Decision Element
	 */
	public static Resource createDecisionElement(Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "decision" + count++);
		model.add(rv, RDF.type, RNRM.DecisionElement);
		addElement(process, rv);
		return rv;
	}

	/**
	 * Creates a Merge Element resource
	 * 
	 * @param process
	 * @return Resource for the new Merge Element
	 */
	public static Resource createMergeElement(Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "decision" + count++);
		model.add(rv, RDF.type, RNRM.MergeElement);
		addElement(process, rv);
		return rv;
	}

	/**
	 * Creates an ObservableElement resource.
	 * 
	 * @return resource for a new ObservableElement.
	 */

	public static Resource createObservableResource(Model model) {
		Resource rv = model.createResource(NS + "observable" + count++);
		model.add(rv, RDF.type, RNRM.Observable);
		return rv;
	}

	public static Resource createActivityElementWithActivity(String label,
			Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "activityElem" + count++);
		model.add(rv, RDF.type, RNRM.ActivityElement);
		Resource act = model.createResource(NS + "activity" + count++);
		model.add(act, RDF.type, RNRM.Activity);
		model
				.add(act, RDFS.label, (null == label) ? "Activity" + count
						: label);
		model.add(rv, RNRM.representsActivity, act);
		addElement(process, rv);
		return rv;
	}
	
	public static Resource createActivityElement(Resource process) {
		Model model = process.getModel();
		Resource rv = model.createResource(NS + "activityElem" + count++);
		model.add(rv, RDF.type, RNRM.ActivityElement);
		addElement(process, rv);
		return rv;
	}

	/**
	 * Creates a new Activity with a Red Barrel observable
	 * 
	 * @return Resource for the ActivityElement with Activity and Observables
	 *         attached
	 */
	protected static Resource createActivityElementWithRedBarrel(String label,
			Resource process) {
		Resource rv = createActivityElement(process);
		Model model = process.getModel();
		Resource act = model.createResource(NS + "activity" + count++);
		model.add(act, RDF.type, RNRM.Activity);
		model.add(act, RDFS.label, (null == label) ? "Activity A" : label);
		model.add(act, RNRM.hasObservable, observable1);

		model.add(rv, RNRM.representsActivity, act);

		return rv;
	}

	/**
	 * Creates a new Activity with a Blue Barrel observable
	 * 
	 * @return Resource for the ActivityElement with Activity and Observables
	 *         attached
	 */
	protected static Resource createActivityElementWithBlueBarrel(String label,
			Resource process) {
		Resource rv = createActivityElement(process);
		Model model = process.getModel();
		Resource act = model.createResource(NS + "activity" + count++);
		model.add(act, RDF.type, RNRM.Activity);
		model.add(act, RDFS.label, (null == label) ? "Activity B" : label);
		model.add(act, RNRM.hasObservable, observable2);

		model.add(rv, RNRM.representsActivity, act);

		return rv;
	}

	/**
	 * Creates a new Activity with both a Red Barrel and Blue Barrel observables
	 * 
	 * @return Resource for the ActivityElement with Activity and Observables
	 *         attached
	 */
	protected static Resource createActivityElementWithRedAndBlueBarrels(
			String label, Resource process) {
		Resource rv = createActivityElement(process);
		Model model = process.getModel();
		Resource act = model.createResource(NS + "activity" + count++);
		model.add(act, RDF.type, RNRM.Activity);
		model.add(act, RDFS.label, (null == label) ? "Activity B" : label);
		model.add(act, RNRM.hasObservable, observable1);
		model.add(act, RNRM.hasObservable, observable2);

		model.add(rv, RNRM.representsActivity, act);

		return rv;
	}

	/**
	 * Creates a new Activity with 0 observables
	 * 
	 * @return Resource for the ActivityElement with Activity and no Observables
	 *         attached
	 */
	protected static Resource createActivityElementWithNoObservables(
			String label, Resource process) {
		Resource rv = createActivityElement(process);
		Model model = process.getModel();
		Resource act = model.createResource(NS + "activity" + count++);
		model.add(act, RDF.type, RNRM.Activity);
		model.add(act, RDFS.label, (null == label) ? "Activity B" : label);

		model.add(rv, RNRM.representsActivity, act);

		return rv;
	}
}
