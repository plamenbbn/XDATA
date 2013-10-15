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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.hp.hpl.jena.rdf.model.Model;

import edu.jhuapl.c2s2.pp.observation.Observation;
import edu.jhuapl.c2s2.pp.observation.SimpleGeocode;
import edu.jhuapl.c2s2.pp.observation.SimpleObservation;

public class SampleDataLoader {
	private boolean _observationsLoaded = false;
	private boolean _processLoaded = false;
	private List<Observation> noiseList;
	private List<Observation> truthList;
	private Map<String, Observation> observationHash;
	private List<Observation> allObservations;
	private Model processModel;

	public SampleDataLoader() {
		observationHash = new HashMap<String, Observation>();
		noiseList = new ArrayList<Observation>();
		truthList = new ArrayList<Observation>();
	}

	public void loadObservations(File observations, File noise, File truth)
			throws Exception {
		if (!_observationsLoaded) {
			allObservations = ObservationLoader.loadObservations(observations);
			for (Observation obs : allObservations) {
				observationHash.put(obs.getUri(), obs);
			}
			// load noise and find in observations
			List<CSVObservation> csvNoiseList = loadCSVObservations(noise);
			for (CSVObservation o : csvNoiseList) {
				if (observationHash.containsKey(o.getUri())) {
					noiseList.add(observationHash.remove(o.getUri()));
				} else {
					System.err.println("Cannot find " + o.getUri()
							+ " from noise.");
				}
			}
			// load truth and find in observations
			List<CSVObservation> csvTruthList = loadCSVObservations(truth);
			for (CSVObservation o : csvTruthList) {
				if (observationHash.containsKey(o.getUri())) {
					truthList.add(observationHash.remove(o.getUri()));
				} else {
					System.err.println("Cannot find " + o.getUri()
							+ " from truth.");
				}
			}
			_observationsLoaded = true;
		}
	}

	private List<CSVObservation> loadCSVObservations(File csvFile)
			throws Exception {
		List<CSVObservation> rv = new ArrayList<CSVObservation>();
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		String line = null;
		reader.readLine(); // skip the field names
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(",");
			CSVObservation o = new CSVObservation();
			o.date = parts[2];
			o.time = parts[3];
			o.lon = parts[4];
			o.lat = parts[5];
			o.description = parts[6];
			rv.add(o);
		}
		return rv;
	}

	public List<Observation> getAllObservations() throws Exception {
		if (!_observationsLoaded) {
			throw new Exception(
					"Invalid state: Must call loadObservations() first.");
		}
		return allObservations;
	}

	public List<Observation> getNoiseList() throws Exception {
		if (!_observationsLoaded) {
			throw new Exception(
					"Invalid state: Must call loadObservations() first.");
		}
		return noiseList;
	}

	public List<Observation> getTruthList() throws Exception {
		if (!_observationsLoaded) {
			throw new Exception(
					"Invalid state: Must call loadObservations() first.");
		}
		return truthList;
	}

	public List<Observation> getPerfectList() throws Exception {
		if (!_observationsLoaded) {
			throw new Exception(
					"Invalid state: Must call loadObservations() first.");
		}
		List<Observation> truth = getTruthList();
		List<Observation> rv = new ArrayList<Observation>(truth.size());
		HashMap<String, Date> map = new HashMap<String, Date>();
		map.put("2009-02-06T08:00:00-0500", DatatypeConverter.parseDateTime(
				"2009-02-06T15:00:00-05:00").getTime());
		map.put("2009-02-06T13:00:00-0500", DatatypeConverter.parseDateTime(
				"2009-02-07T10:00:00-05:00").getTime());
		map.put("2009-02-07T14:30:00-0500", DatatypeConverter.parseDateTime(
				"2009-02-07T08:00:00-05:00").getTime());
		map.put("2009-02-07T23:00:00-0500", DatatypeConverter.parseDateTime(
				"2009-02-06T15:30:00-05:00").getTime());
		map.put("2009-02-09T13:40:00-0500", DatatypeConverter.parseDateTime(
				"2009-02-08T13:40:00-05:00").getTime());

		for (Observation o : truth) {
			Date newDate = o.getObservationTimestamp().getTime();
			String dateString = Constants.DATE_FORMAT_XSD.format(newDate);
			if (map.containsKey(dateString)) {
				newDate = map.get(dateString);
			}
			rv.add(replaceTimeAndShiftLongitude(o, newDate));
		}
		return rv;
	}

	private Observation replaceTimeAndShiftLongitude(Observation o, Date newTime) {
		SimpleObservation rv = null;
		SimpleGeocode geo = new SimpleGeocode(o.getGeocode().getLatitude(), o
				.getGeocode().getLongitude() - 20.0);
		rv = new SimpleObservation(o.getUri(), o.getObservableUri(), o
				.getClassification(), calendarFromDate(newTime), geo, o
				.getCreationTime(), o.getCreator(), o.getSource(), o
				.getOriginatingSource());
		return rv;
	}

	private Calendar calendarFromDate(Date d) {
		Calendar rv = Calendar.getInstance();
		rv.setTime(d);
		return rv;
	}

	public Map<String, Observation> getObservationHash() throws Exception {
		if (!_observationsLoaded) {
			throw new Exception(
					"Invalid state: Must call loadObservations() first.");
		}
		return observationHash;
	}

	public void loadProcesses(File processFile) throws Exception {
		if (!_processLoaded) {
			processModel = RNRMLoader.loadProcesses(processFile);
		}
	}

	public Model getProcessModel() throws Exception {
		if (!_processLoaded) {
			throw new Exception(
					"Invalid state: Must call loadProcesses() first.");
		}
		return processModel;
	}

	/**
	 * CSVObservation class
	 * 
	 * Represents the observation details read from the CSV data files provided
	 * by Jaime
	 * 
	 * @author tself
	 * 
	 */
	class CSVObservation {
		String description;
		String lat;
		String lon;
		String date;
		String time;

		/**
		 * getUri()
		 * 
		 * @return String of the Uri built up the same way Jaime did it in the
		 *         test data
		 */
		String getUri() {
			StringBuilder rv = new StringBuilder(
					"http://www.c2s2.jhuapl.edu/sdg#");
			rv.append(description.replace(' ', '_').replaceAll("\\.", ""));
			String[] aDate = date.split("/");
			String[] aTime = time.split(":");
			rv.append(String.format("_%1$s%2$s%3$s_", aDate[0], aDate[1],
					aDate[2]));
			rv.append(String.format("%1$s%2$s_", aTime[0], aTime[1]));
			rv.append(lon.replace('.', '-'));
			rv.append("_");
			rv.append(lat.replace('.', '-'));

			return rv.toString();
		}
	}
}
