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

import java.util.List;
import java.util.Properties;

import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public interface IProcessFinderClient {
	// List<DetectedProcess> requestDetectedProcesses(String obsGroupUri);
	List<DetectedProcess> solicitDetection(
			List<SimpleObservation> observations, Properties parameters,
			String obsGroupUri);
}
