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
package com.bbn.c2s2.pint.pf.evaluators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.pf.EvaluatedSolution;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.pf.util.ContinuousSpatialViolationCalculator;
import com.bbn.c2s2.pint.pf.util.SimpleComCalculator;
import com.bbn.c2s2.pint.pf.util.ViolationRatios;

/**
 * Evaluates potential process solutions and determines a score.
 * 
 * @author tself
 * 
 */
public class SolutionEvaluator {

	private static Logger logger = LoggerFactory
			.getLogger(SolutionEvaluator.class);
	protected static final int ScoreRange = 100;

	/**
	 * Generate a list of {@link EvaluatedSolution}'s from a list of
	 * {@link Solution}'s
	 * 
	 * @param solutions
	 *            The {@link Solution}'s to evaluate
	 * @return The list of {@link EvaluatedSolution}'s
	 */
	public static List<EvaluatedSolution> getEvaluatedList(
			List<Solution> solutions) {

		List<EvaluatedSolution> evalList = new ArrayList<EvaluatedSolution>();

		for (Solution cs : solutions) {
			// get this solutions evaluation score
			double eval = evaluate(cs);
			EvaluatedSolution es = new EvaluatedSolution(cs, eval);
			if (null != es) {
				evalList.add(es);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("getEvaluatedList() generated a list of: "
					+ evalList.size() + " evaluated solutions.");
		}

		return evalList;
	}

	/**
	 * Evaluate the solution in order to assign a score of 1 to 100 The score
	 * indicates the strength of the solution in terms of its minimization of
	 * violations. Lower score indicates that the solution has more violations
	 * than a higher scored solution.
	 * 
	 * @param solution
	 *            The solution to evaluate
	 * @return A representation of the solution's score (between 1 and 100)
	 */
	public static double evaluate(Solution solution) {

		double toReturn = -1.0;

		// get a reference to the set of non-null bindings for this solution
		Collection<Binding> bindings = solution.getNonNullBindings();
		double[] center = SimpleComCalculator.getCenter(solution
				.getObservations());
		double cumulativeScore = 0.0;

		for (Binding b : bindings) {

			// The spatial portion of the score
			double spatialScore = ContinuousSpatialViolationCalculator
					.evaluateBinding(center, b);

			// The temporal portion of the score
			double temporalScore = ViolationRatios.evaluateTemporalValidRatio(
					solution, b);

			// The product of the scores
			double product_eval = spatialScore * temporalScore;

			// add the score of this binding to the cumulative score for this
			// sln
			cumulativeScore += product_eval;
		}

		double weightedPerf = 1.0;
		if (solution.getProcess().size() > 0) {
			weightedPerf = cumulativeScore
					/ (double) solution.getProcess().size();
		}

		toReturn = ScoreRange * weightedPerf;

		if (logger.isDebugEnabled()) {
			logger.debug("evaluate() created a score of: " + toReturn
					+ " for solution: " + solution.toString());
		}

		return toReturn;
	}

}
