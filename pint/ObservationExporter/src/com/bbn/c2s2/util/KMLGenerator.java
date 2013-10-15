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
import java.net.URI;
import java.util.Collection;

import edu.jhuapl.c2s2.pp.observation.Observation;

public class KMLGenerator {
	private final String KML_TEMPLATE_FILE = "conf/kml-template.txt";
	private final String PLACEMARK_TEMPLATE_FILE = "conf/kml-placemark-template.txt";
	private final String ENDL = System.getProperty("line.separator");
	private SampleDataLoader loader;

	public KMLGenerator() {
		loader = new SampleDataLoader();
	}

	public void load(File observations, File noise, File truth)
			throws Exception {
		loader.loadObservations(observations, noise, truth);
	}

	public void writeKML() throws Exception {
		String documentTemplate = readTemplateFile(new File(KML_TEMPLATE_FILE));
		String placemarkTemplate = readTemplateFile(new File(
				PLACEMARK_TEMPLATE_FILE));
		String truthPlacemarks = generatePlacemarks(loader.getTruthList(),
				"truthStyle", placemarkTemplate);
		String noisePlacemarks = generatePlacemarks(loader.getNoiseList(),
				"noiseStyle", placemarkTemplate);
		String remainingPlacemarks = generatePlacemarks(loader
				.getObservationHash().values(), "remainingStyle",
				placemarkTemplate);
		String resultContent = String.format(documentTemplate, truthPlacemarks,
				noisePlacemarks, remainingPlacemarks);
		System.out.println(resultContent);
	}

	static String uriToName(String uriString) throws Exception {
		String rv = "";
		URI uri = new URI(uriString);
		// strip off everything but the fragment
		rv = uri.getFragment();
		// strip off the time and location bits
		rv = rv.substring(0, rv.length() - 36);
		// replace & with 'and'
		rv = rv.replaceAll("\\&", "and").replaceAll("_", " ");
		return rv;
	}

	private String generatePlacemarks(Collection<Observation> obs,
			String styleID, String template) throws Exception {
		StringBuilder rv = new StringBuilder();
		String nameString = "";
		String dateString = "";
		String lonString = "";
		String latString = "";
		for (Observation o : obs) {
			nameString = uriToName(o.getUri());
			dateString = Constants.DATE_FORMAT_XSD.format(o
					.getObservationTimestamp().getTime());
			lonString = Double.toString(o.getGeocode().getLongitude());
			latString = Double.toString(o.getGeocode().getLatitude());
			rv.append(String.format(template, nameString, styleID, dateString,
					lonString, latString, dateString));
		}
		return rv.toString();
	}

	private String readTemplateFile(File templateFile) throws Exception {
		StringBuilder rv = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		String line = "";
		while ((line = reader.readLine()) != null) {
			rv.append(line + ENDL);
		}
		return rv.toString();
	}

	private String createXsdDateTime(String date, String time) {
		String rv = "";
		String[] aDate = date.split("/");
		String[] aTime = time.split(":");
		rv = String.format("20%1$s-%2$s-%3$sT%4$s:%5$s:%6$s-0500", aDate[2],
				aDate[0], aDate[1], aTime[0], aTime[1], aTime[2]);
		return rv;
	}

	public static void main(String[] args) throws Exception {
		KMLGenerator gen = new KMLGenerator();
		gen.load(Constants.FILE_OBSERVATIONS_DATA, Constants.FILE_NOISE_CSV,
				Constants.FILE_TRUTH_CSV);
		gen.writeKML();
	}

}
