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
 * A very simple heuristic used to test the PairGraph object. This heuristic
 * computes an edge-weight of 1 if one Binding has an odd ID while the other
 * has an even ID. If both Bindings have an odd ID or both have an even ID,
 * then the result is 0.
 * 
 * @author tself
 * 
 */
public class EvenOddEdgeWeightHeuristic implements IEdgeWeightHeuristic {

	@Override
	public double getLinkWeight(Binding bindingA,
			Binding bindingB) {
		double rv = 0.0;
		if (evenAndOdd(bindingA, bindingB)
				|| evenAndOdd(bindingB, bindingA)) {
			rv = 1.0;
		}
		return rv;
	}

	private boolean evenAndOdd(Binding bindingA, Binding bindingB) {
		return (bindingA.getObservationID() % 2 == 0 && bindingB
				.getObservationID() % 2 == 1);
	}

	@Override
	public boolean linked(Binding bindingA,
			Binding bindingB) {
		return getLinkWeight(bindingA, bindingB) > 0;
	}
}
