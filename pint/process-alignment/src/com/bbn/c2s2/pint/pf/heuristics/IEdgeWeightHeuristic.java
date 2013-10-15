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

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.RnrmProcess;

/**
 * Interface for a heuristic that can be used to compute the edge weight between
 * {@link Binding} objects in a graph.
 * 
 * @author tself
 * 
 */
public interface IEdgeWeightHeuristic {

	/**
	 * Determines whether the 2 given {@link Binding} objects should be linked
	 * in an agreement graph based on the given {@link RnrmProcess}.
	 * 
	 * @param bindingA
	 *            First {@link Binding}
	 * @param bindingB
	 *            Second {@link Binding}
	 * @return True if the given {@link Binding}s should be linked in an
	 *         agreement graph.
	 */
	public boolean linked(Binding bindingA,
			Binding bindingB);

	/**
	 * Computes the edge weight between the 2 given {@link Binding} objects.
	 * 
	 * @param bindingA
	 *            First {@link Binding}
	 * @param bindingB
	 *            Second {@link Binding}
	 * @return Value between 0 and 1 that represents the edge weight between
	 *         these 2 {@link Binding} objects. A value of 0 means that they are
	 *         not connected.
	 */
	public double getLinkWeight(Binding bindingA,
			Binding bindingB);

}
