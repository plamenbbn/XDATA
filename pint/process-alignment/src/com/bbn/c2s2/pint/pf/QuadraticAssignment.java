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
import java.util.List;

import no.uib.cipr.matrix.Vector;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.util.PairwiseConsistencyMetrics;
import com.bbn.c2s2.pint.pf.util.PowerIteration;

public class QuadraticAssignment {
	
	private class ScoredBinding implements Comparable<QuadraticAssignment.ScoredBinding> {
		public Binding binding;
		public double score;

		/**
		 * Create the new {@link ScoredBinding}
		 * @param binding The binding
		 * @param score The score of the binding
		 */
		public ScoredBinding(Binding binding, double score) {
			this.binding = binding;
			this.score = score;
		}

		@Override
		public int compareTo(QuadraticAssignment.ScoredBinding arg0) {
			if (this.score > arg0.score)
				return -1;
			if (this.score < arg0.score)
				return 1;
			return 0;
		}
	}
	
	private int bindingsLeft;
	private ArrayList<ScoredBinding> scoredBindings = new ArrayList<ScoredBinding>();
	private RnrmProcess process;
	
	public QuadraticAssignment(RnrmProcess process, List<Binding> bindings, IEdgeWeightHeuristic h) {
		this.process = process;
		PairGraph pairGraph = new PairGraph (bindings, h);
		Vector v = PowerIteration.getEig(pairGraph.getAdjacencyMatrix());
		for (int i = 0; i < pairGraph.getNumberOfNodes(); i++) {
			ScoredBinding sb = new ScoredBinding(pairGraph.getBinding(i),
					v.get(i));
			scoredBindings.add(sb);
		}
		bindingsLeft = scoredBindings.size();
		Collections.sort(scoredBindings);
	}
	
	public Solution solve() {
		Solution sol = new Solution(process);
		while (bindingsLeft > 0) {
			for (int i = 0; i < scoredBindings.size(); i++) {
				if (scoredBindings.get(i) != null) {
					ScoredBinding sb = scoredBindings.get(i);
					scoredBindings.set(i, null);
					bindingsLeft--;
					sol.addBinding(sb.binding);
					this.removeConflicts(sb, i + 1);
				}
			}
		}
		return sol;
	}
	
	private void removeConflicts(ScoredBinding sb, int startIndex) {
		for (int i = startIndex ; i < scoredBindings.size(); i++) {
			ScoredBinding t = scoredBindings.get(i);
			if (t != null) {
				if (sb.binding.getActivityID() == t.binding.getActivityID() || 
					sb.binding.getObservationID() == t.binding.getObservationID() ||
					PairwiseConsistencyMetrics.partialOrderViolation(process, sb.binding, t.binding) ||
					process.opposes(sb.binding.getActivity(), t.binding.getActivity())) {
					scoredBindings.set(i, null);
					bindingsLeft--;
				}
			}
		}
	}

}
