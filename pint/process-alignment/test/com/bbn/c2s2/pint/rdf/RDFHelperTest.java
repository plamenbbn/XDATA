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

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RDFHelperTest extends TestCase {

	public void testRDFHelper() throws Exception {
		new RDFHelper();
	}
	public void testGetLabel() throws Exception {

		// test a single resource with a label
		String uri1 = "http://c2s2.instances#instance1";
		String label1 = "Label 1";

		Model m = ModelFactory.createDefaultModel();
		addResourceToModel(m, uri1, label1);
		assertEquals(label1, RDFHelper.getLabel(m, uri1));

		// test a single resource without a label
		String uri2 = "http://c2s2.instances#instance2";
		String label2 = null;

		m = ModelFactory.createDefaultModel();
		addResourceToModel(m, uri2, label2);
		assertEquals(uri2, RDFHelper.getLabel(m, uri2));

		// test with more than one resource in there and more than one label
		m = ModelFactory.createDefaultModel();
		addResourceToModel(m, uri1, "foo1");
		addResourceToModel(m, uri1, "bar1");
		addResourceToModel(m, uri2, "foo2");

		String label = RDFHelper.getLabel(m, uri1);
		assertTrue("foo1".equals(label) || "bar1".equals(label));
	}

	public static Resource addResourceToModel(Model m, String uri, String label) {
		Resource r = m.createResource(uri);
		if (null != label) {
			m.add(r, RDFS.label, label);
		}

		return r;
	}
}
