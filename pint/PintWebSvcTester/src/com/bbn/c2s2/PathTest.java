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



import com.bbn.c2s2.pint.RnrmProcessElement;
import com.bbn.c2s2.pint.dataGen.PathExtractor;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.RNRMLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class PathTest {
	
	public static void main (String [] args) throws Exception {
		Model processModel = RNRMLoader
		.loadProcesses(Constants.FILE_RNRM_MODIFIED);
		String procUri = 
			"http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcess067c634f-9705-491a-9db8-a65ad5b3ac21";
		Resource procResource = RDFHelper.getResource(processModel, procUri);
		RdfProcess rdfProcess = RdfProcess.create(procResource);
		PathExtractor extractor = new PathExtractor(rdfProcess, 17143);
		Model m = extractor.getPath();
		StmtIterator i = m.listStatements();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			if (s.getPredicate().toString().equals("http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#isFlowingTo")) {
				RnrmProcessElement pes = new RnrmProcessElement(s.getSubject());
				RnrmProcessElement peo = new RnrmProcessElement((Resource) s.getObject());
				if (pes.getActivity() != null)
					System.out.print(pes.getActivity().getLabel() + " ");
				else System.out.print(fixString(s.getSubject().toString()) + " ");
				System.out.print(fixString(s.getPredicate().toString()) + " ");
				if (peo.getActivity() != null)
					System.out.print(peo.getActivity().getLabel() + " ");
				else System.out.print(fixString(s.getObject().toString()) + " ");
				System.out.println();
			}
		}
	}
	
	public static String fixString(String s) {
		String[] a = s.split("#");
		return a[1];
	}

}
