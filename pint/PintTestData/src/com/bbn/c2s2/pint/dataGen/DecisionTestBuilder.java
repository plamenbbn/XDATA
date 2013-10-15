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

import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class DecisionTestBuilder {
	
	public static Model build(Model m, String procUri) {
		Resource procResource = RDFHelper.getResource(m, procUri);
		RdfProcess rdfProcess = RdfProcess.create(procResource);
		List<Resource> activities = rdfProcess.getActivities();
		for (Resource a : activities) {
			String obsUri = a.getURI() + "O";
			Resource obs = m.createResource(obsUri);
			Statement s = m.createStatement(a, RNRM.hasObservable, obs);
			m.add(s);
		}
		return m;
	}

}
