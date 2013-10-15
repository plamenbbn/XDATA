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


/**
 * A solution that has been assigned a score.
 * 
 * @author reblace
 * 
 */
public class EvaluatedSolution extends Solution implements
		Comparable<EvaluatedSolution> {

	private static final long serialVersionUID = -3294889038304596451L;
	double _evaluation = 0.0;

	/**
	 * Creates a new EvaluatedSolution using the given {@link Solution} and
	 * provided score
	 * 
	 * @param solution
	 *            {@link Solution} which is being scored
	 * @param eval
	 *            Score to assign. Must be non-negative.
	 * @throws InvalidSolutionScoreException
	 *             If negative score provided.
	 */
	public EvaluatedSolution(Solution solution, double eval) {
		super(solution);
		if (eval < 0.0) {
			throw new IllegalArgumentException(
					"Invalid Solution Score. Score must be non-negative.");
		}
		_evaluation = eval;
	}

	/**
	 * Gets the score for this {@link Solution}
	 * 
	 * @return Returns the evaluation score associated with this
	 *         {@link Solution}
	 */
	public double getScore() {
		return _evaluation;
	}

	/**
	 * Compares solutions based on their numeric scores.
	 */
	@Override
	public int compareTo(EvaluatedSolution arg) {
		if (_evaluation > arg.getScore()) {
			return -1;
		}
		if (_evaluation < arg.getScore()) {
			return 1;
		}
		return 0;
	}

}
