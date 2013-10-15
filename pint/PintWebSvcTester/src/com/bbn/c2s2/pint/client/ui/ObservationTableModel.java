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
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.util.ObservationLoader;
import com.hp.hpl.jena.rdf.model.Model;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class ObservationTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6024962747284638751L;

	// Observation URI | Time | Lat | Lon | Observable URI
	private String[] _columnNames = { "Observation", "Time", "Latitude",
			"Longitude", "Observable" };
	private List<SimpleObservation> _obs;
	private Model _rnrm;

	public ObservationTableModel() {
		_obs = new ArrayList<SimpleObservation>();
	}

	public ObservationTableModel(List<SimpleObservation> observations) {
		_obs = observations;
	}

	public void addObservations(List<SimpleObservation> observations) {
		_obs.addAll(observations);
		fireTableDataChanged();
	}

	public void setObservations(List<SimpleObservation> observations) {
		_obs = observations;
		fireTableDataChanged();
	}

	public void setRnrmModel(Model m) {
		_rnrm = m;
		fireTableDataChanged();
	}

	public Model getRnrmModel() {
		return _rnrm;
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	@Override
	public int getRowCount() {
		return _obs.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		SimpleObservation obs = _obs.get(row);
		switch (col) {
		case 0:
			return getObservationLabel(obs);
		case 1:
			return obs.getObservationTime().toXMLFormat();
		case 2:
			return new Double(obs.getLocation().getLatitude());
		case 3:
			return new Double(obs.getLocation().getLongitude());
		case 4:
			return getObservableLabel(obs);
		default:
			return "Why did you ask for this?";
		}
	}

	private String getObservationLabel(SimpleObservation obs) {
		return ObservationLoader.getLabel(obs.getUri());
	}

	private String getObservableLabel(SimpleObservation obs) {
		return (null != _rnrm) ? RDFHelper.getLabel(_rnrm, obs
				.getObservableUri()) : obs.getObservableUri();
	}

	@Override
	public String getColumnName(int index) {
		return _columnNames[index];
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

}
