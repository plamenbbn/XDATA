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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.util.AplToPintConversionFactory;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.ObservationLoader;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AlignmentStructureLoader {

	public static void main(String[] args) throws Exception {
		Model processModel = RNRMLoader
				.loadProcesses(Constants.FILE_EXAMPLES_RDF);
		RnrmWrapper rnrm = new RnrmWrapper(processModel);
		rnrm.getProcesses();

		String processUri = "http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e";
		List<edu.jhuapl.c2s2.pp.observation.Observation> aplObservations = ObservationLoader
				.loadObservations(Constants.FILE_OBSERVATIONS_DATA);

		Collection<Observation> observations = AplToPintConversionFactory
				.convertObservations(aplObservations);

		long startTime = System.currentTimeMillis();

		// // Get the candidate mapping from activities to observations.
		// TreeMap<Activity, TimeSortedTcaBag> candidates = procLoader
		// .getCandidateActivityMap();

		RnrmProcess process = RnrmProcessFactory.createProcess(processModel,
				processUri);
		Map<Activity, Set<Activity>> partialOrderedActivities = process
				.getHappensBeforeMap();

		long endTime = System.currentTimeMillis();
		System.out.println(String.format(
				"Process creation completed in %1$d ms.", endTime - startTime));

		startTime = System.currentTimeMillis();

		BindingGroup sortedCandidates = new BindingGroup(BindingGroup
				.createBindingList(process, observations, rnrm.getRdfProcess(process.getProcessUri())));

		endTime = System.currentTimeMillis();

		System.out.println(String.format(
				"BindingGroup creation completed in %1$d ms", endTime
						- startTime));

		System.out.println("Full activity dependency lists:");
		for (Activity act : partialOrderedActivities.keySet()) {
			System.out.println(String.format("[ %1$d: %2$s:", act.getID(), act
					.getLabel()));
			for (Activity priorActivity : partialOrderedActivities.get(act)) {
				System.out.println(String.format("   %1$d: %2$s", priorActivity
						.getID(), getLabel(processModel, priorActivity
						.getActivityURI())));
			}
			System.out.println(String.format(
					"] (CandidateActivity Count: %1$d)", sortedCandidates
							.getBindings(act).size()));
		}
		System.out.println(String.format("Total CandidateActivities is %1$s",
				sortedCandidates.bindingCount()));

		// PRINT the BindingGroup
		// for(Activity act : partialOrderedActivities.keySet()) {
		// System.out.print(String.format("%1$s [", act.getLabel()));
		// for(Binding canAct : sortedCandidates.getCandidateActivities(act)) {
		// System.out.print(String.format("%1$s, ", canAct.getLabel()));
		// }
		// System.out.println("]");
		// }
	}

	public static String getLabel(Model m, String uri) {
		String rv = uri;
		StmtIterator it = m.listStatements(m.getResource(uri), RDFS.label,
				(RDFNode) null);
		while (it.hasNext()) {
			rv = it.nextStatement().getString();
		}
		return rv;
	}
}
