/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
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
package com.bbn.c2s2.pint.dataGen;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class PathExtractor {
	
	private Random randGen;
	private RdfProcess process;
	
	public PathExtractor(RdfProcess process, long randSeed) {
		this.process = process;
		this.randGen = new Random(randSeed);
	}
		
	public Model getPath() throws Exception {
		Set<Resource> visitedDecisionNodes = new HashSet<Resource>();
		Model rv = ModelFactory.createDefaultModel();
		Resource startElement = this.process.getStartElement();
		if (startElement == null) throw new Exception("Invalid process.  Cannot find a root element");
		int id = 0;
		
		Statement s = rv.createStatement(process.getProcessResource(), RDF.type, RNRM.Process);
		rv.add(s);
		this.findPathAux(startElement, id, visitedDecisionNodes, rv);
		return rv;
	}

	private void findPathAux(Resource current, 
			int id,
			Set<Resource> vd,
			Model rv) throws Exception {
		
		
		Statement s;
		s = rv.createStatement(process.getProcessResource(), RNRM.hasElement, current);
		rv.add(s);
		
		boolean isDecisionElement = RDFHelper.hasRdfType(process.getModel(), current, RNRM.DecisionElement);
		boolean isForkElement = RDFHelper.hasRdfType(process.getModel(), current, RNRM.ForkElement);
		boolean isJoinElement = RDFHelper.hasRdfType(process.getModel(), current, RNRM.JoinElement);
		boolean isMergeElement = RDFHelper.hasRdfType(process.getModel(), current, RNRM.MergeElement);
		boolean isActivityElement = RDFHelper.hasRdfType(process.getModel(), current, RNRM.ActivityElement); 
		Resource type = null;
		if (isDecisionElement) type = RNRM.DecisionElement;
		else if (isForkElement) type = RNRM.ForkElement;
		else if (isJoinElement) type = RNRM.JoinElement;
		else if (isMergeElement) type = RNRM.MergeElement;
		else if (isActivityElement) type = RNRM.ActivityElement;
		else throw new Exception("Unrecognized type.");
		s = rv.createStatement(current, RDF.type, type);
		rv.add(s);
		
		List<Resource> children = process.getChildren(current);
		if (isDecisionElement) {
			if (!vd.contains(current)) {
				vd.add(current);
				int index = this.randGen.nextInt(children.size());
				s = rv.createStatement(current, RNRM.isFlowingTo, children.get(index));
				rv.add(s);
				findPathAux(children.get(index), id,
						vd, rv);
			}
		} else {
			for (Resource t : children) {
				if (isActivityElement) {
					Resource actResource = this.process.getActivity(current);
					s = rv.createStatement(current, RNRM.representsActivity, actResource);
					rv.add(s);
					s = rv.createStatement(actResource, RDFS.label, RDFHelper.getLabel(process.getModel(), actResource.getURI()));
					rv.add(s);
					for (Resource observable : this.process.getObservables(actResource)) {
						s = rv.createStatement(actResource, RNRM.hasObservable, observable);
						rv.add(s);
					}
				}
				s = rv.createStatement(current, RNRM.isFlowingTo, t);
				rv.add(s);
				findPathAux(t, id, vd, rv);
			}
		}
	}

}
