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
package com.bbn.c2s2.pint.client.cmd;

import java.util.Collections;
import java.util.List;

import com.bbn.c2s2.TestDataConfig;
import com.bbn.c2s2.TestProcessBuilder;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.ISolutionReport;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.dataGen.DataGenConfig;
import com.bbn.c2s2.pint.dataGen.DataSetGenerator;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.pf.SolutionReport;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;


public class QATest {
	
	
	public static void main (String[] args ) throws Exception {
		
		PintConfiguration config = new PintConfiguration(TestDataConfig.getProperties());
		
		System.out.print("Building processes model...");
		TestProcessBuilder pb = new TestProcessBuilder("testProc");
		pb.buildTest();
		System.out.println("Done.");
		
		System.out.print("Validating model...");
		RdfProcess rdfProc = RdfProcess.create(RDFHelper.getResource(pb.getModel(), "testProc"));
		System.out.println("Done.");
		
		System.out.print("Building RNRM Process...");
		RnrmProcess rnrmProc = RnrmProcessFactory.createProcess(rdfProc);
		System.out.println("Done.");
		
		DataGenConfig dConfig = new DataGenConfig();
		dConfig.setSignalNoiseRatio(Double.POSITIVE_INFINITY);
		
		System.out.print("Generating test data...");
		DataSetGenerator gen = new DataSetGenerator(rdfProc.getModel(), "testProc", dConfig);
		List<Observation> observations = gen.generate();
		System.out.println("Done. There are " + observations.size() + " observations in the dataset.");
		
		System.out.print("Generating binding set...");
		List<Binding> bindings = BindingGroup.createBindingList(rnrmProc, observations, rdfProc);
		System.out.println("Done.  There are " + bindings.size() + " bindings.");
		
		System.out.print("Finding solutions...");
		ProcessFinder processFinder = new ProcessFinder(rnrmProc,
				new BindingGroup(bindings), config);
		List<SolutionReport> reports = processFinder.find();
		System.out.println("Done.");
		
		Collections.sort(reports);
		for (ISolutionReport r : reports) {
			System.out.println(r.toString());
		}
		System.out.println("Total solutions: " + reports.size());
	}

}
