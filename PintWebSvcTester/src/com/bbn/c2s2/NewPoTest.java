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

import com.bbn.c2s2.pint.dataGen.ProcessModelFactory;
import com.bbn.c2s2.pint.pf.PartialOrderBuilder;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class NewPoTest {
	
	public static void main(String [] args) throws Exception {
		Model processModel = RNRMLoader
		.loadProcesses(Constants.FILE_RNRM_MODIFIED);
		String procUri = 
			"http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e";
		RdfProcess rdfProc = RdfProcess.create(RDFHelper.getResource(processModel, procUri));
		
		PartialOrderBuilder pob = new PartialOrderBuilder(rdfProc);
		
//		StmtIterator it = rdfProc.getModel().listStatements();
//		while (it.hasNext()) {
//			Statement s = it.nextStatement();
//			String predicate = s.getPredicate().toString();
//			if (predicate.equals("hasId")) {
//				System.out.println(s.toString());
//			}
//		}
		
		Resource r1 = ProcessModelFactory.createCyclicProcessB();
		RdfProcess pr2 = RdfProcess.create(r1);
		PartialOrderBuilder pob2 = new PartialOrderBuilder(pr2);
		if (pob2.hasCycle()) {
			System.out.println("su CC E SS");
		}
		System.exit(0);
		
		
		
		for (Resource r : pob.getPartialOrder().keySet()) {
			Resource keyAct = rdfProc.getActivity(r);
			if (keyAct != null) {
				String lab = RDFHelper.getLabel(processModel, keyAct.toString());
				System.out.print(lab + "  |  ");
				for (Resource k : pob.getPartialOrder().get(r)) {
					Resource beforeAct = rdfProc.getActivity(k);
					if (beforeAct != null) {
						lab = RDFHelper.getLabel(processModel, beforeAct.toString());
						System.out.print(lab + ", ");
					}
				}
				System.out.println();
			}
		}
		
	}
	

}
