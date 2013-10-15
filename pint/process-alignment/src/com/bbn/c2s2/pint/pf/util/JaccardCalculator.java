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
package com.bbn.c2s2.pint.pf.util;

import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.pf.PairGraph;

/**
 * Calculates the Jaccard coefficient for 2 bindings within a PairGraph
 * 
 * @author tself
 * 
 */
public class JaccardCalculator {

	public static double getUpperBound(PairGraph pairGraph, IBinding b1,
			IBinding b2) {
		int[][] nonZeroIndecies = pairGraph.getNonZeroCols();
		double s1 = nonZeroIndecies[pairGraph.indexOf(b1)].length;
		double s2 = nonZeroIndecies[pairGraph.indexOf(b1)].length;
		double maxIntersection = Math.min(s1, s2);
		return maxIntersection / (s1 + s2 - maxIntersection);
	}

	/**
	 * Get the score of the agreement between the two bindings. The score is
	 * calculated as:
	 * 
	 * <pre>
	 *      B1 = set of nodes adjacent to b1
	 *      B2 = set of nodes adjacent to b2
	 *      U = B1 union B2
	 *      I = B1 intersection B2
	 *      Score(b1, b2) = |I| / (|U| - |I|)
	 * </pre>
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */

	public static double getJaccard(PairGraph pairGraph, IBinding b1, IBinding b2) {
		int vecSize = pairGraph.getBindings().size();
		int i1 = pairGraph.indexOf(b1);
		int[][] nonZeroIndecies = pairGraph.getNonZeroCols();
		Vector v1 = makeSparseVector(vecSize, nonZeroIndecies[i1]);
		int i2 = pairGraph.indexOf(b2);
		Vector v2 = makeSparseVector(vecSize, nonZeroIndecies[i2]);
		double count1 = nonZeroIndecies[i1].length;
		double count2 = nonZeroIndecies[i2].length;
		double intersection = v1.dot(v2);
		double jc = intersection / (count1 + count2 - intersection);
		if (Double.isNaN(jc))
			return 0;
		else
			return jc;
	}

	private static SparseVector makeSparseVector(int vecSize,
			int[] nonZeroIndecies) {
		double onesVec[] = new double[nonZeroIndecies.length];
		for (int i = 0; i < onesVec.length; i++)
			onesVec[i] = 1;
		return new SparseVector(vecSize, nonZeroIndecies, onesVec);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] nonZeroIndecies = { 0 };
		double[] vals = { 1 };
		Vector v1 = new SparseVector(100, nonZeroIndecies, vals);
		System.out.println(v1.dot(v1));
	}

}
