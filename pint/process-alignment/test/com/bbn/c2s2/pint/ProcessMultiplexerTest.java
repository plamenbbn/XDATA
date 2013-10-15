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
package com.bbn.c2s2.pint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.testdata.ProcessModelFactory;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ProcessMultiplexerTest extends TestCase {

	Resource processWithObservable1, processWithoutObservable1,
			processWithObservables1;
	ProcessMultiplexer m1, m2, m3, m4;

	protected void setUp() throws Exception {
		super.setUp();
		processWithObservable1 = ProcessModelFactory
				.createSingleActivityProcessWithObservable();
		processWithoutObservable1 = ProcessModelFactory
				.createSingleActivityProcess();
		processWithObservables1 = ProcessModelFactory
				.createSerialProcessWithObservables();
		m1 = new ProcessMultiplexer(processWithObservable1.getModel());
		m1.initialize();
		m2 = new ProcessMultiplexer(processWithoutObservable1.getModel());
		m2.initialize();
		m3 = new ProcessMultiplexer(processWithoutObservable1.getModel());
		// NOTE: m3 is not initialized.
		m4 = new ProcessMultiplexer(processWithObservables1.getModel());
		m4.initialize();
	}

	public void testInitialize() throws Exception {
		assertTrue(m1.initialized);
		assertTrue(m2.initialized);
		assertFalse(m3.initialized);
		// ignore single activity process test.
		assertTrue(m1.getProcesses().size() == 0);
		// test for process with no observables.
		assertTrue(m2.getProcesses().size() == 0);
		assertTrue(m4.getProcesses().size() == 1);

		Model multiProcessModel = ModelFactory.createDefaultModel();
		multiProcessModel.add(this.processWithObservable1.getModel());
		multiProcessModel.add(this.processWithoutObservable1.getModel());
		multiProcessModel.add(this.processWithObservables1.getModel());
		ProcessMultiplexer m5 = new ProcessMultiplexer(multiProcessModel);
		m5.initialize();
		assertTrue(m5.getProcesses().size() == 1);

	}

	public void testGetProcessAssignments() {
		Model m = m4.rnrm.getModel();
		StmtIterator i = m.listStatements((Resource) null, RNRM.hasObservable,
				(Resource) null);
		Set<String> uniqueObservables = new HashSet<String>();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			uniqueObservables.add(s.getObject().toString());
		}
		int count = 0;
		Collection<Observation> os = new ArrayList<Observation>();
		for (String obsUri : uniqueObservables) {
			Observation o = new Observation("o" + count++, obsUri, 0, 0,
					new Date(0));
			os.add(o);
		}
		Collection<ProcessAssignment> pas = null;
		pas = m4.getProcessAssignments(os);
		ProcessAssignment pa = pas.iterator().next();
		assertTrue(pa._candidates.bindingCount() == 2);

		os = new ArrayList<Observation>();

		pas = m4.getProcessAssignments(os);
		assertTrue(pas.size() == 0);

	}

}
