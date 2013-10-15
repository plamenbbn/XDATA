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

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;

/**
 * This heuristic will generate a pair graph consisting of a set of
 * equal-sized node clusters.
 * @author reblace
 *
 */
public class DisjointNSetHeuristic implements IEdgeWeightHeuristic {

	private int[][] weights;
	
	public DisjointNSetHeuristic(int numSets, int setSize){
		int size = numSets*setSize;
		weights = new int[size][size];
		for(int i=0; i<size; i++){
			for(int j=i; j<size; j++){
				if(j == i + 1 && j % setSize != 0){
					weights[i][j] = weights[j][i] = 1;
				}else{
					weights[i][j] = weights[j][i] = 0;
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic#getLinkWeight(com.bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)
	 */
	@Override
	public double getLinkWeight(Binding bindingA,
			Binding bindingB) {
		return weights[bindingA.getObservationID()-1]
		               [bindingB.getObservationID()-1];
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic#linked(com.bbn.c2s2.pint.pf.RnrmProcess, com.bbn.c2s2.pint.Binding, com.bbn.c2s2.pint.Binding)
	 */
	@Override
	public boolean linked(Binding bindingA,
			Binding bindingB) {
		return getLinkWeight( bindingA, bindingB) > 0;
	}

}
