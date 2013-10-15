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
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestDataPoint {

	// Constants
	protected final int MIN_ACT_CHAIN_LEN = 2;
	protected final int MAX_ACT_CHAIN_LEN = 10;

	protected final int MIN_CHAINS = 0;
	protected final int MAX_CHAINS = 2;

	protected final int MIN_OBSERVABLES_PER_ACT = 1;
	protected final int MAX_OBSERVABLES_PER_ACT = 3;

	protected final double MIN_OBSERVABLE_TO_ACT_RATIO = 1;
	protected final double MAX_OBSERVABLE_TO_ACT_RATIO = 5;
	
	protected final double MAX_SIGNAL_TO_NOISE_RATIO = .1;
	protected final double MIN_SIGNAL_TO_NOISE_RATIO = .01;
	protected final int MAX_TRUTHS = 16;
	protected final int MIN_TRUTHS = 4;



	// Random number generator
	protected Random random;

	// Data ID (used to regenerate)
	protected int id;

	// State
	protected int actChainLen;
	protected int chains;
	protected int observablesPerAct;
	protected double observablesToActRatio;
	
	DataGenConfig config;


	public static List<TestDataPoint> generateDataPoints(int numPoints) {
		ArrayList<TestDataPoint> rv = new ArrayList<TestDataPoint>(numPoints);
		for (int i = 0; i < numPoints; i++) {
			rv.add(new TestDataPoint(i));
		}
		return rv;
	}
	
	public TestDataPoint(DataGenConfig config) {
		this.config = config;
	}
	
	public TestDataPoint(Double snr) {
		this(new DataGenConfig());
		config.setSignalNoiseRatio(snr);
	}	

	public TestDataPoint(int id) {
		this(new DataGenConfig());
		this.id = id;
		
		this.random = new Random( (id+1) * config.getRandomSeed() );
		
		actChainLen = MIN_ACT_CHAIN_LEN	+ random.nextInt(MAX_ACT_CHAIN_LEN - MIN_ACT_CHAIN_LEN + 1);
		chains = MIN_CHAINS + random.nextInt(MAX_CHAINS - MIN_CHAINS + 1);
		observablesPerAct = MIN_OBSERVABLES_PER_ACT	+ random.nextInt(MAX_OBSERVABLES_PER_ACT - MIN_OBSERVABLES_PER_ACT + 1);
		observablesToActRatio = MIN_OBSERVABLE_TO_ACT_RATIO	+ random.nextDouble()*(MAX_OBSERVABLE_TO_ACT_RATIO - MIN_OBSERVABLE_TO_ACT_RATIO);
		double snr = MIN_SIGNAL_TO_NOISE_RATIO + random.nextDouble()* (MAX_SIGNAL_TO_NOISE_RATIO - MIN_SIGNAL_TO_NOISE_RATIO);
		int truths = MIN_TRUTHS + random.nextInt(MAX_TRUTHS - MIN_TRUTHS + 1);
		
		config.setSignalNoiseRatio(snr);
		config.setNumTruths(truths);
	}
	
	public static String getProcessUri(Model m) {
		StmtIterator it = m.listStatements((Resource) null, RDF.type, RNRM.Process);
		String procUri = it.nextStatement().getSubject().toString();
		return procUri;
	}

	public Model getProcessModel() {
		RandomProcessGenerator pGen = new RandomProcessGenerator();
		return pGen.createProcess(actChainLen, chains, observablesPerAct,
				observablesToActRatio);
	}
	
	public Collection<Observation> getObservations(Model m, String procUri) throws Exception {
		return this.getObservations(m, m, procUri, procUri);
	}

	public Collection<Observation> getObservations(Model mTruth, Model mNoise, String truthUri, String noiseUri) throws Exception {
		ObservationGenerator obsGen = new ObservationGenerator(config);
		return generateObservationSet(mTruth, mNoise, truthUri, noiseUri, obsGen);
	}
	
	private  List<Observation> generateObservationSet(Model mTruth, Model mNoise, String truthUri, String noiseUri, ObservationGenerator observationGenerator) throws Exception {
		List<Observation> truth = generateTruth(mTruth, truthUri, observationGenerator);
		int truths = config.getNumTruths();
		int signalSize =  truth.size() / truths;
		List<Observation> noise = observationGenerator.makeNoise(mNoise, noiseUri, signalSize);
		truth.addAll(noise);
		return truth;
	}
	
	private  List<Observation> generateTruth(Model m, String processUri, ObservationGenerator observationGenerator) throws Exception {
		int truths = config.getNumTruths();
		List<Observation> observations = new ArrayList<Observation>();
		for (int i = 0; i < truths; i++) {
			observations.addAll(observationGenerator.makeTruthSet(m, processUri));
		}
		return observations;
	}

	public int getId() {
		return this.id;
	}

}
