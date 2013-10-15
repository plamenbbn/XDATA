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
package com.bbn.c2s2.pint.pf.generators;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.matrix.Vector;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.pf.PairGraph;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.pf.util.PowerIteration;

/**
 * This class builds {@link Cluster}'s based on the set of {@link Binding}'s
 * whose adjacency is contained in the {@link PairGraph}. The clusters group 
 * bindings who share many common neighbors in the pair graph. 
 * The expected use of this class is to create it and then call getClusters
 * 
 * @author reblace
 * 
 */
public class Clusterer  {

	private PairGraph pairGraph;
	private ClusterHeuristic clusterHeuristic;
	private List<ScoredBinding> scoredBindings = new ArrayList<ScoredBinding>();
	private List<Cluster> clusters = null;


	/**
	 * Create a new {@link Clusterer} given the {@link PairGraph} and 
	 * with the provided configuration
	 * @param pairGraph
	 * @param config
	 */
	public Clusterer(PairGraph pairGraph, ClusterHeuristic clusterHeuristic) {
		this.pairGraph = pairGraph;
		this.clusterHeuristic = clusterHeuristic;


		
		Vector v = PowerIteration.getEig(this.pairGraph.getAdjacencyMatrix());
		for (int i = 0; i < this.pairGraph.getNumberOfNodes(); i++) {
			ScoredBinding sb = new ScoredBinding(this.pairGraph.getBinding(i),
					v.get(i));

			scoredBindings.add(sb);
		}
		
		
		Collections.sort(scoredBindings);
	}

	/**
	 * Get the set of {@link Cluster}'s determined by the aggrement scores. 
	 * This method generates the list of clusters and doesn't persist them.
	 * @return A new list of clusters generated on the fly by checking
	 * the aggrement scores of the {@link Binding} objects in this {@link Clusterer}
	 */
	public List<Cluster> getClusters() {
		//int count = 0;
		if(null == clusters){
			
			
			clusters = new ArrayList<Cluster>();
			for (ScoredBinding sb : scoredBindings) {
				
				//***maybe not worth it***
				//if a scored binding has an edge set of size 0, it will not be added to cluster.
				//Further, there is no reason to make it a cluster head since no other bindings will be added to its cluster.
				//Further, since scoredBindings is sorted by score, we know when we reach a binding with zero edges,
				//all remaining bindings will have zero edges (since a binding with more edges has to have a higher
				//centrality score).
				//At this point, we can break.
				
//				if (count % 200 == 0) {
//					System.err.println(count);
//					System.err.println("num clusters: " +clusters.size());
//				}
				
				boolean addedToCluster = false;
				
				for (Cluster c : clusters) {
					
					//We can obtain an upper bound on the jaccard coefficient at this point.
					//It is min(c.getCenter().numEdges, sb.binding.numEdges) / 
					// [c.getCenter.numEdges + sb.binding.numEdges - min(c.getCenter().numEdges, sb.binding.numEdges)]
					//If this bound is lower than the required agreement threshold,
					//we do not need to compute the jaccard coefficent and we should not add the binding 
					//to the cluster.
					
//					double agreementScore = this.getAgreementScore(c.getCenter(),
//							sb.binding);
					
					if (this.clusterHeuristic.agreesWithCluster(sb.binding, c.getCenter(), pairGraph)) {
						addedToCluster = true;
						c.add(sb.binding);
					}
				}
				if (!addedToCluster) {
					
					//We can obtain an upper bound on the cluster size of a cluster centered at this binding.
					//It is sb.binding.numEdges.  This cluster is guaranteed to be filtered out if
					//sb.binding.numEdges < minClusterSize
					
					if (this.clusterHeuristic.isValidClusterHead(sb.binding, pairGraph)) {
						Cluster c = new Cluster(sb.binding);
						clusters.add(c);
					}
				}
				//count++;
			}
			
		}
		return clusters;
	}

	public String toString() {
		StringWriter sWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(sWriter);

		int cid = 0;
		for (Cluster c : getClusters()) {
			writer.println("!----------NEW CLUSTER-------------!");
			for (IBinding b : c.nodesInCluster) {
				writer.println(cid + " : " + b.getObservation().getLat() + " "
						+ b.toString());
			}
			cid++;
		}

		writer.close();
		return sWriter.toString();
	}

	/**
	 * A binding that is paired with a score
	 * @author reblace
	 *
	 */
	private class ScoredBinding implements Comparable<Clusterer.ScoredBinding> {
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
		public int compareTo(Clusterer.ScoredBinding arg0) {
			if (this.score > arg0.score)
				return -1;
			if (this.score < arg0.score)
				return 1;
			return 0;
		}
	}

	/**
	 * A cluster (a set of bindings)
	 * @author reblace
	 *
	 */
	public class Cluster {
		private List<Binding> nodesInCluster = new ArrayList<Binding>();

		/**
		 * Create a new cluster
		 * @param center The initial member of the cluster
		 */
		public Cluster(Binding center) {
			this.nodesInCluster.add(center);
		}

		/**
		 * Get the center (the initial member)
		 * @return
		 */
		public IBinding getCenter() {
			return this.nodesInCluster.get(0);
		}
		
		/**
		 * Get the list of {@link Binding} objects in this cluster
		 * @return The live list of {@link Binding} objects in this {@link Cluster}
		 */
		public List<Binding> getBindings() {
			return this.nodesInCluster;
		}

		/**
		 * Add the {@link Binding} to the {@link Cluster}
		 * @param b The {@link Binding} to add to the {@link Cluster}
		 */
		public void add(Binding b) {
			this.nodesInCluster.add(b);
		}
	}

}
