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
package com.bbn.c2s2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bbn.c2s2.util.ObservationLoader;

import edu.jhuapl.c2s2.pp.observation.Observation;

public class KMLGenerator {
	private static final String KML_TEMPLATE_FILE = "conf/kml-template.txt";
	private static final String PLACEMARK_TEMPLATE_FILE = "conf/kml-placemark-template.txt";
	private static final String ENDL = System.getProperty("line.separator");
	public static final DateFormat DATE_FORMAT_XSD = new SimpleDateFormat(
	"yyyy-MM-dd'T'HH:mm:ssZ");

	public static boolean writeKML(Collection<Observation> observations, Writer kmlFile) {
		boolean rv = true;
		try {
		String documentTemplate = readTemplateFile(new File(KML_TEMPLATE_FILE));
		String placemarkTemplate = readTemplateFile(new File(
				PLACEMARK_TEMPLATE_FILE));
		List<Observation> truth = new ArrayList<Observation>();
		List<Observation> noise = new ArrayList<Observation>();
		for(Observation o : observations) {
			if(o.getUri().indexOf("truth") > -1) {
				truth.add(o);
			} else {
				noise.add(o);
			}
		}
		String truthPlacemarks = generatePlacemarks(truth,
				"truthStyle", placemarkTemplate);
		String noisePlacemarks = generatePlacemarks(noise,
				"noiseStyle", placemarkTemplate);
		String remainingPlacemarks = generatePlacemarks(new ArrayList<Observation>(), "remainingStyle",
				placemarkTemplate);
		String resultContent = String.format(documentTemplate, truthPlacemarks,
				noisePlacemarks, remainingPlacemarks);
		kmlFile.write(resultContent);
		kmlFile.close();
		} catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			rv = false;
		}
		return rv;
	}

	private static String generatePlacemarks(Collection<Observation> obs,
			String styleID, String template) throws Exception {
		StringBuilder rv = new StringBuilder();
		String nameString = "";
		String dateString = "";
		String lonString = "";
		String latString = "";
		for (Observation o : obs) {
			nameString = ObservationLoader.getLabel(o.getUri());
			dateString = DATE_FORMAT_XSD.format(o
					.getObservationTimestamp().getTime());
			lonString = Double.toString(o.getGeocode().getLongitude());
			latString = Double.toString(o.getGeocode().getLatitude());
			rv.append(String.format(template, nameString, styleID, dateString,
					lonString, latString, dateString));
		}
		return rv.toString();
	}

	private static String readTemplateFile(File templateFile) throws Exception {
		StringBuilder rv = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		String line = "";
		while ((line = reader.readLine()) != null) {
			rv.append(line + ENDL);
		}
		return rv.toString();
	}

	private static String createXsdDateTime(String date, String time) {
		String rv = "";
		String[] aDate = date.split("/");
		String[] aTime = time.split(":");
		rv = String.format("20%1$s-%2$s-%3$sT%4$s:%5$s:%6$s-0500", aDate[2],
				aDate[0], aDate[1], aTime[0], aTime[1], aTime[2]);
		return rv;
	}
}
