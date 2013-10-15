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
package com.bbn.c2s2.pint.perftest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.dataGen.TestDataConfig;
import com.bbn.c2s2.pint.dataGen.TestDataPoint;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.pint.testdata.chart.ScatterPlot;
import com.hp.hpl.jena.rdf.model.Model;

public class ProcessMultiplexerPerf {
	
	public static void main (String[] args) throws Exception {
		
		PintConfiguration config = new PintConfiguration(TestDataConfig.getProperties());		
		List<TestDataPoint> dataPoints = new ArrayList<TestDataPoint>();
		for (int i = 0 ; i < 400; i ++) {
			dataPoints.add(new TestDataPoint(i));
		}
		
		double[][] data = new double[2][dataPoints.size()];
		int i = 0;
		for (TestDataPoint p : dataPoints) {
			System.out.println("datapoint id: " + p.getId());
			Model m = p.getProcessModel();
			String uri = TestDataPoint.getProcessUri(m);
			RnrmWrapper rnrm = new RnrmWrapper(m);
			rnrm.getProcesses();
			Collection<Observation> observations = p.getObservations(m,uri);
			
			long startTime = System.currentTimeMillis();
			RnrmProcess proc = RnrmProcessFactory.createProcess(m, TestDataPoint.getProcessUri(m));
			BindingGroup bindings = new BindingGroup(BindingGroup
				.createBindingList(proc, observations, rnrm.getRdfProcess(proc.getProcessUri())));
			long endTime = System.currentTimeMillis();
			
			float duration = endTime - startTime;
			data[0][i] = bindings.bindingCount();
			data[1][i] = duration;
			String out = bindings.bindingCount() + "," + (endTime - startTime);
			System.out.println("(" + out + ")");
			i++;
		}
		ScatterPlot plot = new ScatterPlot("Multiplexer performance", "Bindings", "time (millis)", data);
		plot.toScreen();
		plot.toJpgFile("MultiplexerPerfTest.jpg", 500, 300);
		
	}
	

}
