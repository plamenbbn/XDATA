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

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import com.bbn.c2s2.pint.configuration.Constants;

import edu.jhuapl.c2s2.processfinderenterprisebus.ClientIF;
import edu.jhuapl.c2s2.processfinderenterprisebus.ClientIF_Service;
import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class PintServerClient implements IProcessFinderClient {

	private String _wsdl;
	private ClientIF _wsClient;
	final int SLEEP_MS = 5000;

	public PintServerClient(String wsdlUrl) throws Exception {
		_wsdl = wsdlUrl;
		setupWSClient();
	}

	private void setupWSClient() throws Exception {
		ClientIF_Service service = new ClientIF_Service(new URL(_wsdl),
				new QName("http://processFinderEnterpriseBus.c2s2.jhuapl.edu/",
						"ClientIF"));

		_wsClient = service.getPfebWSInterfaceImplPort();

	}

	@Override
	public List<DetectedProcess> solicitDetection(
			List<SimpleObservation> observations, Properties parameters,
			String obsGroupUri) {
		List<DetectedProcess> rv = null;
		if (null != _wsClient) {
			double maxDistance = Double.parseDouble(parameters.getProperty(
					Constants.KEY_PROCESS_MAX_DISTANCE_KM, "1")) * 1000;
			double maxTimespan = Double.parseDouble(parameters.getProperty(
					Constants.KEY_PROCESS_MAX_TIMESPAN_MS, String
							.valueOf(Double.MAX_VALUE)));

			_wsClient.solicitedDetectionOnNewObservations(observations,
					obsGroupUri, (int) maxDistance, maxTimespan);
			for (int i = 0; i < 100; i++) {
				try {
					Thread.sleep(SLEEP_MS);
				} catch (InterruptedException ie) {
					System.err.println("Interrupted Exception? Whatever.");
				}
				System.out.println("Checking status...");
				List<DetectedProcess> results = _wsClient
						.requestDetectedProcessesOnGroup(obsGroupUri);
				if (null != results && results.size() > 0) {
					String status = results.get(0).getProcessIdUri();
					if(Constants.WSStatus.COMPLETED.equals(status)
						|| Constants.WSStatus.REQUEST_FAILED.equals(status)){
						//have to remove the dummy result before we send it back
						if(null != results && results.size() > 0){
							results.remove(0);
						}
						rv = results;
						
						break;
					}
				}
			}
		}
		return rv;
	}

}
