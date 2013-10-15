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
package com.bbn.c2s2.pint.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.ObservationLoader;
import com.bbn.c2s2.util.SampleDataLoader;

import edu.jhuapl.c2s2.processfinderenterprisebus.ClientIF;
import edu.jhuapl.c2s2.processfinderenterprisebus.ClientIF_Service;
import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.ObservationToActivityMapping;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class PintTestClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		final int SLEEP_MS = 10000;

		List<edu.jhuapl.c2s2.pp.observation.Observation> allObservations = ObservationLoader
				.loadObservations(Constants.FILE_OBSERVATIONS_DATA);

		SampleDataLoader loader = new SampleDataLoader();
		loader.loadObservations(Constants.FILE_OBSERVATIONS_DATA,
				Constants.FILE_NOISE_CSV, Constants.FILE_TRUTH_CSV);

		// VERSION A: RUN THEIR 5000 OBSERVATIONS
		// List<edu.jhuapl.c2s2.pp.observation.Observation> allObservations =
		// loader.getAllObservations();

		// // VERSION B: RUN JUST THE 20 GROUND-TRUTH OBJECTS
		// List<edu.jhuapl.c2s2.pp.observation.Observation> groundTruth = loader
		// .getTruthList();
		//
		// // VERSION C: RUN THE GROUND-TRUTH WITH TIMES FIXED TO BE PERFECT
		// // AND LONGITUDES SHIFTED BY 20 DEGREES TO SEPARATE THE CLUSTER
		// allObservations = loader.getPerfectList();
		//
		// // VERSION D: RUN THEIR 5000 OBERVATIONS WITH OUR 20 PERFECTS ADDED
		// IN
		// List<edu.jhuapl.c2s2.pp.observation.Observation> allPlusPerfects =
		// loader
		// .getAllObservations();
		// allPlusPerfects.addAll(perfects);
		//
		// List<edu.jhuapl.c2s2.pp.observation.Observation> testSet =
		// groundTruth;

		// convert them into web-serviceable SimpleObservations
		List<SimpleObservation> observations = new ArrayList<SimpleObservation>(
				JhuToWsConversionFactory.convertObservations(allObservations));

		System.out.println(String.format(
				"About to hit the web service with %1$s Observations.",
				allObservations.size()));

		ClientIF_Service service = new ClientIF_Service(new URL(
				"http://localhost:8080/PFEB_C2S2_IF/ClientIF?wsdl"), new QName(
				"http://processFinderEnterpriseBus.c2s2.jhuapl.edu/",
				"ClientIF"));

		ClientIF client = service.getPfebWSInterfaceImplPort();

		String obsGroup = "http://sampleset" + System.currentTimeMillis();
		System.out.println("Observation Group: " + obsGroup);

		client.solicitedDetectionOnNewObservations(observations, obsGroup, 1,
				5.5);

		System.out.println(String.format(
				"Waiting %1$d seconds and then requesting results.",
				SLEEP_MS / 1000));

		for (int i = 0; i < 100; i++) {
			Thread.sleep(SLEEP_MS);
			System.out.println("Checking status...");
			List<DetectedProcess> results = client
					.requestDetectedProcessesOnGroup(obsGroup);
			if (results.size() > 0) {
				for (DetectedProcess dp : results) {
					System.out
							.println("Found Process: " + dp.getProcessIdUri());
					System.out.println("     Score: " + dp.getScore());
					System.out.println("  Earliest: "
							+ dp.getEarliestObservationTimeStamp());
					System.out.println("    Latest: "
							+ dp.getLatestObservationTimeStamp());
					System.out.println("      Span: "
							+ dp.getDetectedProcessTimeSpanInSeconds());
					for (ObservationToActivityMapping map : dp
							.getDetectedActivities()) {
						System.out.println(String.format("  %1$s -> %2$s", map
								.getActivityUri(), map.getObservationUri()));
					}
					System.out.println("---------------------");
					System.out.println("");
				}
				break;
			} else {
				System.out.println(String.format(
						"   ... nothing yet. Waiting %1$d seconds.",
						SLEEP_MS / 1000));
			}
		}
	}
}
