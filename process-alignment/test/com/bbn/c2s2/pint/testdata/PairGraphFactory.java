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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.PairGraph;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;

/**
 * Creates PairGraph objects for testing
 * 
 * @author tself
 * 
 */
public class PairGraphFactory {
	/**
	 * Activity ID added to all {@link Activity} objects created for
	 * {@link Binding}s.
	 */
	public static final int ACTIVITY_ID = 15;
	private static IEdgeWeightHeuristic evenOddHeuristic;
	private static IEdgeWeightHeuristic alwaysOneHeuristic;
	private static IEdgeWeightHeuristic alwaysZeroHeuristic;
	private static RnrmProcess process;

	static {
		evenOddHeuristic = new EvenOddEdgeWeightHeuristic();
		alwaysOneHeuristic = new SameValueHeuristic(1.0);
		alwaysZeroHeuristic = new SameValueHeuristic(0.0);
		process = TestRnrmProcessFactory.createSerialProcess();
	}

	/**
	 * Creates a {@link PairGraph} such that there are 'setCount' sets of size
	 * 'setSize' of connected nodes. Each node set is basically a chain of
	 * nodes. As such, the size of the node chain will dictate the specific
	 * characteristics of the pair graph.
	 * 
	 * @param setCount
	 *            The number of sets
	 * @param setSize
	 *            The size of the sets
	 * @param sequentialActivityIds
	 *            whether the activity ids should be sequential or all the same
	 * @return PairGraph object
	 */
	public static PairGraph createDisjointNSetGraph(int setCount, int setSize,
			boolean sequentialActivityIds) {
		IEdgeWeightHeuristic heuristic = new DisjointNSetHeuristic(setCount,
				setSize);
		return new PairGraph(createBindingGroup(setSize * setCount,
				sequentialActivityIds), heuristic);
	}

	/**
	 * Creates a {@link PairGraph} such that edges are created between
	 * {@link Binding} objects only if one has an odd {@link Observation} ID and
	 * the other has an even {@link Observation} ID. Observation IDs are
	 * numbered incrementally from 1-n where n is the number of {@link Binding}
	 * nodes.
	 * 
	 * <pre>
	 * Example with 4 Bindings:
	 *    B1 B2 B3 B4
	 * B1  0  1  0  1
	 * B2  1  0  1  0
	 * B3  0  1  0  1
	 * B4  1  0  1  0
	 * </pre>
	 * 
	 * @param numBindings
	 *            Number of {@link Binding} objects to use
	 * @param sequentialActivityIds
	 *            whether the activity ids should be sequential or all the same
	 * @return PairGraph object
	 */
	public static PairGraph createEvenOddPairGraph(int numBindings,
			boolean sequentialActivityIds) {
		return new PairGraph(createBindingGroup(numBindings,
				sequentialActivityIds), evenOddHeuristic);
	}

	/**
	 * Creates a {@link PairGraph} such that edges of weight 1.0 are created
	 * between all {@link Binding} objects. Observation IDs are numbered
	 * incrementally from 1-n where n is the number of {@link Binding} nodes.
	 * 
	 * @param numBindings
	 *            Number of {@link Binding} objects to use
	 * @param sequentialActivityIds
	 *            whether the activity ids should be sequential or all the same
	 * @return PairGraph object
	 */
	public static PairGraph createFullConnectedPairGraph(int numBindings,
			boolean sequentialActivityIds) {
		return new PairGraph(createBindingGroup(numBindings,
				sequentialActivityIds), alwaysOneHeuristic);
	}

	/**
	 * Creates a {@link PairGraph} such that no edges are created between
	 * {@link Binding} objects. Observation IDs are numbered incrementally from
	 * 1-n where n is the number of {@link Binding} nodes.
	 * 
	 * @param numBindings
	 *            Number of {@link Binding} objects to use
	 * @return PairGraph object
	 */
	public static PairGraph createZeroEdgePairGraph(int numBindings,
			boolean sequentialActivityIds) {
		return new PairGraph(createBindingGroup(numBindings,
				sequentialActivityIds), alwaysZeroHeuristic);
	}

	/**
	 * Creates a {@link BindingGroup} with the given number of {@link Binding}
	 * objects.
	 * 
	 * @param numBindings
	 *            Number of {@link Binding} objects to include
	 * @param uniqueActivityIds
	 *            If true, sequential activity id's are used starting at 0. If
	 *            false, ACTIVITY_ID is used
	 * @return Created BindingGroup
	 */
	public static BindingGroup createBindingGroup(int numBindings,
			boolean uniquelActivityIds) {
		List<Binding> bindings = new ArrayList<Binding>(numBindings);
		int activityId = 0;
		for (int i = 0; i < numBindings; i++) {
			Observation o = new Observation("http://test#observation",
					"http://test#observable", 0.0, 0.0, new Date());
			Activity act = new Activity((uniquelActivityIds) ? activityId++
					: ACTIVITY_ID, "http://test#activity");
			bindings.add(new Binding(act, o, i + 1));
		}
		return new BindingGroup(bindings);
	}
}
