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

import java.util.List;

import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.rdf.RdfProcessEditor;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class DataSetGenerator {
	
	private Model noiseModel, truthModel;
	private String noiseUri, truthUri;
	private TestDataPoint dataPoint;
	
	public DataSetGenerator(Model m, String processUri, DataGenConfig config) throws Exception {
		noiseUri = processUri;
		noiseModel = m;
		Resource procResource = RDFHelper.getResource(m, processUri);
		RdfProcess rdfProcess = RdfProcess.create(procResource);
		// ensure there are observables on all activities
		if(!rdfProcess.hasObservablesForAllActivities()) {
			RdfProcessEditor editor = new RdfProcessEditor(rdfProcess);
			editor.fillInObservables();
		}
		PathExtractor extractor = new PathExtractor(rdfProcess, config.getRandomSeed());
		truthModel = extractor.getPath();
		StmtIterator it = truthModel.listStatements((Resource) null, RDF.type, RNRM.Process);
		truthUri = it.nextStatement().getSubject().getURI();
		dataPoint = new TestDataPoint(config);
	}
	
	public List<Observation> generate() throws Exception {
		return (List<Observation>) dataPoint.getObservations(truthModel, noiseModel, truthUri, noiseUri);
	}
	
	public Model getTruthModel() {
		return truthModel;
	}
}
