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

package com.bbn.c2s2.pint.pf.heuristics;

import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.ActivityIndicationMap;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.testdata.BindingGroupFactory;
import com.bbn.c2s2.pint.testdata.PintConfigurationFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author reblace
 *
 */
public class EdgeWeightHeuristicTest extends TestCase {

	List<Binding> bindings = null;
	List<Binding> bindingsTemporal = null;
	List<Binding> bindingsSpatial = null;
	List<Binding> bindingsReversed = null;
	
	ActivityIndicationMap aiMap = null;
	ActivityIndicationMap aiMapTemporal = null;
	ActivityIndicationMap aiMapSpatial = null;
	PintConfiguration config = null;
	RnrmProcess process = null;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		BindingGroupFactory factory = new BindingGroupFactory();
		
		//spread over time and space
		config = PintConfigurationFactory.createPintConfiguration();
		process = TestRnrmProcessFactory.createSerialProcess(3);
		bindings = factory.generateBindings(0.0, 1000);
		bindingsTemporal = factory.generateBindings(0.0, 365*24*60*60*1000);
		bindingsSpatial = factory.generateBindings(5.0, 0);
		bindingsReversed = factory.generateBindings(0, -1000);
		aiMap = ActivityIndicationMap.create(new BindingGroup(bindings));
		aiMapTemporal = ActivityIndicationMap.create(new BindingGroup(bindingsTemporal));
		aiMapSpatial = ActivityIndicationMap.create(new BindingGroup(bindingsSpatial));
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic#linked(com.bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)}.
	 */
	public void testLinked() {
		EdgeWeightHeuristic h = new EdgeWeightHeuristic(process, config, aiMapSpatial);
		boolean linked = false;
		
		//test the same activity
		linked = h.linked(bindingsTemporal.get(0), bindingsTemporal.get(1));
		assertFalse(linked);
		
		//test the same observation id
		linked = h.linked(bindingsTemporal.get(0), bindingsTemporal.get(8));
		assertFalse(linked);
		
		//test the distance
		linked = h.linked(bindingsSpatial.get(0), bindingsSpatial.get(15));
		assertFalse(linked);
		
		//test the time difference
		linked = h.linked(bindingsTemporal.get(0), bindingsTemporal.get(15));
		assertFalse(linked);
		
		//test the partial order constraint
		linked = h.linked(bindingsReversed.get(0), bindingsReversed.get(15));
		assertFalse(linked);
		
		//test the actual calculation of the link weight
		linked = h.linked(bindings.get(0), bindings.get(4));
		assertTrue(linked);
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic#getLinkWeight(com.bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)}.
	 */
	public void testGetLinkWeight() {
		/*
		 * Test the simple case
		 */
		EdgeWeightHeuristic h = new EdgeWeightHeuristic(process, config, aiMapSpatial);
		double weight = -1;
		
		//test the same activity
		weight = h.getLinkWeight(bindingsTemporal.get(0), bindingsTemporal.get(1));
		assertEquals(0.0, weight);
		
		//test the same observation id
		weight = h.getLinkWeight(bindingsTemporal.get(0), bindingsTemporal.get(8));
		assertEquals(0.0, weight);
		
		//test the distance
		weight = h.getLinkWeight(bindingsSpatial.get(0), bindingsSpatial.get(15));
		assertEquals(0.0, weight);
		
		//test the time difference
		weight = h.getLinkWeight(bindingsTemporal.get(0), bindingsTemporal.get(15));
		assertEquals(0.0, weight);
		
		//test the partial order constraint
		weight = h.getLinkWeight(bindingsReversed.get(0), bindingsReversed.get(15));
		assertEquals(0.0, weight);
		
		//test the actual calculation of the link weight
		weight = h.getLinkWeight(bindings.get(0), bindings.get(4));
		assertTrue(weight < 1);
		assertTrue(weight > 0);
	}

}
