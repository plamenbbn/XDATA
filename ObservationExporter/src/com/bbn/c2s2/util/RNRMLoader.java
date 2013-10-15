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
package com.bbn.c2s2.util;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class RNRMLoader {

	public static Model loadProcesses(File processFile) {
		Model rv = ModelFactory.createDefaultModel();
		try {
			rv.read((Reader) new FileReader(processFile), "RDFXML");
		} catch (Exception e) {
			System.err.println(String
					.format("Error occurred while reading %1$s.", processFile
							.getName()));
		}
		return rv;
	}

	public static void main(String[] args) throws Exception {
		Model m = loadProcesses(Constants.FILE_EXAMPLES_RDF);
		StmtIterator st = m.listStatements();
		while (st.hasNext()) {
			Statement s = (Statement) st.next();
			System.out.println(s);
		}
		System.out.println(String.format("Statement count is %1$s.", m.size()));
		
		Resource processResource = m.getResource("http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e");
		RdfProcess rdfProc = RdfProcess.create(processResource);
		RnrmProcess process = RnrmProcessFactory.createProcess(rdfProc);
		System.out.println( "\n------------PROCESS---------------\n" + process.toCsvString() );
	}
}

//<rdf:Description rdf:about="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessa4ff73f5-0c4d-40d6-84d1-2d8706d665f8">
//<rdf:type rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#Process"/>
//<rdfs:label>Dummy Process</rdfs:label>
//<skos:prefLabel>Dummy Process</skos:prefLabel>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElemf070cea6-b162-4557-b596-3d55a5b190cb"/>
//</rdf:Description>


//<rdf:Description rdf:about="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#rnrmProcessb2a2cee5-4ea3-42a5-a7a2-51f439bae73e">
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem6df837eb-28ed-485c-8bb5-8c785f981a6e"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem3db11d25-3586-4dfd-8980-2a0644f70cde"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElemdf46f101-b111-4bff-bbea-db761cc13fb5"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElemdbafd2fc-d27d-4d8e-bdb7-c33804ee8263"/>
//<rdf:type rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#Process"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityEleme8ada65e-c912-4f6d-93c8-4df22c21a251"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#join74f8896a-3893-40dd-89df-c2a0e9557c93"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#fork5ae3c7b3-86ed-4214-a417-59412c769158"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#fork4e957aaf-8259-4ab3-9d38-ec47577aa0cb"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#joine77fa243-342a-4f5f-8407-edcc09145e36"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem88c5d088-933b-4a3c-bdae-f1e0ef442010"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem76c7ff2c-7b83-4f3b-b1cd-85d365db20f3"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem760dd6f2-5c20-448b-a381-8ac96978fa25"/>
//<skos:prefLabel>Nitric Acid Outdoors</skos:prefLabel>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem8f4297b3-2979-487e-abfd-5286835c1ead"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem1560e6ee-ee35-4583-8141-171374e75c74"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#join8f2248a5-3bd5-4a10-b1fa-e541fb934623"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem584981de-4822-4870-b7fa-f322fbd67214"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#join31e94c94-d459-41ea-a286-324ab8b4af88"/>
//<rdfs:label>Nitric Acid Outdoors</rdfs:label>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElemf7a1eaae-bb49-4818-bfc4-0038078899b6"/>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#activityElem7d55d32a-0e81-4a9a-8fa4-767d89725c14"/>
//<skos:definition>Recipe 1</skos:definition>
//<rnrm:hasElement rdf:resource="http://www.c2s2.jhuapl.edu/2009/04/30/rnrm#forkd7d1e202-1060-4e5d-90e7-721718f9d676"/>
//</rdf:Description>

