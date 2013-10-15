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
package com.bbn.c2s2;

import java.util.Collection;
import java.util.List;

import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.ProcessAssignment;
import com.bbn.c2s2.pint.ProcessMultiplexer;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.test.util.ProcessHelper;
import com.bbn.c2s2.util.AplToPintConversionFactory;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.ObservationLoader;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;

public class ProcessMultiplexerTester {
	public static void main(String[] args) throws Exception {
		Model processModel = RNRMLoader
				.loadProcesses(Constants.FILE_RNRM_MODIFIED);
		// .loadProcesses(Constants.FILE_EXAMPLES_RDF);
		// .loadProcesses(Constants.FILE_EXAMPLES_RDF2);

		// load the observations from the observations.data file
		List<edu.jhuapl.c2s2.pp.observation.Observation> aplObservations = ObservationLoader
		// .loadObservations(Constants.FILE_OBSERVATIONS_DATA);
				.loadObservations(Constants.FILE_OBSERVATIONS_DATA2);

		// convert them into our version of an Observation
		Collection<Observation> observations = AplToPintConversionFactory
				.convertObservations(aplObservations);

		ProcessMultiplexer pMux = new ProcessMultiplexer(processModel);
		pMux.initialize();
		System.out.println(pMux.getProcesses().size());
		for(RnrmProcess proc : pMux.getProcesses()) {
			System.out.println(String.format("Process: [%1$s]", proc.getLabel()));
			System.out.println(proc.getProcessUri());
			System.out.println(ProcessHelper.toString(proc.getHappensBeforeMap(), processModel));
			System.out.println();
			System.out.println(ProcessHelper.toString(proc.getOpposesMap(), processModel));
		}
		
		
//		ProcessExtractor ex = new ProcessExtractor(processModel);
//		System.out.println("opposes map...");
//		System.out.println(ProcessHelper.toString(
//				ex.createOpposingActivitiesMap(
//						"http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcess067c634f-9705-491a-9db8-a65ad5b3ac21"), 
//						processModel));
		
		if(true) return;
		
		Collection<ProcessAssignment> assignments = pMux
				.getProcessAssignments(observations);
		for (ProcessAssignment assign : assignments) {
			System.out
					.println(String.format(
							"Assignment: %1$s (%2$d candidates)", assign
									.getProcess().toString(), assign
									.getCandidates().bindingCount()));

			PintConfiguration config = new PintConfiguration(ConfigLoader
					.getInstance().getPintParameters());
		}
	}
}
