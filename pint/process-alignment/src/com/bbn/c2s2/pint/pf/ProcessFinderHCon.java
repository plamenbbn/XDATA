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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.pf.evaluators.SolutionEvaluator;
import com.bbn.c2s2.pint.pf.generators.ClusterFilter;
import com.bbn.c2s2.pint.pf.generators.Clusterer;
import com.bbn.c2s2.pint.pf.generators.HConsistent;
import com.bbn.c2s2.pint.pf.generators.ISolutionGenerator;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.FilterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;

/**
 * ProcessFinder analyzes a set of {@link Binding} objects against an
 * {@link RnrmProcess} and identifies combinations of the {@link Binding}
 * objects that match the {@link RnrmProcess}.
 * 
 * @author jsherman
 * 
 */
public class ProcessFinderHCon {
	private RnrmProcess process;
	private BindingGroup bindings;
	private PintConfiguration config;
	private static Logger logger = LoggerFactory.getLogger(ProcessFinder.class);

	public ProcessFinderHCon(RnrmProcess process, BindingGroup bindings,
			PintConfiguration config) throws InvalidProcessException {
		if (null != process) {
			this.process = process;
		} else {
			throw new InvalidProcessException("Process cannot be null");
		}
		if (null != bindings) {
			this.bindings = bindings;
		} else {
			this.bindings = new BindingGroup(new ArrayList<Binding>());
		}
		this.config = config;
	}

	/**
	 * Executes the process-finding algorithm to build a list of
	 * {@link SolutionReport} objects where each report represents a potential
	 * process match.
	 * 
	 * @return List of {@link SolutionReport} objects representing each matched
	 *         process
	 */
	public List<SolutionReport> find() {

		// set up heuristics
		IEdgeWeightHeuristic pairGraphHeuristic = new EdgeWeightHeuristic(
				process, config, ActivityIndicationMap.create(bindings));
		ClusterHeuristic clusterHeuristic = new ClusterHeuristic(process,
				config);
		FilterHeuristic filterHeuristic = new FilterHeuristic(process, config);

		// set up data structure
		PairGraph pairGraph = new PairGraph(bindings, pairGraphHeuristic);

		// set up components
		Clusterer c = new Clusterer(pairGraph, clusterHeuristic);
		ClusterFilter f = new ClusterFilter(c.getClusters(), filterHeuristic);

		List<SolutionReport> reports = new ArrayList<SolutionReport>();
		Cluster k = null;
		while ((k = f.next()) != null) {
			ISolutionGenerator gen = new HConsistent(process, new BindingGroup(
					k.getBindings()), config);
			List<Solution> solutions = gen.generateSolutions();
			List<EvaluatedSolution> evalSolList = new ArrayList<EvaluatedSolution>(
					solutions.size());
			for (Solution aSolution : solutions) {
				double score = SolutionEvaluator.evaluate(aSolution);
				EvaluatedSolution es = new EvaluatedSolution(aSolution, score);
				if (null != es) {
					evalSolList.add(es);
				}
			}
			Collections.sort(evalSolList);
			reports.add(new SolutionReport(evalSolList.get(0)));
		}
		Collections.sort(reports);

		if (logger.isDebugEnabled()) {
			logger.debug("find() found: " + reports.size()
					+ " solution reports.");
		}
		return reports;
	}
}
