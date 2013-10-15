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
package com.bbn.c2s2.pint.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Utility for extracting the rdfs:label from an RDF Resource.
 * 
 * @author tself
 * 
 */
public class RDFHelper {

	/**
	 * Returns the label for the uri. If the uri doesn't have a label, it
	 * returns the uri, if the uri isn't in the model, it still returns the uri
	 * 
	 * @param m
	 * @param uri
	 * @return
	 */
	public static String getLabel(Model m, String uri) {
		String rv = uri;

		if (null != m) {
			StmtIterator it = m.listStatements(m.getResource(uri), RDFS.label,
					(RDFNode) null);

			while (it.hasNext()) {
				rv = it.nextStatement().getString();
				break;
			}
		}
		return rv;
	}
	
	public static boolean hasRdfType(Model model, Resource element, Resource type) {
		boolean rv = false;
		NodeIterator typeIterator = model.listObjectsOfProperty(element,
				RDF.type);
		while (typeIterator.hasNext()) {
			if (typeIterator.nextNode().equals(type)) {
				rv = true;
				break;
			}
		}
		return rv;
	}
	
	public static Resource getResource(Model m, String uri) {
		return m.createResource(uri);
	}
	
}
