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

import java.util.List;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.PairGraph;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.testdata.PairGraphFactory;
import com.bbn.c2s2.pint.testdata.PintConfigurationFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author reblace
 * 
 */
public class ClustererTest extends TestCase {

	PairGraph pZero = null;
	PairGraph pEvenOdd = null;
	PairGraph pFull = null;
	
	RnrmProcess process;
	
	PintConfiguration config = null;
	ClusterHeuristic heuristic = null;
	PintConfiguration configHighAgreement = null;
	ClusterHeuristic highAgreementHeuristic = null;
	PintConfiguration configLowAgreement = null;
	ClusterHeuristic lowAgreementHeuristic = null;
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		
		pZero = PairGraphFactory.createZeroEdgePairGraph(6, false);
		pEvenOdd = PairGraphFactory.createEvenOddPairGraph(6, false);
		pFull = PairGraphFactory.createFullConnectedPairGraph(6, false);
		
		process = TestRnrmProcessFactory.createSingleActivityProcess();
		
		config = PintConfigurationFactory.createPintConfiguration();
		heuristic = new ClusterHeuristic(process, config);
		configHighAgreement = PintConfigurationFactory
				.createPintConfigurationAgreementThreshold(Double.MAX_VALUE);
		highAgreementHeuristic = new ClusterHeuristic(process, configHighAgreement);
		configLowAgreement = PintConfigurationFactory
				.createPintConfigurationAgreementThreshold(-Double.MAX_VALUE);
		lowAgreementHeuristic = new ClusterHeuristic(process, configLowAgreement);
	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.generators.Clusterer#getClusters()}.
	 */
	public void testGetClusters() {

		/**
		 * Test the zero graph
		 */
		Clusterer clusterer = new Clusterer(pZero, heuristic);
		List<Cluster> clusters = clusterer.getClusters();

		assertEquals(pZero.getBindings().size(), clusters.size());
		for (Cluster cluster : clusters) {
			assertEquals(1, cluster.getBindings().size());
		}

		/**
		 * Test the full graph
		 */
		clusterer = new Clusterer(pFull, heuristic);
		clusters = clusterer.getClusters();

		// assumes that all satisfy the threshold
		assertEquals(1, clusters.size());
		for (Cluster cluster : clusters) {
			assertEquals(pFull.getBindings().size(), cluster.getBindings()
					.size());
		}

		/**
		 * Test the even/odd graph
		 */
		clusterer = new Clusterer(pEvenOdd, heuristic);
		List<Binding> bindings = pEvenOdd.getBindings();
		clusters = clusterer.getClusters();

		double evenCount = Math.ceil((double) bindings.size() / 2.0);
		double oddCount = Math.floor((double) bindings.size() / 2.0);
		assertEquals(2, clusters.size());
		for (Cluster cluster : clusters) {
			if (cluster.getBindings().size() > 0
					&& cluster.getBindings().get(0).getObservationID() % 2 == 0) {
				// even cluster
				assertEquals((int) evenCount, cluster.getBindings().size());
			} else {
				// odd cluster
				assertEquals((int) oddCount, cluster.getBindings().size());
			}
		}

		/**
		 * Test a graph with a really high threshold and full graph
		 */
		clusterer = new Clusterer(pFull, highAgreementHeuristic);
		clusters = clusterer.getClusters();

		// assumes that none will cluster
		assertEquals(pFull.getBindings().size(), clusters.size());
		for (Cluster cluster : clusters) {
			assertEquals(1, cluster.getBindings().size());
		}
		
		/**
		 * Test a graph with a really high threshold and full graph
		 */
		clusterer = new Clusterer(pZero, lowAgreementHeuristic);
		clusters = clusterer.getClusters();

		// assumes that none will cluster
		assertEquals(1, clusters.size());
		for (Cluster cluster : clusters) {
			assertEquals(pZero.getBindings().size(), cluster.getBindings().size());
		}
	}

	/**
	 * Test method for
	 * {@link com.bbn.c2s2.pint.pf.generators.Clusterer#toString()}.
	 */
	public void testToString() {
		Clusterer clusterer = new Clusterer(pZero, heuristic);
		String val = clusterer.toString();
		assertNotNull(val);

		clusterer = new Clusterer(pFull, heuristic);
		val = clusterer.toString();
		assertNotNull(val);

		clusterer = new Clusterer(pEvenOdd, heuristic);
		val = clusterer.toString();
		assertNotNull(val);
	}

}
