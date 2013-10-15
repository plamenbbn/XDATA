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
package com.bbn.c2s2.pint.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.ISolutionReport;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.ProcessAssignment;
import com.bbn.c2s2.pint.ProcessMultiplexer;
import com.bbn.c2s2.pint.client.WsToPintConversionFactory;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.factories.DetectedProcessFactory;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.SolutionReport;
import com.hp.hpl.jena.rdf.model.Model;

import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class LocalClient implements IProcessFinderClient {
	private static Logger logger = LoggerFactory.getLogger(LocalClient.class);
	private Model _rnrm;

	public LocalClient(Model rnrm) {
		_rnrm = rnrm;
	}

	@Override
	public List<DetectedProcess> solicitDetection(
			List<SimpleObservation> observations, Properties parameters,
			String obsGroupUri) {
		long start = System.currentTimeMillis();
		List<DetectedProcess> rv = new ArrayList<DetectedProcess>();
		List<Observation> pintObs = new ArrayList<Observation>(
				WsToPintConversionFactory.convertObservations(observations));
		PintConfiguration config = new PintConfiguration(parameters);

		ProcessMultiplexer pMux = new ProcessMultiplexer(_rnrm);

		pMux.initialize();
		Collection<ProcessAssignment> assignments = null;
		try {
			assignments = pMux.getProcessAssignments(pintObs);
		} catch (Exception e) {
			System.err
					.println("Error occurred while determining process assignments.");
			e.printStackTrace(System.err);
			assignments = new ArrayList<ProcessAssignment>();
		}

		for (ProcessAssignment assign : assignments) {
			System.out
					.println(String.format(
							"Assignment: %1$s (%2$d candidates)", assign
									.getProcess().toString(), assign
									.getCandidates().bindingCount()));

			RnrmProcess process = assign.getProcess();
			BindingGroup bindings = assign.getCandidates();
			ProcessFinder processFinder = null;
			boolean valid = true;
			try {
				processFinder = new ProcessFinder(process, bindings, config);
			} catch (InvalidProcessException ipe) {
				logger
						.error(
								"Invalid RnrmProcess. Unable to proceed. Producing 0 SolutionReports.",
								ipe);
				valid = false;
			}
			List<SolutionReport> reports = null;
			if (valid) {
				reports = processFinder.find();
			} else {
				reports = new ArrayList<SolutionReport>();
			}

			System.out.println(String.format("Computation Time: %1$f",
					(double) (System.currentTimeMillis() - start) / 1000.0f));
			for (ISolutionReport report : reports) {
				DetectedProcess dp = DetectedProcessFactory
						.createDetectedProcess(report);
				dp.setObservationGroupUri(obsGroupUri);
				dp.setObservationGroupVersionUri(obsGroupUri + "/0");
				dp.setRnrmVersion(0);
				rv.add(dp);
			}
		}
		return rv;
	}
}
