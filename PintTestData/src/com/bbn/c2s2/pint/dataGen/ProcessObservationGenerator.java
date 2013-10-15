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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.bbn.c2s2.pint.Observation;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author krohloff
 * 
 */
public class ProcessObservationGenerator {

	private Model _baseModel;
	private Date _startDate;
	private Date _endDate;
	private double _radius;
	private Random _generator;

	private int _obsCounter = 0;
	private List<String> _observableUriList = new ArrayList<String>();
	private Map<String, List<String>> _activityObservableUriMap = new HashMap<String, List<String>>();
	private Map<String, Set<String>> _flowsTo = new HashMap<String, Set<String>>();
	// private Set<String> _parents = new HashSet<String>();

	private Map<String, Set<String>> _before = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> _after = new HashMap<String, Set<String>>();

	final String BASE_OBSERVATION_URI = "http://c2s2.bbn.com/test#observation";
	final double EARTH_RADIUS_KM = 6371;
	private long _maxProcessInterval;

	/**
	 * @param randomModel
	 * @param startDate
	 * @param endDate
	 * @param center
	 * @param radius
	 */
	public ProcessObservationGenerator(Model baseModel, Date startDate,
			Date endDate, long maxInterval, double radius) {
		_baseModel = baseModel;
		_startDate = startDate;
		_endDate = endDate;
		_maxProcessInterval = maxInterval;
		_radius = radius;

		_generator = new Random();

		setObservableUriList();
		setFlowsTo();
		updateFlowsTo();
		// setParents();
		// System.out.println(_activityObservableUriMap);

		setBeforeAfter();

		// for(String key : _flowsTo.keySet()){
		// System.out.println(key+","+_flowsTo.get(key));
		// }
		// System.out.println(_parents);
	}

	/**
	 * 
	 */
	private void setBeforeAfter() {
		for (String act : _activityObservableUriMap.keySet()) {
			Set<String> beforeSet = new HashSet<String>();
			_before.put(act, beforeSet);
			_after.put(act, beforeSet);
		}

		_after.putAll(_flowsTo);

		for (String key : _flowsTo.keySet()) {
			for (String child : _flowsTo.get(key)) {
				_before.get(child).add(key);
			}
		}
		trimBefore();
		trimAfter();

		// System.out.println("---------Before");
		// for (String key : _before.keySet()) {
		// System.out.println(key + "," + _before.get(key));
		// }
		// System.out.println("---------After");
		// for (String key : _after.keySet()) {
		// System.out.println(key + "," + _after.get(key));
		// }

	}

	/**
	 * 
	 */
	private void trimAfter() {
		// System.out.println("----------------------Trimming After");
		boolean trimmed = false;
		while (!trimmed) {
			trimmed = true;
			for (String key : _after.keySet()) {
				// System.out.println(key+","+_after.get(key));
				Set<String> addSet = new HashSet<String>();
				for (String child : _after.get(key)) {
					Set<String> childSet = _after.get(child);
					if (!_after.get(key).containsAll(childSet)) {
						trimmed = false;
						addSet.addAll(childSet);
					}
				}
				_after.get(key).addAll(addSet);
			}
			// System.out.println("----------------------");
		}
	}

	/**
	 * 
	 */
	private void trimBefore() {

		// System.out.println("----------------------Trimming Before");
		boolean trimmed = false;
		while (!trimmed) {
			trimmed = true;
			for (String key : _before.keySet()) {
				// System.out.println(key+","+_before.get(key));
				Set<String> addSet = new HashSet<String>();
				for (String child : _before.get(key)) {
					Set<String> childSet = _before.get(child);
					if (!_before.get(key).containsAll(childSet)) {
						trimmed = false;
						addSet.addAll(childSet);
					}
				}
				_before.get(key).addAll(addSet);
			}
			// System.out.println("----------------------");
		}
	}

	/**
	 * 
	 */
	private void updateFlowsTo() {
		Map<String, String> actElemActMap = getActElemActMap();
		Map<String, Set<String>> updatedFlowsTo = new HashMap<String, Set<String>>();

		for (String elemKey : _flowsTo.keySet()) {
			String actKey = actElemActMap.get(elemKey);
			Set<String> actFlowsTo = new HashSet<String>();
			for (String actElem : _flowsTo.get(elemKey)) {
				actFlowsTo.add(actElemActMap.get(actElem));
			}
			// System.out.println(actKey+","+actFlowsTo);
			updatedFlowsTo.put(actKey, actFlowsTo);
		}
		_flowsTo = updatedFlowsTo;
	}

	/**
	 * @return
	 */
	private Map<String, String> getActElemActMap() {

		Map<String, String> actElemActMap = new HashMap<String, String>();

		StmtIterator i = _baseModel.listStatements();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			if (true) {
				Resource subject = s.getSubject();
				RDFNode object = s.getObject();
				String subjLabel = subject.toString().split("#")[1];
				if (s.getObject().toString().contains("#")) {
					String objLabel = object.toString().split("#")[1];
					if (subjLabel.startsWith("activityElem")
							&& objLabel.startsWith("activity")
							&& !objLabel.startsWith("activityElem")) {
						// System.out.println(subjLabel+","+objLabel);
						actElemActMap.put(subjLabel, objLabel);
					}
				}

			}
		}
		return actElemActMap;
	}

	// /**
	// *
	// */
	// private void setParents() {
	//		
	// _parents.addAll(_flowsTo.keySet());
	//		
	// for(String key : _flowsTo.keySet()){
	// _parents.removeAll(_flowsTo.get(key));
	// }
	//		
	// // System.out.println("Parents: "+_parents);
	//		
	// }

	/**
	 * 
	 */
	private void setFlowsTo() {
		intializeFlowsTo();

		// for(String key : _flowsTo.keySet()){
		// System.out.println(key+","+_flowsTo.get(key));
		// }

		boolean trimmed = false;
		while (!trimmed) {
			// for(String key : _flowsTo.keySet()){
			// System.out.println(key+","+_flowsTo.get(key));
			// }
			trimmed = trimFlowsTo();
		}
		// for(String key : _flowsTo.keySet()){
		// System.out.println(key+","+_flowsTo.get(key));
		// }
		removeJoinForks();

	}

	/**
	 * 
	 */
	private void removeJoinForks() {
		Set<String> remSet = new HashSet<String>();
		for (String key : _flowsTo.keySet()) {
			if (key.startsWith("join") || key.startsWith("fork")) {
				remSet.add(key);
			}
		}
		for (String key : remSet) {
			_flowsTo.remove(key);
		}
	}

	/**
	 * 
	 */
	private boolean trimFlowsTo() {
		boolean trimmed = true;
		Set<String> removeKeys = new HashSet<String>();
		for (String key : _flowsTo.keySet()) {
			Set<String> remove = new HashSet<String>();
			Set<String> add = new HashSet<String>();
			Set<String> children = _flowsTo.get(key);
			for (String child : children) {
				if (child.startsWith("fork") || child.startsWith("join")) {
					trimmed = false;
					remove.add(child);
					if (_flowsTo.get(child) != null) {
						add.addAll(_flowsTo.get(child));
					}
					if (!removeKeys.contains(child)) {
						removeKeys.add(child);
					}
				}
			}
			// System.out.println("Remove: "+ remove);
			// System.out.println("Add: "+ add);
			children.removeAll(remove);
			children.addAll(add);
		}

		return trimmed;

		// System.out.println("Remove Keys"+removeKeys);
		// for(String key : removeKeys){
		// _flowsTo.remove(key);
		// }
	}

	/**
	 * 
	 */
	private void intializeFlowsTo() {
		StmtIterator i = _baseModel.listStatements();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			if (true) {
				Resource subject = s.getSubject();
				String subjLabel = subject.toString().split("#")[1];
				Resource predicate = s.getPredicate();
				String predLabel = predicate.toString().split("#")[1];
				boolean flows = predLabel.startsWith("isFlowingTo");
				if (flows) {
					RDFNode object = s.getObject();
					if (s.getObject().toString().contains("#")) {
						String objLabel = object.toString().split("#")[1];
						// System.out.println(subjLabel+" "+predLabel+" "+objLabel);
						if (!_flowsTo.keySet().contains(subjLabel)) {
							_flowsTo.put(subjLabel, new HashSet<String>());
						}
						_flowsTo.get(subjLabel).add(objLabel);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void setObservableUriList() {

		StmtIterator i = _baseModel.listStatements();
		while (i.hasNext()) {
			Statement s = i.nextStatement();
			String label = s.getSubject().toString().split("#")[1];
			// System.out.println(label);
			if (label.startsWith("observable")) {
				// System.out.println(s.getSubject().getURI());
				_observableUriList.add(s.getSubject().getURI());
			}
			if (label.startsWith("activity")
					&& !label.startsWith("activityElem")) {
				// System.out.println(s.getSubject().getURI());
				if (s.getObject().toString().contains("#")) {
					String objLabel = s.getObject().toString().split("#")[1];
					if (objLabel.startsWith("observable")) {
						// System.out.println("\t"+s.getObject());
						if (!_activityObservableUriMap.containsKey(s
								.getSubject().getURI())) {
							_activityObservableUriMap.put(label,
									new ArrayList<String>());
						}
						_activityObservableUriMap.get(label).add(
								s.getObject().toString());
					}
				}
			}
		}
	}

	/**
	 * @param numTrueObservationSets
	 * @param numNumCollocatedPerCenter
	 * @param numNoisePerCenter
	 * @return
	 */
	private Set<Observation> generateTrue(int numTrueObservationSets,
			int numNumCollocatedPerCenter, int numNoisePerCenter) {
		Set<Observation> retSet = new HashSet<Observation>();
		for (int i = 0; i < numTrueObservationSets; i++) {

			long interval = (long) (_maxProcessInterval * _generator
					.nextDouble());
			long lowBound = _startDate.getTime();
			long upBound = _endDate.getTime();
			long latestOffsetStart = upBound - lowBound - interval;
			long offsetStart = (long) (latestOffsetStart * _generator.nextDouble());
			Date start = new Date(lowBound + offsetStart);
			Date end = new Date(lowBound + offsetStart + interval);

			double[] center = getRandomLocation();
			for (int j = 0; j < numNumCollocatedPerCenter; j++) {
				Set<Observation> currentSet = new HashSet<Observation>();
				currentSet = generateTrue(center, start, end);
				retSet.addAll(currentSet);
			}
			for (int j = 0; j < numNoisePerCenter; j++) {
				Observation noise = generateRandomObservation(center);
				retSet.add(noise);
			}
		}

		return retSet;
	}

	/**
	 * @param center
	 * @return a set of observations that are correct w.r.t. the process - both
	 *         within the radius and distance temporal constraints
	 */
	private Set<Observation> generateTrue(double[] center, Date startProcess,
			Date endProcess) {
		Map<String, Observation> actMap = new HashMap<String, Observation>();
		List<String> toAssign = new ArrayList<String>(_activityObservableUriMap
				.keySet());

		while (toAssign.size() > 0) {
			String current = toAssign.remove(_generator
					.nextInt(toAssign.size()));
			Date start = getStart(_before.get(current), actMap, startProcess);
			Date end = getEnd(_after.get(current), actMap, endProcess);
			// System.out.println("Start: "+start);
			// System.out.println("End: "+end);
			Observation obs = generateObservation(current, start, end, center);
			actMap.put(current, obs);
		}

		Set<Observation> retSet = new HashSet<Observation>();

		for (String key : actMap.keySet()) {
			retSet.add(actMap.get(key));
		}

		return retSet;
	}

	/**
	 * @param set
	 * @param actMap
	 * @return
	 */
	private Date getEnd(Set<String> afterSet, Map<String, Observation> actMap,
			Date end) {
		Date retDate = end;
		for (String act : afterSet) {
			if (actMap.containsKey(act)) {
				Observation obs = actMap.get(act);
				Date obsDate = obs.getTimestamp();
				if (obsDate.before(retDate)) {
					retDate = obsDate;
				}
			}
		}
		return retDate;
	}

	/**
	 * @param set
	 * @param actMap
	 * @return
	 */
	private Date getStart(Set<String> beforeSet,
			Map<String, Observation> actMap, Date start) {
		Date retDate = start;
		for (String act : beforeSet) {
			if (actMap.containsKey(act)) {
				Observation obs = actMap.get(act);
				Date obsDate = obs.getTimestamp();
				if (obsDate.after(retDate)) {
					retDate = obsDate;
				}
			}
		}
		return retDate;
	}

	// /**
	// * @param parentCopy
	// * @param flowsToCopy
	// * @return
	// */
	// private List<String> removeSamplePath(Set<String> parentCopy,
	// Map<String, Set<String>> flowsToCopy) {
	//		
	// List<String> retList = new ArrayList<String>();
	//		
	// List<String> childList = new ArrayList<String>(parentCopy);
	// while(childList.size()>0){
	// String head = childList.get(_generator.nextInt(childList.size()));
	// retList.add(head);
	// childList = new ArrayList<String>(flowsToCopy.get(head));
	// flowsToCopy.remove(head);
	// }
	// for(String key : flowsToCopy.keySet()){
	// Set<String> remove = new HashSet<String>();
	// for(String child : flowsToCopy.get(key)){
	// if(!flowsToCopy.keySet().contains(child)){
	// remove.add(child);
	// }
	// }
	// flowsToCopy.get(key).removeAll(remove);
	// }
	//		
	//		
	// return retList;
	// }
	//
	// /**
	// * @param flowsToCopy
	// * @return
	// */
	// private Set<String> getParents(Map<String, Set<String>> flowsToCopy) {
	// Set<String> retParents = new HashSet<String>();
	// retParents.addAll(flowsToCopy.keySet());
	//		
	// for(String key : flowsToCopy.keySet()){
	// retParents.removeAll(flowsToCopy.get(key));
	// }
	// return retParents;
	// }

	/**
	 * @param numNoiseObservations
	 * @return observations uniformly distributed around a center point and
	 *         between two time points.
	 */
	private Set<Observation> generateNoise(int numNoiseObservations) {

		Set<Observation> returnSet = new HashSet<Observation>();

		for (int i = 0; i < numNoiseObservations; i++) {
			Observation randObs = generateRandomObservation();
			returnSet.add(randObs);
			// System.out.println(randObs.getObservationUri());
			// System.out.println(randObs.getTimestamp().getTime());
		}
		return returnSet;
	}

	/**
	 * @param center
	 * @return
	 */
	private Observation generateObservation(String act, Date start, Date end,
			double[] center) {
		String observationUri = getObservationUri();
		String observableUri = getRandomObservable(_activityObservableUriMap
				.get(act));
		double[] loc = getRandomLocation(center);
		Date timeStamp = getRandomDate(start, end);

		// System.out.println(observationUri+","+observableUri+","+loc[0]+","+loc[1]+","+timeStamp);

		Observation obs = new Observation(observationUri, observableUri,
				loc[0], loc[1], timeStamp);
		return obs;
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	private Date getRandomDate(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		long difference = endTime - startTime;

		long fraction = (long) (difference * _generator.nextDouble());
		long obsTimeMillis = fraction + startTime;

		Calendar obsCalendar = Calendar.getInstance();
		obsCalendar.setTimeInMillis(obsTimeMillis);

		return obsCalendar.getTime();
	}

	/**
	 * @return
	 */
	private Observation generateRandomObservation() {
		String observationUri = getObservationUri();
		String observableUri = getRandomObservable(_observableUriList);
		double[] loc = getRandomLocation();
		Date timeStamp = getRandomDate();
		Observation obs = new Observation(observationUri, observableUri,
				loc[0], loc[1], timeStamp);

		// Observation obs = new Observation(observationUri, observableUri,
		// loc[0], loc[1], timeStamp);
		return obs;
	}

	/**
	 * @return
	 */
	private Observation generateRandomObservation(double[] center) {
		String observationUri = getObservationUri();
		String observableUri = getRandomObservable(_observableUriList);
		double[] loc = getRandomLocation(center);
		Date timeStamp = getRandomDate();
		Observation obs = new Observation(observationUri, observableUri,
				loc[0], loc[1], timeStamp);

		// Observation obs = new Observation(observationUri, observableUri,
		// loc[0], loc[1], timeStamp);
		return obs;
	}

	/**
	 * @return
	 */
	private String getObservationUri() {
		int currentObs = _obsCounter;
		_obsCounter++;
		return BASE_OBSERVATION_URI + currentObs;
	}

	/**
	 * @return
	 */
	private double[] getRandomLocation(double[] center) {

		// This code to convert distance, bearing into location is from
		// http://www.movable-type.co.uk/scripts/latlong.html

		double bearing = 360.0 * _generator.nextDouble();
		// distance is in kilometers;
		double distanceRatio = getDistanceRatio();
		double distance = distanceRatio * _radius;

		double[] randPoint = findPointBearingDistance(center, bearing, distance);

		// System.out.println("-----------------");
		// System.out.println(_center[0]+","+_center[1]);
		// System.out.println(distance);
		// System.out.println(bearing);
		// System.out.println(DistanceCalculator.getDistance(_center[0],
		// _center[1], randPoint[0], randPoint[1]));
		// System.out.println(randPoint[0]+","+randPoint[1]);
		// System.out.println("-----------------");

		return randPoint;
	}

	/**
	 * @return
	 */
	private double[] getRandomLocation() {

		double[] randPoint = new double[2];
		randPoint[0] = 180.0 * _generator.nextDouble() - 90.0;
		randPoint[1] = 360.0 * _generator.nextDouble() - 180.0;

		return randPoint;
	}

	/**
	 * @return
	 */
	private double getDistanceRatio() {
		double areaEnclosed = _generator.nextDouble();
		double radius = Math.sqrt(areaEnclosed / Math.PI);
		return radius;
	}

	/**
	 * @return
	 */
	private double[] findPointBearingDistance(double[] point, double bearing,
			double distance) {
		// """
		// http://stackoverflow.com/questions/877524/calculating-coordinates-given-a-bearing-and-a-distance
		// """
		double rlat1 = Math.toRadians(point[0]);
		double rlon1 = Math.toRadians(point[1]);
		double rbearing = Math.toRadians(bearing);

		double rdistance = distance / EARTH_RADIUS_KM;

		double rlat = Math.asin(Math.sin(rlat1) * Math.cos(rdistance)
				+ Math.cos(rlat1) * Math.sin(rdistance) * Math.cos(rbearing));
		double rlon = 0.0;

		if ((Math.cos(rlat) == 0) || (Math.abs(Math.cos(rlat)) < 0.0001)) {
			rlon = rlon1;
		} else {
			rlon = ((rlon1
					- Math.asin(Math.sin(rbearing) * Math.sin(rdistance)
							/ Math.cos(rlat)) + Math.PI) % (2 * Math.PI))
					- Math.PI;
		}

		double[] retPoint = new double[2];
		retPoint[0] = Math.toDegrees(rlat);
		retPoint[1] = Math.toDegrees(rlon);

		return retPoint;
	}

	/**
	 * @return
	 */
	private Date getRandomDate() {
		long startTime = _startDate.getTime();
		long endTime = _endDate.getTime();
		long difference = endTime - startTime;

		long fraction = (long) (difference * _generator.nextDouble());
		long obsTimeMillis = fraction + startTime;

		Calendar obsCalendar = Calendar.getInstance();
		obsCalendar.setTimeInMillis(obsTimeMillis);

		return obsCalendar.getTime();
	}

	/**
	 * @return
	 */
	private String getRandomObservable(List<String> obsUriList) {
		int randIndex = _generator.nextInt(obsUriList.size());
		return obsUriList.get(randIndex);
	}

	/**
	 * @return
	 */
	private static Date getStartDate(long startTimeMillis) {

		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTimeInMillis(startTimeMillis);

		return startCalendar.getTime();
	}

	/**
	 * @return
	 */
	private static Date getEndDate(long endTimeMillis) {

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTimeInMillis(endTimeMillis);

		return endCalendar.getTime();
	}

	public Set<Observation> generateObservations(
			int numTrueObservationLocations,
			int numNumCollocatedMatchesPerLocation,
			int numNoiseObservationPerLocation,
			int numDispersedNoiseObservations) {

		Set<Observation> retObservations = new HashSet<Observation>();

		Set<Observation> noiseObservations = generateNoise(numDispersedNoiseObservations);

		Set<Observation> trueObservations = generateTrue(
				numTrueObservationLocations,
				numNumCollocatedMatchesPerLocation,
				numNoiseObservationPerLocation);

		retObservations.addAll(noiseObservations);
		retObservations.addAll(trueObservations);

		return retObservations;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int maxPathLen = 10;
		int numPaths = 1;
		int maxObservablesPerAct = 2;
		double percent = 1;

		long startTimeMillis = 0;
		long endTimeMillis = 10000000;

		Date startDate = ProcessObservationGenerator
				.getStartDate(startTimeMillis);
		Date endDate = ProcessObservationGenerator.getEndDate(endTimeMillis);

		long maxProcessInterval = 100000;
		double radius = 10;

		RandomProcessGenerator r = new RandomProcessGenerator();
		Model randomModel = r.createProcess(maxPathLen, numPaths,
				maxObservablesPerAct, percent);
		RandomProcessGenerator.printModel(randomModel);

		ProcessObservationGenerator processObsGen = new ProcessObservationGenerator(
				randomModel, startDate, endDate, maxProcessInterval, radius);

		int numDispersedNoiseObservations = 100;
		int numTrueObservationLocations = 1;
		int numNumCollocatedMatchesPerLocation = 2;
		int numNoiseObservationPerLocation = 5;

		Set<Observation> noiseObservations = processObsGen
				.generateNoise(numDispersedNoiseObservations);

		Set<Observation> trueObservations = processObsGen.generateTrue(
				numTrueObservationLocations,
				numNumCollocatedMatchesPerLocation,
				numNoiseObservationPerLocation);

		Set<Observation> composedObservations = processObsGen
				.generateObservations(numTrueObservationLocations,
						numNumCollocatedMatchesPerLocation,
						numNoiseObservationPerLocation,
						numDispersedNoiseObservations);

	}
}
