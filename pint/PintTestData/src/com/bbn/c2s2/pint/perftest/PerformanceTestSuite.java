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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.dataGen.TestDataConfig;
import com.bbn.c2s2.pint.dataGen.TestDataPoint;
import com.bbn.c2s2.pint.pf.ActivityIndicationMap;
import com.bbn.c2s2.pint.pf.PairGraph;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.pf.SolutionReport;
import com.bbn.c2s2.pint.pf.generators.ClusterFilter;
import com.bbn.c2s2.pint.pf.generators.Clusterer;
import com.bbn.c2s2.pint.pf.generators.Clusterer.Cluster;
import com.bbn.c2s2.pint.pf.heuristics.ClusterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.EdgeWeightHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.FilterHeuristic;
import com.bbn.c2s2.pint.pf.heuristics.IEdgeWeightHeuristic;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.pint.testdata.chart.ScatterPlot;
import com.hp.hpl.jena.rdf.model.Model;

public class PerformanceTestSuite {
	
	private static final int NUM_DATA = 2; //00;
	private PintConfiguration config = new PintConfiguration(TestDataConfig.getProperties());		
	private List<TestDataPoint> dataPoints = new ArrayList<TestDataPoint>();

	public PerformanceTestSuite() {
		for (int i = 0; i < NUM_DATA; i++) {
			dataPoints.add(new TestDataPoint(i));
		}
	}
	
	public void runTests() throws Exception {
		double[][] data;
		ScatterPlot plot;
		/*
		System.out.println("running multiplexer test...");
		data = new double[2][dataPoints.size()];
		for (int i = 0; i < dataPoints.size(); i++) {
			runMultiplexer(dataPoints.get(i), data, i);
		}
		System.out.println("done.");
		plot = new ScatterPlot("Multiplexer performance", "Bindings", "time (millis)", data);
		plot.toJpgFile("MultiplexerPerfTest1.jpg", 1200, 900);
		plot.toCsvFile("MultiplexerPerfTest1.csv");
		
		System.out.println("running clusterer test...");
		data = new double[2][dataPoints.size()];
		for (int i = 0; i < dataPoints.size(); i++) {
			runClusterer(dataPoints.get(i), data, i);
		}
		System.out.println("done.");
		plot = new ScatterPlot("Clusterer performance", "Bindings", "time (millis)", data);
		plot.toJpgFile("ClustererPerfTest1.jpg", 1200, 900);
		plot.toCsvFile("ClustererPerfTest1.csv");		
		*/
		
		System.out.println("running system test...");
		data = new double[2][dataPoints.size()];
		for (int i = 0; i < dataPoints.size(); i++) {
			runSystem(dataPoints.get(i), data, i);
		}
		System.out.println("done.");
		plot = new ScatterPlot("System performance", "Bindings", "time (millis)", data);
		plot.toJpgFile("SystemPerfTest1.jpg", 1200, 900);
		plot.toCsvFile("SystemPerfTest1.csv");
		
		System.out.println( "=================== END SYSTEM TEST ========================" );

	}
	
	private void runMultiplexer(TestDataPoint p, double[][] data, int i) throws Exception {
		Model m = p.getProcessModel();
		String uri = TestDataPoint.getProcessUri(m);
		RnrmWrapper rnrm = new RnrmWrapper(m);
		rnrm.getProcesses();
		Collection<Observation> observations = p.getObservations(m, uri);	
		
		System.out.println( "\n-------------------------------------------------\n" );
		System.out.println( "Observations for Test Data Point #" + p.getId() + " count,{label,timestamp,lon,lat,URI,observableURI}" );
		int count = 1;
		for( Observation o : observations ) {
			System.out.println( count++ + "," +  o.toCsvString() );
		}
		
		long startTime = System.currentTimeMillis();
		RnrmProcess proc = RnrmProcessFactory.createProcess(m, TestDataPoint.getProcessUri(m));
		Collection<Binding> bindings = BindingGroup.createBindingList(proc, observations, rnrm.getRdfProcess(proc.getProcessUri()));
		BindingGroup bindingGroup = new BindingGroup( bindings );
		long endTime = System.currentTimeMillis();

		// Print the process here...
		System.out.println( "Process Tested:" + proc.getLabel() + " " + proc.getProcessUri() );
		System.out.println( proc.toCsvString() );
		System.out.println( );
		System.out.println( "Bindings for Test Data Point #" + p.getId() + " count,{observation,activity}" );
		count=1;
		for( Binding b : bindings ) {
			System.out.println( "\t" + count++ + "," + b.toCsvString() );
		}
		System.out.println( "BindingGroup: total binding count = " + bindingGroup.bindingCount() + " for "  + bindingGroup.getActivities().size() + " activities." );
		
		float duration = endTime - startTime;
		data[0][i] = bindingGroup.bindingCount();
		data[1][i] = duration;
	}
	
	private void runClusterer(TestDataPoint p, double[][] data, int i) throws Exception {
		Model m = p.getProcessModel();
		String uri = TestDataPoint.getProcessUri(m);
		RnrmWrapper rnrm = new RnrmWrapper(m);
		rnrm.getProcesses();
		Collection<Observation> observations = p.getObservations(m, uri);
		RnrmProcess proc = RnrmProcessFactory.createProcess(m, TestDataPoint.getProcessUri(m));
		BindingGroup bindingGroup = new BindingGroup(BindingGroup.createBindingList(proc, observations, rnrm.getRdfProcess(proc.getProcessUri())));
		
		System.out.println( "\n-------------------------------------------------\n" );
		System.out.println( "Observations for Test Data Point #" + p.getId() + " count,{label,timestamp,lon,lat,URI,observableURI}" );
		int count = 1;
		for( Observation o : observations ) {
			System.out.println( count++ + "," +  o.toCsvString() );
		}

		// Print the process here...
		System.out.println( "Process Tested:" + proc.getLabel() + " " + proc.getProcessUri() );
		System.out.println( proc.toCsvString() );
		System.out.println( );
		System.out.println( "Bindings for Test Data Point #" + p.getId() + " count,{observation,activity}" );
		count=1;
		for( Binding b : bindingGroup.getBindings() ) {
			System.out.println( "\t" + count++ + "," + b.toCsvString() );
		}
		System.out.println( "BindingGroup: total binding count = " + bindingGroup.bindingCount() + " for "  + bindingGroup.getActivities().size() + " activities." );

		
		long startTime = System.currentTimeMillis();
		ActivityIndicationMap map = ActivityIndicationMap.create(bindingGroup);
		IEdgeWeightHeuristic h = new EdgeWeightHeuristic(proc, config, map);
		PairGraph pg = new PairGraph(bindingGroup, h);
		Clusterer c = new Clusterer(pg, new ClusterHeuristic(proc, config));
		List<Cluster> cls = c.getClusters();
		ClusterFilter f = new ClusterFilter(cls, new FilterHeuristic(proc, config));
		long endTime = System.currentTimeMillis();
		
		float duration = endTime - startTime;
		data[0][i] = bindingGroup.bindingCount();
		data[1][i] = duration;
	}
	
	private void runSystem(TestDataPoint p, double[][] data, int i) throws Exception {
		Model m = p.getProcessModel();
		String uri = TestDataPoint.getProcessUri(m);
		RnrmWrapper rnrm = new RnrmWrapper(m);
		rnrm.getProcesses();
		Collection<Observation> observations = p.getObservations(m, uri);
		RnrmProcess proc = RnrmProcessFactory.createProcess(m, TestDataPoint.getProcessUri(m));
		BindingGroup bindingGroup = new BindingGroup(BindingGroup.createBindingList(proc, observations, rnrm.getRdfProcess(proc.getProcessUri())));
		
		
		System.out.println( "\n-------------------------------------------------\n" );
		System.out.println( "Observations for Test Data Point #" + p.getId() + " count,{label,timestamp,lon,lat,URI,observableURI}" );
		int count = 1;
		for( Observation o : observations ) {
			System.out.println( count++ + "," +  o.toCsvString() );
		}
		// Print the process here...
		System.out.println( );
		System.out.println( "Process Tested:" + proc.getLabel() + " " + proc.getProcessUri() );
		System.out.println( proc.toCsvString() );
		System.out.println( );
		System.out.println( "Bindings for Test Data Point #" + p.getId() + " count,{observation,activity}" );
		count=1;
		for( Binding b : bindingGroup.getBindings() ) {
			System.out.println( "\t" + count++ + "," + b.toCsvString() );
		}
		System.out.println( "BindingGroup: total binding count = " + bindingGroup.bindingCount() + " for "  + bindingGroup.getActivities().size() + " activities." );

		

		
		long startTime = System.currentTimeMillis();
		ProcessFinder processFinder = new ProcessFinder(proc, bindingGroup, config);
		List<SolutionReport> reports = processFinder.find();
		Collections.sort(reports);
		long endTime = System.currentTimeMillis();
		
		Thread.sleep(1000);
		
		System.out.println( "Solution reports:" );
		count=0;
		for( SolutionReport report : reports ) {
			count++;
			System.out.println( "Report " + count + "." );
			System.out.println( report.toString() );
			System.out.println( "-------------End Report " + count + "-----------------------/n" );
		}
		System.out.println( "\n-------------------------------------------------------------\n" );
		
		float duration = endTime - startTime;
		data[0][i] = bindingGroup.bindingCount();
		data[1][i] = duration;
		
	}
	
	
	public static List<Observation> readObservations( File inFile ) throws IOException {
		ArrayList<Observation> rv = new ArrayList<Observation>();
		DateFormat df = new SimpleDateFormat( "M/d/yyyy HH:mm" ); // example: "yyyy-MM-dd_HHmm_ss" 
		BufferedReader br = new BufferedReader( new FileReader(inFile) ) ;
		String line;
		int linecount = 0;
		while( (line=br.readLine()) != null ) {
			linecount++;
			String[] split = line.split(",");
			// skip the header line
			if( linecount == 1 )
				continue;
			//EXPECTS: Arrival Date,Arrival Time,Acquire Date,Acquire Time,Long,Lat,Observation,Role	
			String dateStr = split[0] + " " + split[1];
			//System.out.println( dateStr );
			Date timestamp = new Date( dateStr );
			double lon = Double.parseDouble( split[4] ); 
			double lat = Double.parseDouble( split[5] ); 
			String uri = "#obs" + linecount;
			String observableUri = "#observable_"+split[6].replace(" ", "_");
			Observation obs = new Observation(uri,observableUri,lat,lon,timestamp);
			rv.add( obs );
		}		
		return rv;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		File file = new File("combined-2.csv");
		Collection<Observation> observations = readObservations( file );
		System.out.println( "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++" );
		System.out.println( "Observations from file " + file.getAbsolutePath() + " count,{label,timestamp,lon,lat,URI,observableURI}" );
		int count = 1;
		for( Observation o : observations ) {
			System.out.println( count++ + "," +  o.toCsvString() );
		}
		System.out.println( "++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" );

		new PerformanceTestSuite().runTests(); 

	}

}
