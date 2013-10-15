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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Analyzes the Processes in an RNRM to validate them and generate metrics.
 * 
 * @author tself
 * 
 */
public class RnrmValidator {

	static List<String> processes = new ArrayList<String>();
	static List<String> validProcesses = new ArrayList<String>();

	public static void printMetrics(RdfProcess process) throws Exception {
		int elemCount = process.getProcessElements().size();
		int actCount = process.getActivityElements().size();
		int actObsCount = process.getActivitiesWithObservables().size();
		int forkCount = process.getForkElements().size();
		int joinCount = process.getJoinElements().size();
		int decCount = process.getDecisionElements().size();
		int mergeCount = process.getMergeElements().size();
		
		String dataString = String.format("%1$d (%2$d),%3$d,%4$d,%5$d,%6$d,%7$s",
				actCount, actObsCount, forkCount, joinCount, decCount, mergeCount, process.getLabel());
		processes.add(dataString);
		print("----------BEGIN--------------");
		print("URI", process.getProcessUri());
		print("Label", process.getLabel());
		print("Elements", "" + elemCount);
		print("Activities", "" + actCount);
		print("...with Observables", "" + actObsCount);
		print("Forks/Joins", forkCount + "/" + joinCount);
		print("Decisions/Merges", decCount + "/" + mergeCount);
		boolean valid = process.isValid();
		if (valid) {
			validProcesses.add(dataString);
		}
		print("Is Valid", "" + valid);
		// print("Has Partial Order", "" + process.isOrderConstrained());
		print("----------END----------------");
	}

	static void resetLists() {
		processes = new ArrayList<String>();
		validProcesses = new ArrayList<String>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File f = null;
		if (args.length > 0) {
			f = new File(args[0]);
		}
		if (null == f || !f.exists()) {
			f = Constants.FILE_RNRM_MODIFIED;
		}
		Model model = RNRMLoader.loadProcesses(f);
		RnrmWrapper rnrm = new RnrmWrapper(model);

		// analyze each Process
		List<Resource> processResources = rnrm.getProcessResources();
		for (Resource r : processResources) {
			RdfProcess proc = RdfProcess.create(r);
			if (proc.isValid()) {
				printMetrics(proc);
			}
		}

		print("========= VALID PROCESSES ==========");
		for (String s : validProcesses) {
			print(s);
		}

		print(String.format(
				"=== Total Processes (%1$d) === Valid Processes (%2$d) ===",
				processes.size(), validProcesses.size()));
	}

	private static void print(String attrib, String value) {
		print(String.format("%1$s = %2$s", attrib, value));
	}

	private static void print(String s) {
		System.out.println(s);
	}

}
