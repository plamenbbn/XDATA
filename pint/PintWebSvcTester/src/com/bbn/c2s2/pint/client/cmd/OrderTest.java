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

import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;

public class OrderTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Model processModel = RNRMLoader
				.loadProcesses(Constants.FILE_EXAMPLES_RDF);
		String processUri = "http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e";
//		List<Observation> observations = ObservationLoader
//				.loadObservations(Constants.FILE_OBSERVATIONS_DATA);

		RnrmProcess proc = RnrmProcessFactory.createProcess(processModel, processUri);

		Map<Activity, Set<Activity>> hBefore = proc.getHappensBeforeMap();
		for (Activity a : hBefore.keySet()) {
			System.out.print(String.format("%1$s must follow [", a.getLabel()));
			Set<Activity> priors = hBefore.get(a);
			for (Activity b : priors) {
				System.out.print(String.format("%1$s, ", b.getLabel()));
			}
			System.out.println("]");
		}
		// ProcessLoader procLoader = new ProcessLoader(processModel,
		// observations);
		// procLoader.loadProcess(processUri);
		// PartialOrder process = new
		// PartialOrder(procLoader.getOrderedActivityMap());
		// ObservationGroup observationGroup = new
		// ObservationGroup(procLoader.getCandidateActivityMap());
		// CandidateSolutionGenerator solutionGenerator = new
		// CandidateSolutionGenerator(process,observationGroup);

		// List<CandidateSolution> solutions = solutionGenerator.equals
		// CandidateSolutionEvaluator evaluator = new
		// CandidateSolutionEvaluator(process);
		// for (CandidateSolution cs : solutions)
		// evaluator.printSolution(cs);
	}

}
