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

import java.awt.Dimension;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.bbn.c2s2.util.Constants;
import com.bbn.c2s2.util.ObservationLoader;
import com.hp.hpl.jena.rdf.model.Model;

import edu.jhuapl.c2s2.processfinderenterprisebus.DetectedProcess;
import edu.jhuapl.c2s2.processfinderenterprisebus.ObservationToActivityMapping;

public class ResultsPanel extends JPanel {
	private static final long serialVersionUID = 1115124904413317852L;
	private DefaultListModel _resListModel;
	private Model _rnrm;
	private ObservationIndex _obsIndex;
	private MappingTableModel _tblMappingModel;

	// components
	private JTextField _tfScore;
	private JTextField _tfMaxDistance;
	private JTextField _tfEarliest;
	private JTextField _tfLatest;
	private JTextField _tfMaxTimespan;

	public ResultsPanel() {
		super();
		initializeComponents();
	}

	public void setObservationIndex(ObservationIndex obsIndex) {
		_obsIndex = obsIndex;
		_tblMappingModel.setObservationIndex(obsIndex);
	}

	public void setModel(Model rnrm) {
		_rnrm = rnrm;
		// update the results table
		_tblMappingModel.setModel(rnrm);
		// update each of the DPListItems
		List<DPListItem> items = new ArrayList<DPListItem>(_resListModel.size());
		for (int i = 0; i < _resListModel.size(); i++) {
			items.add((DPListItem) _resListModel.get(i));
		}
		_resListModel.clear();
		for (DPListItem item : items) {
			item.setModel(rnrm);
			_resListModel.addElement(item);
		}
	}

	public void clearResults() {
		// clear mapping table
		_tblMappingModel.setDetectedProcess(null);

		// clear list of results
		_resListModel.clear();

		// clear eval score
		_tfScore.setText("");
		_tfEarliest.setText("");
		_tfLatest.setText("");
		_tfMaxDistance.setText("");
		_tfMaxTimespan.setText("");

	}

	private void initializeComponents() {
		setBorder(BorderFactory.createTitledBorder("Results"));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// #### Result list
		add(createResultListPanel());

		// #### DetectedProcess
		add(createDetectedProcessPanel());
	}

	private JPanel createResultListPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		// _resListModel = new ResultsListModel(rnrm);
		_resListModel = new DefaultListModel();
		JList list = new JList(_resListModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				if (!lse.getValueIsAdjusting()) {
					JList src = (JList) lse.getSource();
					if (null != src) {
						DPListItem item = (DPListItem) (src.getSelectedValue());
						if (null != item) {
							setSelectedProcess(item.getDetectedProcess());
						}
					}
				}
			}
		});
		JScrollPane scroll = new JScrollPane(list);
		scroll.setMinimumSize(new Dimension(150, 200));
		scroll.setPreferredSize(new Dimension(150, 200));
		scroll.setMaximumSize(new Dimension(150, Integer.MAX_VALUE));
		p.add(scroll);
		return p;
	}

	private String secondsToDateString(double seconds) {
		String rv = "";
		Date d = new Date((long) seconds * 1000);
		rv = Constants.DATE_FORMAT_SHORT.format(d);
		return rv;
	}

	private String secondsToElapsedTime(double seconds) {
		int hours = (int) Math.floor(seconds / 3600);
		double rem = seconds - (hours * 3600);
		int minutes = (int) Math.floor(rem / 60);
		rem = rem - (minutes * 60);
		return String.format("%1$2d:%2$2d:%3$2.0f", hours, minutes, rem);
	}

	private void setSelectedProcess(DetectedProcess proc) {
		// update evaluation score
		_tfScore.setText(String.format("%1$1.4f", proc.getScore()));
		_tfEarliest.setText(secondsToDateString(proc
				.getEarliestObservationTimeStamp()));
		_tfLatest.setText(secondsToDateString(proc
				.getLatestObservationTimeStamp()));
		_tfMaxDistance.setText(String.format("%1$1.4f", proc
				.getDetectedProcessMaximumDistanceInMeters()));
		_tfMaxTimespan.setText(secondsToElapsedTime(proc
				.getDetectedProcessTimeSpanInSeconds()));

		// TODO: update result properties

		// update mappings table
		_tblMappingModel.setDetectedProcess(proc);
	}

	private JPanel createDetectedProcessPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// #### score and properties - top row
		JPanel pScoreProps = new JPanel();
		pScoreProps.setLayout(new BoxLayout(pScoreProps, BoxLayout.LINE_AXIS));
		pScoreProps.setAlignmentY(TOP_ALIGNMENT);

		// Score field
		JPanel pField = new JPanel();
		pField.setAlignmentY(TOP_ALIGNMENT);
		pField.setLayout(new BoxLayout(pField, BoxLayout.LINE_AXIS));
		JLabel lField = new JLabel("Evalution Score:");
		_tfScore = new JTextField(8);
		Dimension d = new Dimension(50, 20);
		_tfScore.setPreferredSize(d);
		_tfScore.setMaximumSize(d);
		_tfScore.setMinimumSize(d);
		_tfScore.setEditable(false);
		pField.add(lField);
		pField.add(Box.createRigidArea(new Dimension(10, 0)));
		pField.add(_tfScore);
		pScoreProps.add(pField);
		pScoreProps.add(Box.createRigidArea(new Dimension(10, 0)));

		// Max Distance field
		pField = new JPanel();
		pField.setAlignmentY(TOP_ALIGNMENT);
		pField.setLayout(new BoxLayout(pField, BoxLayout.LINE_AXIS));
		lField = new JLabel("Max Distance (m):");
		_tfMaxDistance = new JTextField(8);
		d = new Dimension(70, 20);
		_tfMaxDistance.setPreferredSize(d);
		_tfMaxDistance.setMaximumSize(d);
		_tfMaxDistance.setMinimumSize(d);
		_tfMaxDistance.setEditable(false);
		pField.add(lField);
		pField.add(Box.createRigidArea(new Dimension(10, 0)));
		pField.add(_tfMaxDistance);
		pScoreProps.add(pField);
		pScoreProps.add(Box.createRigidArea(new Dimension(10, 0)));

		// Max Time field
		pField = new JPanel();
		pField.setAlignmentY(TOP_ALIGNMENT);
		pField.setLayout(new BoxLayout(pField, BoxLayout.LINE_AXIS));
		lField = new JLabel("Max Timespan (s):");
		_tfMaxTimespan = new JTextField(12);
		d = new Dimension(80, 20);
		_tfMaxTimespan.setPreferredSize(d);
		_tfMaxTimespan.setMaximumSize(d);
		_tfMaxTimespan.setMinimumSize(d);
		_tfMaxTimespan.setEditable(false);
		pField.add(lField);
		pField.add(Box.createRigidArea(new Dimension(10, 0)));
		pField.add(_tfMaxTimespan);
		pScoreProps.add(pField);

		pScoreProps.add(Box.createHorizontalGlue());
		p.add(pScoreProps);

		p.add(Box.createRigidArea(new Dimension(0, 5)));

		// #### score and properties - 2nd row
		pScoreProps = new JPanel();
		pScoreProps.setLayout(new BoxLayout(pScoreProps, BoxLayout.LINE_AXIS));
		pScoreProps.setAlignmentY(TOP_ALIGNMENT);

		// Earliest time field
		pField = new JPanel();
		pField.setAlignmentY(TOP_ALIGNMENT);
		pField.setLayout(new BoxLayout(pField, BoxLayout.LINE_AXIS));
		lField = new JLabel("Earliest Time (s):");
		_tfEarliest = new JTextField(16);
		d = new Dimension(100, 20);
		_tfEarliest.setPreferredSize(d);
		_tfEarliest.setMaximumSize(d);
		_tfEarliest.setMinimumSize(d);
		_tfEarliest.setEditable(false);
		pField.add(lField);
		pField.add(Box.createRigidArea(new Dimension(10, 0)));
		pField.add(_tfEarliest);
		pScoreProps.add(pField);
		pScoreProps.add(Box.createRigidArea(new Dimension(10, 0)));

		// Latest time field
		pField = new JPanel();
		pField.setAlignmentY(TOP_ALIGNMENT);
		pField.setLayout(new BoxLayout(pField, BoxLayout.LINE_AXIS));
		lField = new JLabel("Latest Time (s):");
		_tfLatest = new JTextField(16);
		d = new Dimension(100, 20);
		_tfLatest.setPreferredSize(d);
		_tfLatest.setMaximumSize(d);
		_tfLatest.setMinimumSize(d);
		_tfLatest.setEditable(false);
		pField.add(lField);
		pField.add(Box.createRigidArea(new Dimension(10, 0)));
		pField.add(_tfLatest);
		pScoreProps.add(pField);

		pScoreProps.add(Box.createHorizontalGlue());
		p.add(pScoreProps);
		// #### mapping table
		JPanel pBindings = new JPanel();
		pBindings.setLayout(new BoxLayout(pBindings, BoxLayout.LINE_AXIS));

		_tblMappingModel = new MappingTableModel(null, _obsIndex, _rnrm);
		JTable mapTable = new JTable(_tblMappingModel);
		JScrollPane scroll = new JScrollPane(mapTable);
		scroll.setPreferredSize(new Dimension(-1, 200));
		scroll.setMinimumSize(scroll.getPreferredSize());
		pBindings.add(scroll);
		pBindings.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		p.add(pBindings);

		return p;
	}

	public void setResults(List<DetectedProcess> processes) {
		_resListModel.clear();
		for (DetectedProcess proc : processes) {
			DPListItem item = new DPListItem(proc, _rnrm);
			_resListModel.addElement(item);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(500, 400);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 400);
	}

	class DPListItem {
		private DetectedProcess _dp;
		private Model _rnrm;

		DPListItem(DetectedProcess proc) {
			this(proc, null);
		}

		DPListItem(DetectedProcess proc, Model rnrm) {
			_dp = proc;
			_rnrm = rnrm;
		}

		@Override
		public String toString() {
			String rv = _dp.getProcessIdUri();
			if (null != _rnrm) {
				rv = RDFHelper.getLabel(_rnrm, rv);
			}
			return rv;
		}

		public DetectedProcess getDetectedProcess() {
			return _dp;
		}

		public void setModel(Model rnrm) {
			_rnrm = rnrm;
		}
	}

	class MappingTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7160317490178615265L;
		final String[] _colNames = { "Activity", "Observation", "Time",
				"Latitude", "Longitude" };
		private DetectedProcess _dp;
		private Model _rnrm;
		private ObservationIndex _obsIndex;

		MappingTableModel(DetectedProcess dp, ObservationIndex obsIndex,
				Model rnrm) {
			_dp = dp;
			_rnrm = rnrm;
			_obsIndex = obsIndex;
		}

		public void setObservationIndex(ObservationIndex obsIndex) {
			_obsIndex = obsIndex;
		}

		public void setModel(Model rnrm) {
			_rnrm = rnrm;
			fireTableDataChanged();
		}

		public void setDetectedProcess(DetectedProcess dp) {
			_dp = dp;
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column) {
			return _colNames[column];
		}

		@Override
		public int getColumnCount() {
			return _colNames.length;
		}

		@Override
		public int getRowCount() {
			int rv = 0;
			if (null != _dp) {
				return _dp.getDetectedActivities().size();
			}
			return rv;
		}

		@Override
		public Object getValueAt(int row, int col) {
			// TODO: handle case where the _obsIndex is empty because a new
			// empty set of results has been added
			Object rv = null;
			ObservationToActivityMapping binding = _dp.getDetectedActivities()
					.get(row);
			boolean unbound = (null == binding.getObservationUri());
			switch (col) {
			case 0:
				rv = RDFHelper.getLabel(_rnrm, binding.getActivityUri());
				break;
			case 1:
				rv = (unbound) ? " ---UNBOUND--- " : ObservationLoader
						.getLabel(binding.getObservationUri());
				break;
			case 2:
				rv = (unbound) ? "     -----     " : _obsIndex.getObservation(
						binding.getObservationUri()).getObservationTime()
						.toXMLFormat();
				break;
			case 3:
				rv = (unbound) ? "     -----     " : _obsIndex.getObservation(
						binding.getObservationUri()).getLocation()
						.getLatitude();
				break;
			case 4:
				rv = (unbound) ? "     -----     " : _obsIndex.getObservation(
						binding.getObservationUri()).getLocation()
						.getLongitude();
				break;
			default:
				rv = "UNKNOWN COLUMN";
			}
			return rv;
		}
	}
}
