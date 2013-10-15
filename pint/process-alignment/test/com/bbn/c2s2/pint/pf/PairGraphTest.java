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

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import no.uib.cipr.matrix.Matrix;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.testdata.EvenOddEdgeWeightHeuristic;
import com.bbn.c2s2.pint.testdata.PairGraphFactory;

public class PairGraphTest extends TestCase {
	private final int BINDING_COUNT = 4;

	private BindingGroup bindingGroup;
	private PairGraph evenOddPairGraph;
	private PairGraph fullConnectedPairGraph;
	private PairGraph zeroEdgePairGraph;

	protected void setUp() throws Exception {
		super.setUp();
		bindingGroup = PairGraphFactory
				.createBindingGroup(BINDING_COUNT, false);
		evenOddPairGraph = PairGraphFactory.createEvenOddPairGraph(
				BINDING_COUNT, false);
		fullConnectedPairGraph = PairGraphFactory.createFullConnectedPairGraph(
				BINDING_COUNT, false);
		zeroEdgePairGraph = PairGraphFactory.createZeroEdgePairGraph(
				BINDING_COUNT, false);
	}

	public void testPairGraphRnrmProcessBindingGroupEdgeWeightHeuristic() {
		//		
		// Matrix mx = pg.getAdjacencyMatrix();
		// assertEquals("Num Rows", BINDING_COUNT, mx.numRows());
		// assertEquals("Num Columns", BINDING_COUNT, mx.numColumns());
	}

	public void testIndexOf() {
		PairGraph pg = new PairGraph(bindingGroup,
				new EvenOddEdgeWeightHeuristic());
		Activity act = new Activity(PairGraphFactory.ACTIVITY_ID, "");
		Observation o = new Observation("", "", 0.0, 0.0, new Date());
		// test existing
		System.err.println("testing existing");
		IBinding b = new Binding(act, o, BINDING_COUNT - 1);
		int index = pg.indexOf(b);
		assertTrue("Existing Binding", index >= 0 && index < BINDING_COUNT);
		// test non-existent
		b = new Binding(act, o, BINDING_COUNT + 1);
		index = pg.indexOf(b);
		assertEquals("Non-existent binding", -1, index);
	}

	public void testGetConnectedNodes() {
		/*
		 * Test with partial matrix <pre> 0 1 0 1 1 0 1 0 0 1 0 1 1 0 1 0 </pre>
		 */
		PairGraph pg = evenOddPairGraph;
		Set<Integer> nodes = pg.getConnectedNodes(0);
		assertTrue(nodes.contains(new Integer(1)));
		assertTrue(nodes.contains(new Integer(3)));
		assertFalse(nodes.contains(new Integer(0)));
		assertFalse(nodes.contains(new Integer(4)));

		// test with empty matrix (all zeros)
		pg = zeroEdgePairGraph;
		nodes = pg.getConnectedNodes(0);
		assertEquals("Zero Connected Nodes", 0, nodes.size());

		// test with full matrix (all ones)
		pg = fullConnectedPairGraph;
		nodes = pg.getConnectedNodes(0);
		assertEquals("All Connected Nodes", BINDING_COUNT - 1, nodes.size());
	}

	public void testGetBindings() {
		PairGraph pg = evenOddPairGraph;
		List<Binding> bindings = pg.getBindings();
		assertEquals("Populated Bindings", BINDING_COUNT, bindings.size());
	}

	public void testRemoveBindingsWithObservation() {
		int bindingIndex = BINDING_COUNT - 1;
		int obsId = BINDING_COUNT;
		PairGraph pg = fullConnectedPairGraph;
		Set<Integer> connected = pg.getConnectedNodes(bindingIndex);
		// binding should have connections before removing it
		assertTrue(connected.size() > 0);

		// remove one of the bindings
		pg.removeBindingsWithObservation(obsId);
		// ensure the binding no longer has any connections
		connected = pg.getConnectedNodes(bindingIndex);
		assertEquals("No connected nodes", 0, connected.size());
	}

	public void testGetBinding() {
		// test valid index
		PairGraph pg = fullConnectedPairGraph;
		Binding b = pg.getBinding(0);
		assertNotNull(b);
		assertEquals("Correct Binding", 1, b.getObservationID());

		// test invalid index
		boolean caught = false;
		try {
			b = pg.getBinding(BINDING_COUNT);
		} catch (IndexOutOfBoundsException e) {
			caught = true;
		}
		assertTrue("Exception Caught", caught);
	}

	public void testGetAdjacancyMatrix() {
		PairGraph pg = evenOddPairGraph;
		Matrix mx = pg.getAdjacencyMatrix();

		// test zero'd elements
		assertEquals("Zero'd Values", 0.0, mx.get(BINDING_COUNT - 1,
				BINDING_COUNT - 1));

		// test non-zero elements
		assertTrue("Non-zero Values", mx.get(BINDING_COUNT - 1,
				BINDING_COUNT - 2) > 0);
	}

	public void testGetPercentSparse() {
		// empty matrix
		PairGraph pg = zeroEdgePairGraph;
		assertEquals("Empty Matrix", 1.0, pg.getPercentSparse());

		// full matrix
		pg = fullConnectedPairGraph;
		assertEquals("Full Matrix", 0.0, pg.getPercentSparse());

		// half-populated matrix
		pg = evenOddPairGraph;
		int maxEdges = BINDING_COUNT * (BINDING_COUNT - 1);
		int actualEdges = BINDING_COUNT * BINDING_COUNT / 2;
		double expected = 1.0 - ((double) actualEdges / (double) maxEdges);
		assertEquals("Half-populated Matrix", expected, pg.getPercentSparse());
	}

	public void testGetNumberOfNodes() {
		PairGraph pg = fullConnectedPairGraph;
		assertEquals(BINDING_COUNT, pg.getNumberOfNodes());
	}

	public void testGetNumberOfEdges() {
		// empty matrix
		PairGraph pg = zeroEdgePairGraph;
		int expectedEdges = 0;
		assertEquals("Empty Matrix", expectedEdges, pg.getNumberOfEdges());

		// full matrix
		pg = fullConnectedPairGraph;
		expectedEdges = BINDING_COUNT * (BINDING_COUNT - 1);
		assertEquals("Full Matrix", expectedEdges, pg.getNumberOfEdges());

		// half-populated matrix
		pg = evenOddPairGraph;
		expectedEdges = (BINDING_COUNT * BINDING_COUNT) / 2;
		assertEquals("Half-populated Matrix", expectedEdges, pg
				.getNumberOfEdges());

		// remove edges from a node and test again
		pg = fullConnectedPairGraph;
		pg.removeBindingsWithObservation(BINDING_COUNT);
		expectedEdges = (BINDING_COUNT - 1) * (BINDING_COUNT - 2);
		assertEquals("After Removing Binding", expectedEdges, pg
				.getNumberOfEdges());
	}

	public void testPrintRows() {
		// populated matrix
		assertNotNull(evenOddPairGraph.printRows());

		// empty matrix
		assertNotNull("Empty matrix", zeroEdgePairGraph.printRows());
	}

	public void testPrintWeights() {
		// populated matrix
		assertNotNull(evenOddPairGraph.printWeights());

		// empty matrix
		assertNotNull("Empty matrix", zeroEdgePairGraph.printWeights());
	}
}
