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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.pf.util.DistanceCalculator;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ObservationGenerator {
	
	private static final double MILLIS_IN_DAY = 86400000; 
	private static final double MAX_LAT = 90;
	private static final double MAX_LON = 180;
	
	private DataGenConfig config;
	private Window dataWindow;
	private Random random = new Random(467644);
	
	private class Window {
		private double x1, x2, y1, y2;
		public Window(double x1, double y1, double x2, double y2) throws Exception {
			this.init(x1, y1, x2, y2);
		}
		
		public Window(Location minCornerPoint, Location maxCornerPoint) throws Exception {
			this.init(minCornerPoint.x, minCornerPoint.y, maxCornerPoint.x, maxCornerPoint.y);
		}
		
		private void init(double x1, double y1, double x2, double y2) throws Exception {
			if (x1 >x2 || y1>y2) throw new Exception("Not a valid window.  (x1 must be less than x2, y1 must be less than y2)");
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}
		
		public Location selectRandomPoint(Random r) {
			double randX = x1 + r.nextDouble()*(x2-x1);
			double randY = y1 + r.nextDouble()*(y2-y1);
			return new Location(randX, randY);
		}
		
		public Location selectRandomPointInEnclosedCircle(Random r) {
			double centerX = (x2 + x1) / 2;
			double centerY = (y2 + y1) / 2;
			double radiusSquared = Math.pow(  ((x2 - x1) / 2) , 2  );

			Location l;
			while (true ) {
				l = this.selectRandomPoint(r);
				if ( Math.pow( (l.x - centerX), 2)  + Math.pow( (l.y - centerY), 2)< radiusSquared  ) {
					break;
				}
			}
			return l;
		}
	}
	
	private class Location {
		private double x, y;
		public Location(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	private long daysToMillis(double days) {
		return (long) (days * MILLIS_IN_DAY);
	}
	
	public double findLat(Location minCorner, double targetDist) {
		Location maxPointEst = new Location(MAX_LAT, minCorner.y);
		Location minPointEst = minCorner;
		Location midPoint = minCorner;
		double currentDist = Double.MAX_VALUE;
		while (Math.abs(targetDist - currentDist) > .001) {
			double t = (minPointEst.x + maxPointEst.x) / 2;
			midPoint = new Location(t, minPointEst.y);
			currentDist = DistanceCalculator.getDistance(minCorner.x, minCorner.y, midPoint.x, midPoint.y);
			if (currentDist > targetDist) {
				maxPointEst = midPoint;
			}
			else {
				minPointEst = midPoint;
			}
		}
		return midPoint.x;
	}
	
	public double findLon(Location minCorner, double targetDist) {
		Location maxPointEst = new Location(minCorner.x, MAX_LON);
		Location minPointEst = minCorner;
		Location midPoint = minCorner;
		double currentDist = Double.MAX_VALUE;
		while (Math.abs(targetDist - currentDist) > .001) {
			double t = (minPointEst.y + maxPointEst.y) / 2;
			midPoint = new Location(minPointEst.x, t);
			currentDist = DistanceCalculator.getDistance(minCorner.x, minCorner.y, midPoint.x, midPoint.y);
			if (currentDist > targetDist) {
				maxPointEst = midPoint;
			}
			else {
				minPointEst = midPoint;
			}
		}
		return midPoint.y;
	}
	
	public Window createNewWindow(Location centerPoint, double enclosedCircleRadius) throws Exception {
		double topCornerLat = this.findLat(centerPoint, enclosedCircleRadius);
		double topCornerLon = this.findLon(centerPoint, enclosedCircleRadius);
		double latDist = topCornerLat - centerPoint.x;
		double lonDist = topCornerLon - centerPoint.y;
		double bottomCornerLat = centerPoint.x - latDist;
		double bottomCornerLon = centerPoint.y - lonDist;
		return new Window(bottomCornerLat,
				bottomCornerLon,
				topCornerLat,
				topCornerLon);
	}
	
	
	public long pickProcessStartTime() {
		return (long) (config.getStartDate().getTime() + this.random.nextDouble()*(config.getEndDate().getTime() - config.getStartDate().getTime()));
	}
	
	
	public ObservationGenerator(DataGenConfig config) throws Exception {
		this.init(config);
	}
	
	
	private void init(DataGenConfig config) throws Exception {
		this.config = config;
		Location center = new Location(config.getLatitude(), config.getLongitude());
		this.dataWindow = this.createNewWindow(center, config.getDataRadiusKm());
	}
	
	public List<Observation> makeNoise(Model m, String processUri, int signalSize) throws Exception {
		
		
		RnrmProcess proc = RnrmProcessFactory.createProcess(m, processUri);
		List<Activity> actsInProc = proc.getOrderedActivities();
		Map<String, List<String>> actsToObservables = this.buildActivitiesToObservablesMap(actsInProc, m);
		
		List<Observation> rv = new ArrayList<Observation>();
		double signal = signalSize;
		int numNoise = (int) (signal / config.getSignalNoiseRatio());
		int noiseId = 0;
		for (int i = 0 ; i < numNoise; i++) {
			Location l = this.dataWindow.selectRandomPointInEnclosedCircle(random);
			long time = (long) (config.getStartDate().getTime() +
						this.random.nextDouble() *
						(config.getEndDate().getTime() - config.getStartDate().getTime()));
			String observationUri = "n" + noiseId;
			Activity randomAct = actsInProc.get(this.random.nextInt(actsInProc.size()));
			List<String> observables = actsToObservables.get(randomAct.getActivityURI());
			String observableUri = observables.get(this.random.nextInt(observables.size()));
			Observation o = new Observation(observationUri, observableUri, l.x, l.y, new Date(time));
			rv.add(o);
			noiseId++;
		}
		return rv;
	}
	
	private Map<String, List<String>> buildActivitiesToObservablesMap(List<Activity> activities, Model m) {
		HashMap<String, List<String>> rv = new HashMap<String, List<String>>();
		for (Activity a : activities) {
			List<String> observables = new ArrayList<String>();
			StmtIterator it = m.listStatements(m.createResource(a.getActivityURI()), RNRM.hasObservable, (Resource) null);
			while (it.hasNext()) {
				String obs = it.nextStatement().getObject().toString();
				observables.add(obs);
			}
			rv.put(a.getActivityURI(), observables);
		}
		return rv;
	}
	
	public List<Observation> makeTruthSet(Model m, String processUri) throws Exception {
		
		StmtIterator it = m.listStatements();

		RnrmProcess proc = RnrmProcessFactory.createProcess(m, processUri);
		List<Activity> actsInProc = proc.getOrderedActivities();

		
		Map<String, List<String>> actsToObservables = this.buildActivitiesToObservablesMap(actsInProc, m);		
		
		
		List<Observation> rv = new ArrayList<Observation>();
		long startTime = this.pickProcessStartTime();
		long endTime = startTime + this.daysToMillis(config.getTruthDurationDays());
		long step = (endTime - startTime) / actsInProc.size();
		Location center = this.dataWindow.selectRandomPointInEnclosedCircle(random);
		Window truthWindow = this.createNewWindow(center, config.getTruthRadiusKm());
		long currentTime = startTime;
		int observationId = 0;
		for (Activity a : actsInProc) {
			Location observationLocation = truthWindow.selectRandomPointInEnclosedCircle(random);
			String observationUri = "truth" + observationId;
			String observableUri = this.selectObservable(a.getActivityURI(), actsToObservables);
			Observation o = new Observation(observationUri, observableUri, 
					observationLocation.x, observationLocation.y, 
					new Date(currentTime));
			rv.add(o);
			currentTime += step;
			observationId++;
		}
		// now remove some of them based on the percent visibility
		int numToRemove = (int)(Math.ceil(rv.size() * (1.0 - config.getPercentVisible())));
		// don't remove all of them
		if(numToRemove == rv.size()) {
			numToRemove--;
		}
		for(int i = 0; i < numToRemove; i++) {
			rv.remove(random.nextInt(rv.size()));
		}
		return rv;
	}
	
	private String selectObservable(String actUri, Map<String, List<String>> actsToObservables) {
		List<String> obs = actsToObservables.get(actUri);
		return obs.get(this.random.nextInt(obs.size()));
	}
}
