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

import junit.framework.TestCase;

import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.FilterHeuristic;
import com.bbn.c2s2.pint.testdata.PairGraphFactory;
import com.bbn.c2s2.pint.testdata.PintConfigurationFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author reblace
 *
 */
public class ClusterFilterTest extends TestCase {

	Clusterer cDisjoint2Set = null;
	Clusterer cZeroEdgeSetSize5 = null;
	RnrmProcess singleActProc = null;
	ClusterHeuristic clusterHeuristic = null;
	RnrmProcess process10 = null;
	FilterHeuristic h10 = null;
	RnrmProcess process5 = null;
	FilterHeuristic h5 = null;
	RnrmProcess process4 = null;
	FilterHeuristic h4 = null;
	RnrmProcess process2 = null;
	FilterHeuristic h2 = null;
	PintConfiguration config = null;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		config = PintConfigurationFactory.createPintConfiguration();
		singleActProc = TestRnrmProcessFactory.createSingleActivityProcess();
		clusterHeuristic = new ClusterHeuristic(singleActProc, config);
		process10 = TestRnrmProcessFactory.createSerialProcess(10);
		h10 = new FilterHeuristic(process10, config);
		process5 = TestRnrmProcessFactory.createSerialProcess(5);
		h5 = new FilterHeuristic(process5, config);
		process4 = TestRnrmProcessFactory.createSerialProcess(4);
		h4 = new FilterHeuristic(process4, config);
		process2 = TestRnrmProcessFactory.createSerialProcess(2);
		h2 = new FilterHeuristic(process2, config);
		cDisjoint2Set = new Clusterer(
				PairGraphFactory.createDisjointNSetGraph(3, 3, true), clusterHeuristic);
		cZeroEdgeSetSize5 = new Clusterer(
				PairGraphFactory.createZeroEdgePairGraph(5, true), clusterHeuristic);
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.generators.ClusterFilter#reset()}.
	 */
	public void testReset() {
		ClusterFilter filter = null;
		int size = 0;

		// count all the way through, should be 5 clusters
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		while(null != filter.next()){
			size++;
		}
		filter.reset();
		while(null != filter.next()){
			size++;
		}
		assertEquals(2*filter.validClusterCount(), size);
		filter.reset();
		assertNotNull(filter.next());
		
		// test incrementing partway through and then finishing
		size = 0;
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		while(null != filter.next()){
			size++;
		}
		filter.reset();
		while(null != filter.next()){
			size++;
		}
		assertEquals(2*filter.validClusterCount(), size);
		filter.reset();
		assertNotNull(filter.next());
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.generators.ClusterFilter#validClusterCount()}.
	 */
	public void testValidClusterCount() {
		ClusterFilter filter = null;

		// should have no valid clusters
		filter = new ClusterFilter(cDisjoint2Set.getClusters(), h10);
		assertEquals(0, filter.validClusterCount());
		
		// should have 6 valid cluster - 3 of size 2 and 3 of size 1
		filter = new ClusterFilter(cDisjoint2Set.getClusters(), h2);
		assertEquals(6, filter.validClusterCount());
		
		// should have 3 valid cluster - 3 of size 2 and 3 of size 1
		filter = new ClusterFilter(cDisjoint2Set.getClusters(), h5);
		assertEquals(3, filter.validClusterCount());
		
		// should have no valid clusters
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h10);
		assertEquals(0, filter.validClusterCount());
		
		//should have all valid clusters
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		assertEquals(5, filter.validClusterCount());
		
		//test doing some increments and then getting the count
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		int size = 0;
		while(filter.next() != null){
			size++;
		}
		assertEquals(size, filter.validClusterCount());
		assertNull(filter.next());
		
		//test counting part way and then getting the count, should be 5 clusters
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertEquals(5, filter.validClusterCount());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNull(filter.next());
		assertNull(filter.next());
		
		//test one where all the activities are the same
		Clusterer clusterer = new Clusterer(
				PairGraphFactory.createDisjointNSetGraph(3, 3, false), clusterHeuristic);
		filter = new ClusterFilter(clusterer.getClusters(), h4);
		assertEquals(0, filter.validClusterCount());
		
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.generators.ClusterFilter#next()}.
	 */
	public void testNext() {
		
		ClusterFilter filter = null;
		
		//test counting part way and then getting the count, should be 5 clusters
		filter = new ClusterFilter(cZeroEdgeSetSize5.getClusters(), h2);
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertEquals(5, filter.validClusterCount());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNull(filter.next());
		assertNull(filter.next());
		filter.reset();
		for(int i=0; i<5; i++){
			assertNotNull(filter.next());
		}
		assertNull(filter.next());
		assertNull(filter.next());
		
		//test when there are no valid clusters
		filter = new ClusterFilter(cDisjoint2Set.getClusters(), h10);
		assertNull(filter.next());
		assertNull(filter.next());
		filter.reset();
		assertNull(filter.next());
		assertNull(filter.next());
		
		//test when there are 50/50 valid clusters
		filter = new ClusterFilter(cDisjoint2Set.getClusters(), h5);
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNull(filter.next());
		assertNull(filter.next());
		filter.reset();
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNotNull(filter.next());
		assertNull(filter.next());
		assertNull(filter.next());
		
	}

}
