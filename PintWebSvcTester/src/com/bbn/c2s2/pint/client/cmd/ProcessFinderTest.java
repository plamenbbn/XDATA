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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.bbn.c2s2.ConfigLoader;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.ISolutionReport;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.pf.SolutionReport;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.util.AplToPintConversionFactory;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.ObservationLoader;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;

public class ProcessFinderTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// DEMONSTRATES READING IN A PROCESS AND DATA SET AND GENERATING
		// SOLUTIONS
		// Initialize configuration object...
		PintConfiguration config = new PintConfiguration(ConfigLoader
				.getInstance().getPintParameters());
		// STEP 0 - READ STUFF FROM THE MODEL
		Model processModel = RNRMLoader
				.loadProcesses(Constants.FILE_EXAMPLES_RDF);
		RnrmWrapper rnrm = new RnrmWrapper(processModel);
		rnrm.getProcesses();
		String processUri = "http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e";
		List<edu.jhuapl.c2s2.pp.observation.Observation> aplObservations = ObservationLoader
				.loadObservations(Constants.FILE_OBSERVATIONS_DATA);

		Collection<Observation> observations = AplToPintConversionFactory
				.convertObservations(aplObservations);

		// STEP 1 - BUILD THE PROCESS
		RnrmProcess process = RnrmProcessFactory.createProcess(processModel,
				processUri);

		System.out.println("Initializing process...");
		// STEP 2 - SORT OBSERVATIONS BY TIME FOR THE PROCESS
		BindingGroup sortedCandidates = new BindingGroup(BindingGroup
				.createBindingList(process, observations, rnrm.getRdfProcess(process.getProcessUri())));
		System.out.println("Building binding group...");

		ProcessFinder processFinder = new ProcessFinder(process,
				sortedCandidates, config);
		List<SolutionReport> reports = processFinder.find();

		Collections.sort(reports);
		for (ISolutionReport r : reports) {
			System.out.println(r.toString());
		}
		System.out.println("Total solutions: " + reports.size());

	}

}
