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

import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.dataGen.DataGenConfig;
import com.bbn.c2s2.pint.dataGen.DataSetGenerator;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.bbn.c2s2.test.util.ProcessHelper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestProcessBuilder {
	
	private Model model = ModelFactory.createDefaultModel();
	private Resource proc;
	
	public Model getModel() {
		return this.model;
	}
	
	public TestProcessBuilder(String procName) {
		proc = model.createResource(procName);
	}
	
	public Resource makeDecision(int id) {
		Resource rv = model.createResource("dec"+id);
		Statement s = model.createStatement(rv, RDF.type, RNRM.DecisionElement);
		model.add(s);
		s = model.createStatement(proc, RNRM.hasElement, rv);
		model.add(s);
		return rv;
	}
	
	public Resource makeMerge(int id) {
		Resource rv = model.createResource("mer"+id);
		Statement s = model.createStatement(rv, RDF.type, RNRM.MergeElement);
		model.add(s);
		s = model.createStatement(proc, RNRM.hasElement, rv);
		model.add(s);
		return rv;
	}
	
	public Resource makeFork(int id) {
		Resource rv = model.createResource("frk"+id);
		Statement s = model.createStatement(rv, RDF.type, RNRM.ForkElement);
		model.add(s);
		s = model.createStatement(proc, RNRM.hasElement, rv);
		model.add(s);
		return rv;
	}
	
	public Resource makeJoin(int id) {
		Resource rv = model.createResource("jon"+id);
		Statement s = model.createStatement(rv, RDF.type, RNRM.JoinElement);
		model.add(s);
		s = model.createStatement(proc, RNRM.hasElement, rv);
		model.add(s);
		return rv;
	}
	
	public Resource makeActivity(int id) {
		Resource rv = model.createResource("ael"+id);
		Resource act = model.createResource("act" + id);
		Resource obs = model.createResource("obs" + id);
		Statement s = model.createStatement(rv, RDF.type, RNRM.ActivityElement);
		model.add(s);
		s = model.createStatement(rv, RNRM.representsActivity, act);
		model.add(s);
		s = model.createStatement(act, RDFS.label, "alb" + id);
		model.add(s);
		s = model.createStatement(act, RNRM.hasObservable, obs);
		model.add(s);
		s = model.createStatement(proc, RNRM.hasElement, rv);
		model.add(s);
		return rv;
	}
	
	public void flowsInto(Resource r1, Resource r2) {
		Statement s = model.createStatement(r1, RNRM.isFlowingTo, r2);
		model.add(s);
	}
	
	public void buildTest() {
		
		Resource J1 = makeJoin(1);
		Resource J2 = makeJoin(2);
		Resource J3 = makeJoin(3);
		Resource J4 = makeJoin(4);
		Resource F1 = makeFork(1);
		Resource F2 = makeFork(2);
		Resource F3 = makeFork(3);
		Resource F4 = makeFork(4);
		Resource D1 = makeDecision(1);
		Resource D2 = makeDecision(2);
		Resource D3 = makeDecision(3);
		Resource D4 = makeDecision(4);
		Resource D5 = makeDecision(5);
		Resource D6 = makeDecision(6);
		Resource D7 = makeDecision(7);
		Resource D8 = makeDecision(8);
		Resource D9 = makeDecision(9);
		Resource D10 = makeDecision(10);
		Resource D11 = makeDecision(11);
		Resource D12 = makeDecision(12);
		Resource D13 = makeDecision(13);
		Resource D14 = makeDecision(14);
		Resource M1 = makeMerge(1);
		Resource M2 = makeMerge(2);
		Resource M3 = makeMerge(3);
		Resource M4 = makeMerge(4);
		Resource M5 = makeMerge(5);
		Resource M6 = makeMerge(6);
		Resource M7 = makeMerge(7);
		Resource M8 = makeMerge(8);
		Resource M9 = makeMerge(9);
		Resource M10 = makeMerge(10);
		Resource M11 = makeMerge(11);
		Resource M12 = makeMerge(12);
		Resource M13 = makeMerge(13);
		Resource M14 = makeMerge(14);
		Resource A1 = makeActivity(1);
		Resource A2 = makeActivity(2);
		Resource A3 = makeActivity(3);
		Resource A4 = makeActivity(4);
		Resource A5 = makeActivity(5);
		Resource A6 = makeActivity(6);
		Resource A7 = makeActivity(7);
		Resource A8 = makeActivity(8);
		Resource A9 = makeActivity(9);
		Resource A10 = makeActivity(10);
		Resource A11 = makeActivity(11);
		Resource A12 = makeActivity(12);
		Resource A13 = makeActivity(13);
		Resource A14 = makeActivity(14);
		Resource A15 = makeActivity(15);
		Resource A16 = makeActivity(16);
		Resource A17 = makeActivity(17);
		Resource A18 = makeActivity(18);
		Resource A19 = makeActivity(19);
		Resource A20 = makeActivity(20);
		Resource A21 = makeActivity(21);
		Resource A22 = makeActivity(22);
		Resource A23 = makeActivity(23);
		Resource A24 = makeActivity(24);
		Resource A25 = makeActivity(25);
		Resource A26 = makeActivity(26);
		Resource A27 = makeActivity(27);
		Resource A28 = makeActivity(28);
		Resource A29 = makeActivity(29);
		Resource A30 = makeActivity(30);
		Resource A31 = makeActivity(31);
		Resource A32 = makeActivity(32);
		Resource A33 = makeActivity(33);
		Resource A34 = makeActivity(34);
		Resource A35 = makeActivity(35);
		Resource A36 = makeActivity(36);
		Resource A37 = makeActivity(37);
		Resource A38 = makeActivity(38);
		Resource A39 = makeActivity(39);
		Resource A40 = makeActivity(40);
		Resource A41 = makeActivity(41);
		Resource A42 = makeActivity(42);
		Resource A43 = makeActivity(43);
		Resource A44 = makeActivity(44);
		Resource A45 = makeActivity(45);
		Resource A46 = makeActivity(46);
		Resource A47 = makeActivity(47);
		Resource A48 = makeActivity(48);
		Resource A49 = makeActivity(49);
		
		flowsInto(J1, A1);
		flowsInto(A1, F1);
		flowsInto(F1, D1);
		flowsInto(F1, D2);
		flowsInto(F1, D3);
		flowsInto(F1, D4);
		flowsInto(F1, D5);
		flowsInto(F1, A8);
		flowsInto(F1, A9);
		flowsInto(F1, A10);
		flowsInto(D1, A2);
		flowsInto(D1, M1);
		flowsInto(D2, A3);
		flowsInto(D2, M2);
		flowsInto(D3, A4);
		flowsInto(D3, M3);
		flowsInto(D4, A5);
		flowsInto(D4, M4);
		flowsInto(D5, A6);
		flowsInto(D5, A7);
		flowsInto(A2, M1);
		flowsInto(A3, M2);
		flowsInto(A4, M3);
		flowsInto(A5, M4);
		flowsInto(A6, M5);
		flowsInto(A7, M5);
		flowsInto(M1, J2);
		flowsInto(M2, J2);
		flowsInto(M3, J2);
		flowsInto(M4, J2);
		flowsInto(M5, J2);
		flowsInto(A8, J2);
		flowsInto(A9, J2);
		flowsInto(A10, J2);
		flowsInto(J2, A11);
		flowsInto(A11, F2);
		flowsInto(F2, A12);
		flowsInto(F2, A13);
		flowsInto(F2, A14);
		flowsInto(A12, J3);
		flowsInto(A13, J3);
		flowsInto(A14, J3);
		flowsInto(J3, D6);
		flowsInto(D6, A15);
		flowsInto(D6, F3);
		flowsInto(A15, A16);
		flowsInto(A16, A17);
		flowsInto(A17, A18);
		flowsInto(A18, D7);
		flowsInto(D7, A19);
		flowsInto(A19, M6);
		flowsInto(D7, M6);
		flowsInto(M6, A20);
		flowsInto(A20, A21);
		flowsInto(A21, M8);
		flowsInto(F3, A22);
		flowsInto(F3, A23);
		flowsInto(A22, J4);
		flowsInto(A23, J4);
		flowsInto(J4, A24);
		flowsInto(A24, A25);
		flowsInto(A25, A26);
		flowsInto(A26, D8);
		flowsInto(D8, A27);
		flowsInto(D8, M7);
		flowsInto(A27, M7);
		flowsInto(M7, A28);
		flowsInto(A28, A29);
		flowsInto(A29, A30);
		flowsInto(A30, A31);
		flowsInto(A31, A32);
		flowsInto(A32, A33);
		flowsInto(A33, M8);
		flowsInto(M8, D9);
		flowsInto(D9, A34);
		flowsInto(D9, A35);
		flowsInto(A35, A36);
		flowsInto(A34, M9);
		flowsInto(A36, M9);
		flowsInto(M9, A37);
		flowsInto(A37, A38);
		flowsInto(A38, D10);
		flowsInto(D10, A39);
		flowsInto(D10, M10);
		flowsInto(A39, A40);
		flowsInto(A40, M10);
		flowsInto(M10, D11);
		flowsInto(D11, A41);
		flowsInto(D11, A42);
		flowsInto(D11, M11);
		flowsInto(A41, M11);
		flowsInto(A42, M11);
		flowsInto(M11, D12);
		flowsInto(D12, D13);
		flowsInto(D12, M13);
		flowsInto(D13, A43);
		flowsInto(D13, A44);
		flowsInto(D13, A45);
		flowsInto(D13, A46);
		flowsInto(A43, M12);
		flowsInto(A44, M12);
		flowsInto(A45, M12);
		flowsInto(A46, M12);
		flowsInto(M12, M13);
		flowsInto(M13, A47);
		flowsInto(A47, D14);
		flowsInto(D14, A48);
		flowsInto(D14, M14);
		flowsInto(A48, M14);
		flowsInto(M14, A49);
		flowsInto(A49, F4);
		
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		TestProcessBuilder pb = new TestProcessBuilder("testProc");
		pb.buildTest();
		RdfProcess rdfProc = RdfProcess.create(RDFHelper.getResource(pb.getModel(), "testProc"));
		RnrmProcess rnrmProc = RnrmProcessFactory.createProcess(rdfProc);
		System.out.println(ProcessHelper.toString(rnrmProc.getOpposesMap(), pb.getModel()));
		DataGenConfig dConfig = new DataGenConfig();
		dConfig.setSignalNoiseRatio(Double.POSITIVE_INFINITY);
		DataSetGenerator gen = new DataSetGenerator(rdfProc.getModel(), "testProc", dConfig);
		List<Observation> observations = gen.generate();
		List<Binding> g = BindingGroup.createBindingList(rnrmProc, observations, rdfProc);
		for (Binding b : g) {
			String l = RDFHelper.getLabel(pb.getModel(), b.getActivityUri());
			System.out.println(l + " " + b.toString());
		}
		
		
		

	}

}
