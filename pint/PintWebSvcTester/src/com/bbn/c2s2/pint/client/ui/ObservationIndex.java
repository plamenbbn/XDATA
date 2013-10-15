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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class ObservationIndex {
	private List<SimpleObservation> _obs;
	private Map<String, SimpleObservation> _map;

	public ObservationIndex(List<SimpleObservation> observations) {
		_obs = observations;
		buildHash();
	}

	private void buildHash() {
		_map = new HashMap<String, SimpleObservation>(_obs.size());
		for (SimpleObservation o : _obs) {
			_map.put(o.getUri(), o);
		}
	}

	public SimpleObservation getObservation(String uri) {
		return _map.get(uri);
	}

	public List<SimpleObservation> getObservations() {
		return _obs;
	}
}
