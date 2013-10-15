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
import com.bbn.c2s2.pint.pf.generators.ClusterFilter;
import com.bbn.c2s2.pint.pf.generators.Clusterer;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.FilterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.OpposesEdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.util.Evaluator;

public class ProcessFinder {
	
	
	private RnrmProcess process;
	private BindingGroup bindings;
	private PintConfiguration config;
	private static Logger logger = LoggerFactory.getLogger(ProcessFinder.class);

	public ProcessFinder(RnrmProcess process, BindingGroup bindings,
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
		
		//set up heuristics
		IEdgeWeightHeuristic pairGraphHeuristic = new EdgeWeightHeuristic(process, config, ActivityIndicationMap.create(bindings));
		IEdgeWeightHeuristic opposesHeuristic = new OpposesEdgeWeightHeuristic(process, config, ActivityIndicationMap.create(bindings));
		ClusterHeuristic clusterHeuristic = new ClusterHeuristic(process, config);
		FilterHeuristic filterHeuristic = new FilterHeuristic(process, config);
		
		long startTime = System.currentTimeMillis();
		//set up data structure		
		System.out.print("\t...Preparing PairGraph with " + bindings.getBindings().size()  + " bindings...");
		PairGraph pairGraph = new PairGraph( bindings, pairGraphHeuristic);
		long endTime = System.currentTimeMillis();
		System.out.println( " done in " + (endTime-startTime) + " ms." );
		
		startTime = endTime;
		//set up components
		Clusterer c = new Clusterer(pairGraph, clusterHeuristic);
		List<Cluster> clusters = c.getClusters();
		endTime = System.currentTimeMillis();
		System.out.print("\t...Found " + clusters.size() + " clusters...");
		System.out.println( " in " + (endTime-startTime) + " ms." );

		
		startTime = endTime;
		ClusterFilter f = new ClusterFilter(clusters, filterHeuristic);
		System.out.print("\t...Filtered down to " + f.validClusterCount() + " valid clusters...");
		endTime = System.currentTimeMillis();
		System.out.println( " in " + (endTime-startTime) + " ms." );


		startTime = endTime;
		System.out.print("\t...Performing Quadratic Assignment on " + f.validClusterCount() + " clusters...");
		List<SolutionReport> reports = new ArrayList<SolutionReport>();
		Cluster k = null;
		int count = 0;
		while ((k = f.next()) != null) {
			count++;
//			System.out.println("Cluster " + count + ". Bindings in current cluster: " + k.getBindings().size());
			QuadraticAssignment gen = new QuadraticAssignment(process, k.getBindings(), opposesHeuristic);
			Solution solution = gen.solve();
			// Multiply the score by 100 to make the range 0.0 - 100.0
			double score = Evaluator.score(solution) * 100.0f;
			EvaluatedSolution es = new EvaluatedSolution(solution, score);
			reports.add(new SolutionReport(es));
		}
		endTime = System.currentTimeMillis();
		System.out.println( "done in " + (endTime-startTime) + " ms." );

		Collections.sort(reports);
		
		if(logger.isDebugEnabled()){
			logger.debug("find() found: " + reports.size() 
					+ " solution reports.");
		}
		return reports;
	}

}
