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

import java.util.List;

import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.dataGen.DataGenConfig;
import com.bbn.c2s2.pint.dataGen.DataSetGenerator;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DataGenerationTest {
	
	public static void main(String [] args) throws Exception {
		Model processModel = RNRMLoader
		.loadProcesses(Constants.FILE_RNRM_MODIFIED);
		String procUri = 
			"http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcess067c634f-9705-491a-9db8-a65ad5b3ac21";

		DataGenConfig dConfig = new DataGenConfig();
		dConfig.setSignalNoiseRatio(.3);
		DataSetGenerator gen = new DataSetGenerator(processModel, procUri, dConfig);
		List<Observation> observations = gen.generate();
		
		RdfProcess rdfProc = RdfProcess.create((RDFHelper.getResource(processModel, procUri)));
		List<Resource> actRes = rdfProc.getActivities();
		for (Resource q : actRes) {
			System.out.println(RDFHelper.getLabel(processModel, q.getURI()));
			StmtIterator it = rdfProc.getModel().listStatements(q, RNRM.hasObservable, (Resource) null);
			while (it.hasNext()) {
				System.out.println(it.nextStatement().getObject().toString());
			}
		}

		RnrmProcess p = RnrmProcessFactory.createProcess(processModel, procUri);
		
//		RnrmWrapper w = new RnrmWrapper(processModel);
//		List<Binding> g = BindingGroup.createBindingList(p, observations, w.getRdfProcess(p.getProcessUri()));
//		for (Binding b : g) {
//			String l = RDFHelper.getLabel(processModel, b.getActivityUri());
//			System.out.println(l + " " + b.toString());
//		}
		
	}

}
