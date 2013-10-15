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
package com.bbn.c2s2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.pf.SolutionReport;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.jhuapl.c2s2.pp.observation.Observation;

public class TimelineGenerator {
	private final String EVENT_TEMPLATE_FILE = "conf/event-js-template.txt";
	private final String ENDL = System.getProperty("line.separator");
	private SampleDataLoader loader;

	public TimelineGenerator() {
		loader = new SampleDataLoader();
	}

	public void load(File observations, File noise, File truth)
			throws Exception {
		loader.loadObservations(observations, noise, truth);
	}

	public void writeEvents() throws Exception {
		StringBuilder rv = new StringBuilder("{" + ENDL + "'events' : [" + ENDL);
		String eventTemplate = readTemplateFile(new File(EVENT_TEMPLATE_FILE));

		String dateString;
		String titleString = "";
		String descString = "";
		String iconString = "http://static.simile.mit.edu/timeline/api-2.3.0/images/green-circle.png";
		// truth events
//		for (Observation o : loader.getTruthList()) {
		
		// all events
		for( Observation o : loader.getAllObservations() ) {
			titleString = KMLGenerator.uriToName(o.getUri());
			dateString = Constants.DATE_FORMAT_GREGORIAN.format(o
					.getObservationTimestamp().getTime());
			descString = dateString + "&lt;br/&gt;" + o.getObservableUri();
			rv.append(String.format(eventTemplate, dateString, titleString,
					descString, iconString));
		}

		// noise events
		// titleString = "";
		// iconString =
		// "http://static.simile.mit.edu/timeline/api-2.3.0/images/red-circle.png";
		// for(Observation o : loader.getNoiseList()) {
		// descString = KMLGenerator.uriToName(o.getUri()) + "\\n" +
		// o.getObservableUri();
		// dateString =
		// gregorianFormat.format(o.getObservationTimestamp().getTime());
		// rv.append(String.format(eventTemplate, dateString, titleString,
		// descString, iconString));
		// }

		// remaining events
		// iconString =
		// "http://static.simile.mit.edu/timeline/api-2.3.0/images/gray-circle.png";
		// for(Observation o : loader.getObservationHash().values()) {
		// descString = KMLGenerator.uriToName(o.getUri()) + "\\n" +
		// o.getObservableUri();
		// dateString =
		// gregorianFormat.format(o.getObservationTimestamp().getTime());
		// rv.append(String.format(eventTemplate, dateString, titleString,
		// descString, iconString));
		// }

		rv.delete(rv.lastIndexOf(","), rv.length() - 1);
		rv.append(ENDL + "]}");
		System.out.println(rv.toString());
	}

	private String readTemplateFile(File templateFile) throws Exception {
		StringBuilder rv = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		String line = "";
		while ((line = reader.readLine()) != null) {
			rv.append(line + ENDL);
		}
		return rv.toString();
	}
	
	
	private List<com.bbn.c2s2.pint.Observation> getPintObservations() throws Exception {
		ArrayList<com.bbn.c2s2.pint.Observation> pintObservations = new ArrayList<com.bbn.c2s2.pint.Observation>();
		Collection<Observation> observations = loader.getAllObservations();
		for( Observation o : observations ) {
			com.bbn.c2s2.pint.Observation po = new com.bbn.c2s2.pint.Observation(
					o.getUri(),
					o.getObservableUri(),
					o.getGeocode().getLatitude(),
					o.getGeocode().getLongitude(),
					o.getObservationTimestamp().getTime()
					);
			pintObservations.add( po );
		}
		return pintObservations;
	}

	private void runProcessFinder( Model m, RnrmProcess process ) throws Exception {
		
		RnrmWrapper rnrm = new RnrmWrapper(m);
		rnrm.getProcesses();
		
		Collection<com.bbn.c2s2.pint.Observation> observations = this.getPintObservations();
		
		File obsFile = new File( "observation_data.csv" );
		PrintWriter pr = new PrintWriter( new FileOutputStream( obsFile ) );
		System.out.println( "\n-------------------------------------------------\n" );
		System.out.println( "Observations locaded: format is {count,label,timestamp,lon,lat,URI,observableURI}" );
		int count = 1;
		pr.println( "count,label,timestamp,lon,lat,URI,observableURI" );
		for( com.bbn.c2s2.pint.Observation o : observations ) {
			pr.println( count++ + "," +  o.toCsvString() );
		}
		System.out.println( "Number of observations loaded: " + (count-1) + " - written to file: " + obsFile.getAbsolutePath() );
		System.out.println( "\n-------------------------------------------------\n" );
		pr.close();

		
		RnrmProcess proc = process; 											//RnrmProcessFactory.createProcess(m, TestDataPoint.getProcessUri(m));
		RdfProcess rdfProc = rnrm.getRdfProcess(proc.getProcessUri());
		Collection<Binding> bindings = BindingGroup.createBindingList( proc, observations, rdfProc );
		BindingGroup bindingGroup = new BindingGroup( bindings );

		PintConfiguration config = new PintConfiguration( getProperties() );
		ProcessFinder finder = new ProcessFinder( process, bindingGroup, config );
		
		List<SolutionReport> reports = finder.find();
		System.out.println( "------- Reports -------" );
		for( SolutionReport rep : reports ) {
			System.out.println( rep.toString() );
		}
		System.out.println( "------- End Reports -------" );
	}
	
	public static Properties getProperties() {
		Properties p = new Properties();
//		p.setProperty("pf.clusterer.agreement-threshold", "0.4");
//		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
//		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
//		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
//		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
//		p.setProperty("process.max.distance.km", "1.3");
//		p.setProperty("process.max.timespan.ms", "2.3");

		p.setProperty("pf.clusterer.agreement-threshold", "0.5");
		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
		p.setProperty("pf.generators.hconsistent.spatial-weight", "2.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.3");
		p.setProperty("process.max.distance.km", "3.5");
		p.setProperty("process.max.timespan.ms", "20.5");

		return p;
	}

	
	public static void main(String[] args) throws Exception {
		TimelineGenerator gen = new TimelineGenerator();
		gen.load( Constants.FILE_OBSERVATIONS_DATA, Constants.FILE_NOISE_CSV, Constants.FILE_TRUTH_CSV );
	
		//gen.writeEvents();
		
		
		Model m = RNRMLoader.loadProcesses( Constants.FILE_EXAMPLES_RDF );		
		Resource processResource = m.getResource("http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e");
		RdfProcess rdfProc = RdfProcess.create(processResource);
		RnrmProcess process = RnrmProcessFactory.createProcess(rdfProc);
		System.out.println( "\n------------PROCESS---------------\n" + process.toCsvString() );

		gen.runProcessFinder( m, process );
		
	}

}
