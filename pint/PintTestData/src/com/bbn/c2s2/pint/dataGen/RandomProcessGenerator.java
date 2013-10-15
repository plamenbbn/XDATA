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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RandomProcessGenerator {
	
	private int currentId = 0;
	private Set<InternalActivity> activities = new LinkedHashSet<InternalActivity>();
	private Map<InternalActivity, Set<InternalActivity>> happnesBefore = new HashMap<InternalActivity, Set<InternalActivity>>();
	private Random random = new Random(1137854);
	public int modelCount = 0;
	
	public int getNumActivities() {
		return this.activities.size() -2;
	}
	
	public void reset() {
		this.currentId = 0;
		this.activities = new LinkedHashSet<InternalActivity>();
		this.happnesBefore = new HashMap<InternalActivity, Set<InternalActivity>>();
	}
	
	public Model createProcess(int minChainLen, int numChains, int maxObservablesPerAct, double obsToActRatio) {
		//System.out.println(maxPathLen + " " + numPaths);
		Resource processNode = ProcessModelFactory.createNewProcessWithoutObservables();
		// Model aProcess = ModelFactory.createDefaultModel();
		Model aProcess = processNode.getModel();
		this.generate(minChainLen, numChains);
		this.makeRdfFragments(processNode, this.getFirstActivity());
		this.connectRdfFragments(aProcess, this.getFirstActivity());
		this.removeForks(aProcess);
		this.removeJoins(aProcess);
		this.capProcess(processNode);
		List<Resource> observables = this.generateObservableSet(obsToActRatio);
		for (InternalActivity a : this.activities) {
			if (a.id != -1 && a.id != Integer.MAX_VALUE) {
				int numObservables = this.random.nextInt(maxObservablesPerAct) + 1;
				for (int k = 0; k < numObservables; k++) {
					int index = this.random.nextInt(observables.size());
					StmtIterator i = a.resource.getModel().listStatements(a.resource, RNRM.representsActivity, (Resource) null);
					Resource act = (Resource) i.nextStatement().getObject();
					aProcess.add(act, RNRM.hasObservable, observables.get(index));
					aProcess.add(observables.get(index).getModel());
				}
			}
		}
		System.out.println( "RandomProcessGenerator:createProcess:- created Process " + processNode.getURI() );
		printModel(aProcess);
		return aProcess;
	}
	
	
	
	public void capProcess(Resource processNode) {
		Model m = processNode.getModel();
		InternalActivity startAct = this.getFirstActivity();
		m.remove(m.createStatement(startAct.join.resource, RNRM.isFlowingTo, startAct.resource));
		m.remove(m.createStatement(startAct.resource, RDF.type, RNRM.ActivityElement));
		StmtIterator s;
		s = m.listStatements(startAct.resource, RNRM.representsActivity, (Resource) null);
		Resource actToDelete = (Resource) s.nextStatement().getObject();
		m.remove(m.createStatement(startAct.resource, RNRM.representsActivity, actToDelete));
		m.remove(m.createStatement(actToDelete, RDFS.label, "A" + startAct.id));
		m.remove(m.createStatement(actToDelete, RDF.type, RNRM.Activity));
		
		Resource joinCap = ProcessModelFactory.createJoinElement(processNode);
		
		if (startAct.fork.outList.size() > 1) {
			m.remove(m.createStatement(startAct.resource, RNRM.isFlowingTo, startAct.fork.resource));
			m.add(joinCap, RNRM.isFlowingTo, startAct.fork.resource);
		}
		else {
			m.remove(m.createStatement(startAct.resource, RNRM.isFlowingTo, startAct.fork.outList.get(0).internalActivity.resource));
			m.add(joinCap, RNRM.isFlowingTo, startAct.fork.outList.get(0).internalActivity.resource);
		}
		
		InternalActivity endAct = this.getLastActivity();
		m.remove(m.createStatement(endAct.resource, RNRM.isFlowingTo, endAct.fork.resource));
		m.remove(m.createStatement(endAct.resource, RDF.type, RNRM.ActivityElement));
		s = m.listStatements(endAct.resource, RNRM.representsActivity, (Resource) null);
		actToDelete = (Resource) s.nextStatement().getObject();
		m.remove(m.createStatement(endAct.resource, RNRM.representsActivity, actToDelete));
		m.remove(m.createStatement(actToDelete, RDFS.label, "A" + endAct.id));
		m.remove(m.createStatement(actToDelete, RDF.type, RNRM.Activity));
		
		Resource forkCap = ProcessModelFactory.createForkElement(processNode);
		
		if (endAct.join.inList.size() > 1) {
			m.remove(m.createStatement(endAct.join.resource, RNRM.isFlowingTo, endAct.resource));
			m.add(endAct.join.resource, RNRM.isFlowingTo, forkCap);
		}
		else {
			m.remove(m.createStatement(endAct.join.inList.get(0).internalActivity.resource, RNRM.isFlowingTo, endAct.resource));
			m.add(endAct.join.inList.get(0).internalActivity.resource, RNRM.isFlowingTo, forkCap);
		}
		
		ArrayList<Resource> detachedForks = new ArrayList<Resource>();
		ArrayList<Resource> detachedJoins = new ArrayList<Resource>();
		m.add(processNode.getModel());
		StmtIterator i = m.listStatements((Resource) null, RDF.type, RNRM.ActivityElement);
		while (i.hasNext()) {
			Statement stmt = m.createStatement(processNode, RNRM.hasElement, i.nextStatement().getSubject());
			m.add(stmt);
		}
		i = m.listStatements((Resource) null, RDF.type, RNRM.JoinElement);
		while (i.hasNext()) {
			Resource sub = i.nextStatement().getSubject();
			if (this.detachedJoin(m, sub)) {
				detachedJoins.add(sub);
				//m.remove(m.createStatement(sub, RDF.type, RNRM.JoinElement));
			}
			else {
				Statement stmt = m.createStatement(processNode, RNRM.hasElement, sub );
				m.add(stmt);
			}
		}
		i = m.listStatements((Resource) null, RDF.type, RNRM.ForkElement);
		while (i.hasNext()) {
			Resource sub = i.nextStatement().getSubject();
			if (this.detachedFork(m, sub)) {
				detachedForks.add(sub);
				//System.out.println("here");
				//m.remove(m.createStatement(sub, RDF.type, RNRM.ForkElement));
			}
			else {
				Statement stmt = m.createStatement(processNode, RNRM.hasElement, sub);
				m.add(stmt);
			}
		}
		
		for (Resource r : detachedJoins) {
			m.remove(m.createStatement(r, RDF.type, RNRM.JoinElement));
		}
		
		for (Resource r : detachedForks ) {
			m.remove(m.createStatement(r, RDF.type, RNRM.ForkElement));
		}
		
	}
	
	public boolean detachedJoin(Model m, Resource join) {
		if (m.listStatements(join, RNRM.isFlowingTo, (Resource) null).hasNext() || 
				m.listStatements((Resource) null, RNRM.isFlowingTo, join).hasNext())
			return false;
		else return true;
	}
	
	public boolean detachedFork(Model m, Resource fork) {
		if (m.listStatements(fork, RNRM.isFlowingTo, (Resource) null).hasNext() || 
				m.listStatements((Resource) null, RNRM.isFlowingTo, fork).hasNext())
			return false;
		else return true;
	}
	
	public InternalActivity getFirstActivity() {
		for (InternalActivity a : this.activities) {
			if (a.id == -1) return a;
		}
		return null;
	}
	
	public InternalActivity getLastActivity() {
		for (InternalActivity a : this.activities) {
			if (a.id == Integer.MAX_VALUE) return a;
		}
		return null;
	}
	
	public void makeRdfFragments(Resource processNode, InternalActivity current) {
		Model m = processNode.getModel();
		if (current.resource == null) {
			//System.out.print("ID: " + current.id + " ");
			Resource join = ProcessModelFactory.createJoinElement(processNode);
			Resource fork = ProcessModelFactory.createForkElement(processNode);
			Resource act = ProcessModelFactory.createActivityElementWithActivity("A" + current.id, processNode);
			//System.out.println(act.toString());
			m.add(join, RNRM.isFlowingTo, act);
			m.add(act, RNRM.isFlowingTo, fork);
			current.resource = act;
			current.fork.resource = fork;
			current.join.resource = join;
			for (InternalJoin jToA : current.fork.outList) {
				this.makeRdfFragments(processNode, jToA.internalActivity);
			}
		}
	}
	
	public void connectRdfFragments(Model m, InternalActivity current) {

		for (InternalJoin j : current.fork.outList) {
			Resource fork = current.fork.resource;
			Resource join = j.resource;
			m.add(fork , RNRM.isFlowingTo, join);
		}
		for (InternalJoin jToA : current.fork.outList) {
			this.connectRdfFragments(m, jToA.internalActivity);
		}
		return;
	}
	
	public void removeForks(Model m) {
		List<Resource> forks = this.getForkResources();
		for (Resource r : forks) {
			StmtIterator stit = m.listStatements(r, RNRM.isFlowingTo, (RDFNode) null);
			List<Resource> results = this.resultSetToArray(stit, 2);
			if (results.size() == 1) {
				//m.remove(m.createStatement(r, RDF.type, RNRM.ForkElement));
				StmtIterator i = m.listStatements((Resource) null, RNRM.isFlowingTo, r);
				Resource actIn = i.nextStatement().getSubject();
				Resource joinOut = results.get(0);
				StmtIterator remove1 = m.listStatements(actIn, RNRM.isFlowingTo, r);
				StmtIterator remove2 = m.listStatements(r, RNRM.isFlowingTo, joinOut);
				m.remove(remove1.nextStatement());
				m.remove(remove2.nextStatement());
				m.add(m.createStatement(actIn, RNRM.isFlowingTo, joinOut));
			}
		}
	}
	
	public void removeJoins(Model m) {
		List<Resource> joins = this.getJoinResources();
		for (Resource r : joins) {
			StmtIterator stit = m.listStatements((Resource) null, RNRM.isFlowingTo, r);
			List<Resource> results = this.resultSetToArray(stit, 0);
			if (results.size() == 1) {
				//m.remove(m.createStatement(r, RDF.type, RNRM.JoinElement));
				StmtIterator i = m.listStatements(r, RNRM.isFlowingTo, (RDFNode) null);
				Resource actOut = (Resource) i.nextStatement().getObject();
				Resource forkOrActIn = results.get(0);
				StmtIterator remove1 = m.listStatements(forkOrActIn, RNRM.isFlowingTo, r);
				StmtIterator remove2 = m.listStatements(r, RNRM.isFlowingTo, actOut);
				m.remove(remove1.nextStatement());
				m.remove(remove2.nextStatement());
				m.add(m.createStatement(forkOrActIn, RNRM.isFlowingTo, actOut));
			}
		}
	}
	
	public List<Resource> resultSetToArray(StmtIterator i, int pos) {
		ArrayList<Resource> rv = new ArrayList<Resource>();
		while (i.hasNext()) {
			if (pos == 0) {
				rv.add(i.nextStatement().getSubject());
			}
			else if (pos == 1) {
				rv.add(i.nextStatement().getPredicate());
			}
			else if (pos == 2) {
				rv.add((Resource) i.nextStatement().getObject());
			}
			else return null;
		}
		return rv;
	}
	
	public List<Resource> getForkResources() {
		ArrayList<Resource> rv = new ArrayList<Resource>();
		for (InternalActivity a : this.activities) {
			rv.add(a.fork.resource);
		}
		return rv;
	}
	
	public List<Resource> getJoinResources() {
		ArrayList<Resource> rv = new ArrayList<Resource>();
		for (InternalActivity a : this.activities) {
			rv.add(a.join.resource);
		}
		return rv;
	}
	
	public void generate(int maxPathLen, int numPaths) {
		this.init(maxPathLen);
		//this.printHappensBefore();
		//System.out.println();
		for (int i = 0 ; i < numPaths; i++) {
			this.addPath(1 + this.random.nextInt(maxPathLen - 1));
		}
	}
	
	public List<Resource> generateObservableSet(double percent) {
		int numObservables = (int) (this.activities.size() * percent);
		if (numObservables == 0) numObservables = 1;
		List<Resource> observables = new ArrayList<Resource>(numObservables);
		for (int i = 0; i < numObservables; i++) {
			Model m = ModelFactory.createDefaultModel();
			observables.add(ProcessModelFactory.createObservableResource(m));
		}
		return observables;
	}
	
	public void addPath(int pathLen) {
		//System.out.println("path len: " + pathLen);
		ArrayList<InternalActivity> path = new ArrayList<InternalActivity>();
		this.createPathInternal(pathLen, path);
		InternalActivity startPoint = null;
		InternalActivity endPoint = null;
		do {
			endPoint = this.selectRandomInternalActivity(this.activities);
			startPoint = this.chooseStartPoint(endPoint);
		} while (startPoint == null || directlyConnected(startPoint, endPoint) || (startPoint.id == -1 && endPoint.id == Integer.MAX_VALUE) );
		InternalActivity head = path.get(0);
		InternalActivity tail = path.get(path.size() -1);
		this.connect(startPoint, head);
		this.connect(tail, endPoint);
		this.activities.addAll(path);
		this.buildHappensBeforeMap();
	}
	
	public boolean directlyConnected(InternalActivity a1, InternalActivity a2) {
		boolean r =  a1.fork.outList.contains(a2.join) && a2.join.inList.contains(a1.fork);
		//System.out.println(a1.id + " " + a2.id + " " + r);
		return r;
	}
	
	public void createPathInternal(int pathLen, ArrayList<InternalActivity> path) {
		if (path.size() == pathLen) return;
		InternalActivity newTail = new InternalActivity(this.currentId);
		this.currentId ++;
		if (path.size() != 0) {
			InternalActivity tail = path.get(path.size() - 1);
			this.connect(tail, newTail);
		}
		path.add(newTail);
		createPathInternal(pathLen, path );	
	}
	
	public boolean flowsInto(InternalActivity x, InternalActivity z) {
		if (this.happnesBefore.get(z) != null && this.happnesBefore.get(z).contains(x)) {
			return true;
		}
		else if (x.fork.outList.contains(z.join)) {
			return true;
		}
		else {
			for (InternalJoin jToA : x.fork.outList) {
				InternalActivity newX = jToA.internalActivity;
				return flowsInto(newX, z);
			}
			return false;  
		}
	}
	
	
	private InternalActivity chooseStartPoint(InternalActivity endPoint) {
		return this.selectRandomInternalActivity(this.happnesBefore.get(endPoint));
	}
	
	private InternalActivity selectRandomInternalActivity(Set<InternalActivity> candidates) {
		if (candidates.size() == 0) return null;
		int index = random.nextInt(candidates.size());
		int count = 0;
		for (InternalActivity a : candidates) {
			if (count == index) return a;
			count++;
		}
		return null;
	}
	
	public void init(int pathLen) {
		InternalActivity start = new InternalActivity(-1);
		InternalActivity end = new InternalActivity(Integer.MAX_VALUE);
		ArrayList<InternalActivity> path = new ArrayList<InternalActivity>();
		this.createPathInternal(pathLen, path);
		InternalActivity head = path.get(0);
		InternalActivity tail = path.get(path.size() -1 );
		this.connect(start, head);
		this.connect(tail, end);
		this.activities.add(start);
		this.activities.add(end);
		this.activities.addAll(path);
		this.buildHappensBeforeMap();
	}
	
	public void connect(InternalActivity a1, InternalActivity a2) {
		a1.fork.outList.add(a2.join);
		a2.join.inList.add(a1.fork);
	}
	
	public void buildHappensBeforeMap() {
		this.happnesBefore = new HashMap<InternalActivity, Set<InternalActivity>>();
		this.happnesBefore.put(new InternalActivity(-1), new LinkedHashSet<InternalActivity>());
		for (InternalActivity i : this.activities) {
			for (InternalActivity j : this.activities) {
				if (this.flowsInto(i, j)) {
					if (this.happnesBefore.get(j) == null) {
						this.happnesBefore.put(j, new LinkedHashSet<InternalActivity>());
					}
					this.happnesBefore.get(j).add(i);
				}
			}
		}
	}
	
	public static void printModel(Model m) {
		StmtIterator i = m.listStatements();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			if (true) {
				System.out.print(s.getSubject().toString().split("#")[1] +  "  ");
				System.out.print(s.getPredicate().toString().split("#")[1] +  "  ");
				if (s.getObject().toString().contains("#"))
					System.out.print(s.getObject().toString().split("#")[1] +  "  ");
				else 
					System.out.print(s.getObject().toString());
				System.out.println();
			}
		}
		
	}
	
	public void printHappensBefore() {
		for (InternalActivity a : this.happnesBefore.keySet()) {
			System.out.print(a.id + " | "	);
			for (InternalActivity j : this.happnesBefore.get(a)) {
				System.out.print(j.id + ", ");
			}
			System.out.println();
		}
	}
	
	private class InternalActivity implements Comparable<InternalActivity>{
		Resource resource = null;
		private int id;
		private InternalJoin join = new InternalJoin(this); //back pointer
		private InternalFork fork = new InternalFork(this); //forward pointer
		public InternalActivity(int id) {
			this.id = id;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InternalActivity other = (InternalActivity) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}
		private RandomProcessGenerator getOuterType() {
			return RandomProcessGenerator.this;
		}
		@Override
		public int compareTo(InternalActivity arg0) {
			if (this.id < arg0.id) return -1;
			if (this.id > arg0.id) return 1;
			return 0;
		}
	}
	
	private class InternalJoin {
		Resource resource = null;
		InternalActivity internalActivity;
		public InternalJoin(InternalActivity a) {
			this.internalActivity = a;
		}
		private List<InternalFork> inList = new ArrayList<InternalFork> ();
		public void addIncoming(InternalFork f) {
			this.inList.add(f);
		}
	}
	
	private class InternalFork {
		Resource resource = null;
		InternalActivity internalActivity;
		public InternalFork(InternalActivity a) {
			this.internalActivity = a;
		}
		private List<InternalJoin> outList = new ArrayList<InternalJoin> ();
		public void addIncoming(InternalJoin j) {
			this.outList.add(j);
		}
	} 
	
}
