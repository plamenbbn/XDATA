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

import junit.framework.TestCase;

import com.bbn.c2s2.pint.testdata.ProcessModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author tself
 *
 */
public class ProcessValidatorTest extends TestCase {
	// cycle detection
	private RdfProcess cycle1;
	private RdfProcess cycle2;
	private RdfProcess nonCycle1;
	private RdfProcess empty;
	
	// contiguous activities
	private RdfProcess contiguous;
	private RdfProcess isolatedActivity;
	private RdfProcess isolatedGraph;
	
	// cardinality violations
	private RdfProcess cardinalityNone;
	private RdfProcess cardinalityLeafActivity;
	private RdfProcess cardinalityLeafElement;
	private RdfProcess cardinalityBadStart;
	private RdfProcess cardinalityBadEnd;
	private RdfProcess cardinalityOverPopActivity;
	private RdfProcess cardinalityOverPopOpening;
	private RdfProcess cardinalityOverPopClosing;
	private RdfProcess cardinalityUnnecessaryElement;
	
	protected void setUp() throws Exception {
		super.setUp();
		cycle1 = RdfProcess.create(ProcessModelFactory.createCyclicProcessA());
		cycle2 = RdfProcess.create(ProcessModelFactory.createCyclicProcessB());
		nonCycle1 = RdfProcess.create(ProcessModelFactory.createBackToBackForkedProcess());
		empty = RdfProcess.create(ProcessModelFactory.createEmptyProcess());
		
		contiguous = RdfProcess.create(ProcessModelFactory.createBackToBackForkedProcess());
		isolatedActivity = RdfProcess.create(ProcessModelFactory.createProcessWithIsolatedActivity());
		isolatedGraph = RdfProcess.create(ProcessModelFactory.createProcessWithMultipleIsolatedActivities());
		
		// cardinality violation tests
		cardinalityNone = nonCycle1;
		
		// leaf activity
		cardinalityLeafActivity = RdfProcess.create(ProcessModelFactory.createForkedProcess());
		Model m = cardinalityLeafActivity.getModel();
		Resource proc = cardinalityLeafActivity.getProcessResource();
		Resource act = ProcessModelFactory.createActivityElementWithActivity("D", proc);
		ProcessModelFactory.addElement(proc, act);
		List<Resource> forks = cardinalityLeafActivity.getForkElements();
		Resource fork = forks.get(0);
		if(cardinalityLeafActivity.getEndElement().equals(fork)) {
			fork = forks.get(1);
		}
		ProcessModelFactory.addFlowsTo(fork, act);
		cardinalityLeafActivity.initialize();
		
		// leaf element
		cardinalityLeafElement = RdfProcess.create(ProcessModelFactory.createForkedProcess());
		m = cardinalityLeafElement.getModel();
		proc = cardinalityLeafElement.getProcessResource();
		Resource decision = ProcessModelFactory.createDecisionElement(proc);
		ProcessModelFactory.addElement(proc, decision);
		forks = cardinalityLeafElement.getForkElements();
		fork = forks.get(0);
		if(cardinalityLeafElement.getEndElement().equals(fork)) {
			fork = forks.get(1);
		}
		ProcessModelFactory.addFlowsTo(fork, decision);
		cardinalityLeafElement.initialize();
		
		// bad start
		cardinalityBadStart = RdfProcess.create(ProcessModelFactory.createForkedProcess());
		m = cardinalityBadStart.getModel();
		proc = cardinalityBadStart.getProcessResource();
		Resource start = cardinalityBadStart.getStartElement();
		act = ProcessModelFactory.createActivityElementWithActivity("Foo", proc);
		ProcessModelFactory.addElement(proc, act);
		ProcessModelFactory.addFlowsTo(act, start);
		cardinalityBadStart.initialize();
		
		// bad end
		cardinalityBadEnd = RdfProcess.create(ProcessModelFactory.createForkedProcess());
		m = cardinalityBadEnd.getModel();
		proc = cardinalityBadEnd.getProcessResource();
		Resource end = cardinalityBadEnd.getEndElement();
		act = ProcessModelFactory.createActivityElementWithActivity("Foo", proc);
		ProcessModelFactory.addElement(proc, act);
		ProcessModelFactory.addFlowsTo(end, act);
		cardinalityBadEnd.initialize();
		
		// overpopulated activity
		cardinalityOverPopActivity = RdfProcess.create(ProcessModelFactory.createForkedProcess());
		m = cardinalityOverPopActivity.getModel();
		proc = cardinalityOverPopActivity.getProcessResource();
		act = cardinalityOverPopActivity.getActivities().get(0);
		Resource act2 = cardinalityOverPopActivity.getActivities().get(1);
		forks = cardinalityOverPopActivity.getForkElements();
		fork = forks.get(0);
		if(cardinalityOverPopActivity.getEndElement().equals(fork)) {
			fork = forks.get(1);
		}
		Resource fork2 = ProcessModelFactory.createForkElement(proc);
		ProcessModelFactory.addElement(proc, fork2);
		ProcessModelFactory.addFlowsTo(fork, fork2);
		ProcessModelFactory.addFlowsTo(fork2, act);		
		ProcessModelFactory.addFlowsTo(fork2, act2);
		cardinalityOverPopActivity.initialize();
		
		// overpopulated open
		proc = ProcessModelFactory.createNewProcess();
		m = proc.getModel();
		start = ProcessModelFactory.addStartElement(m, proc);
		end = ProcessModelFactory.addEndElement(m, proc);
		fork = ProcessModelFactory.createForkElement(proc);
		ProcessModelFactory.addElement(proc, fork);
		decision = ProcessModelFactory.createDecisionElement(proc);
		ProcessModelFactory.addElement(proc, decision);
		act = ProcessModelFactory.createActivityElementWithActivity("A", proc);
		act2 = ProcessModelFactory.createActivityElementWithActivity("B", proc);
		ProcessModelFactory.addElement(proc, act);
		ProcessModelFactory.addElement(proc, act2);
		ProcessModelFactory.addFlowsTo(start, fork);
		ProcessModelFactory.addFlowsTo(fork, act);
		ProcessModelFactory.addFlowsTo(fork, act2);
		ProcessModelFactory.addFlowsTo(act, decision);
		ProcessModelFactory.addFlowsTo(act2, decision);
		Resource act3 = ProcessModelFactory.createActivityElementWithActivity("C", proc);
		ProcessModelFactory.addElement(proc, act3);
		Resource act4 = ProcessModelFactory.createActivityElementWithActivity("D", proc);
		ProcessModelFactory.addElement(proc, act4);
		ProcessModelFactory.addFlowsTo(decision, act3);
		ProcessModelFactory.addFlowsTo(decision, act4);
		Resource merge = ProcessModelFactory.createMergeElement(proc);
		ProcessModelFactory.addElement(proc, merge);
		ProcessModelFactory.addFlowsTo(act3, merge);
		ProcessModelFactory.addFlowsTo(act4, merge);
		ProcessModelFactory.addFlowsTo(merge, end);
		cardinalityOverPopOpening = RdfProcess.create(proc);
		
		// overpopulated close
		proc = ProcessModelFactory.createNewProcess();
		m = proc.getModel();
		start = ProcessModelFactory.addStartElement(m, proc);
		end = ProcessModelFactory.addEndElement(m, proc);
		fork = ProcessModelFactory.createForkElement(proc);
		ProcessModelFactory.addElement(proc, fork);
		act = ProcessModelFactory.createActivityElementWithActivity("A", proc);
		act2 = ProcessModelFactory.createActivityElementWithActivity("B", proc);
		ProcessModelFactory.addElement(proc, act);
		ProcessModelFactory.addElement(proc, act2);
		ProcessModelFactory.addFlowsTo(start, fork);
		ProcessModelFactory.addFlowsTo(fork, act);
		ProcessModelFactory.addFlowsTo(fork, act2);
		merge = ProcessModelFactory.createMergeElement(proc);
		ProcessModelFactory.addElement(proc, merge);
		ProcessModelFactory.addFlowsTo(act, merge);
		ProcessModelFactory.addFlowsTo(act2, merge);		
		act3 = ProcessModelFactory.createActivityElementWithActivity("C", proc);
		ProcessModelFactory.addElement(proc, act3);
		act4 = ProcessModelFactory.createActivityElementWithActivity("D", proc);
		ProcessModelFactory.addElement(proc, act4);		
		ProcessModelFactory.addFlowsTo(merge, act3);
		ProcessModelFactory.addFlowsTo(merge, act4);
		Resource join = ProcessModelFactory.createJoinElement(proc);
		ProcessModelFactory.addElement(proc, join);
		ProcessModelFactory.addFlowsTo(act3, join);
		ProcessModelFactory.addFlowsTo(act4, join);
		ProcessModelFactory.addFlowsTo(join, end);
		cardinalityOverPopClosing = RdfProcess.create(proc);
		
		
		// unnecessary element
		proc = ProcessModelFactory.createNewProcess();
		start = ProcessModelFactory.addStartElement(proc.getModel(), proc);
		end = ProcessModelFactory.addEndElement(proc.getModel(), proc);
		act = ProcessModelFactory.createActivityElementWithActivity("A", proc);
		fork = ProcessModelFactory.createForkElement(proc);
		ProcessModelFactory.addFlowsTo(start, fork);
		ProcessModelFactory.addFlowsTo(fork, act);
		ProcessModelFactory.addFlowsTo(act, end);
		cardinalityUnnecessaryElement = RdfProcess.create(proc);
		
		
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.rdf.ProcessValidator#containsCycle(com.bbn.c2s2.pint.rdf.RdfProcess)}.
	 */
	public void testContainsCycle() {
		// empty
		assertFalse(ProcessValidator.containsCycle(empty));
		
		// forks/joins, but no cycles
		assertFalse(ProcessValidator.containsCycle(nonCycle1));
		
		// cycle via overloaded activity
		assertTrue(ProcessValidator.containsCycle(cycle1));
		
		// cycle via fork/join
		assertTrue(ProcessValidator.containsCycle(cycle2));
		
	}
	
	public void testHasContiguousActivities() {
		// contiguous
		assertTrue(ProcessValidator.hasContiguousActivities(contiguous));
		
		// single isolated activity
		assertFalse(ProcessValidator.hasContiguousActivities(isolatedActivity));

		// isolated graph of activities
		assertFalse(ProcessValidator.hasContiguousActivities(isolatedGraph));
	}
	
	public void testViolatesCardinality() {
		// no violations
		assertFalse("No Violations", ProcessValidator.violatesCardinality(cardinalityNone));
		
		// leaf activity
		assertTrue("Leaf Activity", ProcessValidator.violatesCardinality(cardinalityLeafActivity));
		
		// leaf element
		assertTrue("Leaf Element", ProcessValidator.violatesCardinality(cardinalityLeafElement));
		
		// bad start
		assertTrue("Non-terminated Start", ProcessValidator.violatesCardinality(cardinalityBadStart));
		
		// bad end
		assertTrue("Non-terminated End", ProcessValidator.violatesCardinality(cardinalityBadEnd));
		
		// overpopulated activity
		assertTrue("Overpopulated Activity", ProcessValidator.violatesCardinality(cardinalityOverPopActivity));
		
		// overpopulated opening
		assertTrue("Overpopulated Opening Element", ProcessValidator.violatesCardinality(cardinalityOverPopOpening));
		
		// overpopulated closing
		assertTrue("Overpopulated Closing Element", ProcessValidator.violatesCardinality(cardinalityOverPopClosing));
		
		// unnecessary fork/join/decision/merge
		assertFalse("Unnecessary Element", ProcessValidator.violatesCardinality(cardinalityUnnecessaryElement));
	}

}
