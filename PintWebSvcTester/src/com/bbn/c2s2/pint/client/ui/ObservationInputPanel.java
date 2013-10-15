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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.bbn.c2s2.ConfigLoader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class ObservationInputPanel extends JPanel {
	private static final long serialVersionUID = 7155002333721768319L;
	private final int RIGID_HEIGHT = 30;
	final String[] DEFAULT_WSDLS = {
			"http://issl.bbn.com:8080/PFEB_C2S2_IF/ClientIF?wsdl",
			"http://localhost:8080/PFEB_C2S2_IF/ClientIF?wsdl", };
	final String[] DEFAULT_RNRM_URLS = { "http://localhost:8080/examples.rdf" };
	private boolean _rnrmRequired = true;

	// Data
	private List<SimpleObservation> _observations;

	// Components
	private JButton _btnExecute;
	private JTextField _tfMaxDistance;
	private JTextField _tfMaxTimespan;
	private JComboBox _cbWsdl;
	private JLabel _lWsdl;
	private ObservationTableModel _tblModel;
	private ConfigPropertiesTableModel _tblParams;
	private JTable _jtblConfigProps;
	private ActionListener _executionListener;

	public ObservationInputPanel(ActionListener listener) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		_executionListener = listener;
		initializeComponents();
	}

	private void initializeComponents() {
		setBorder(BorderFactory.createTitledBorder("Process Finder Input"));
		// ######## data loading panel
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		// p.setBorder(BorderFactory
		// .createEtchedBorder());

		// computation source
		p.add(createComputationPanel());
		p.add(Box.createHorizontalGlue());

		// algorithm configuration
		p.add(createConfigPanel());
		this.add(p);

		// ########## observation table panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));

		_tblModel = new ObservationTableModel();
		JTable obsTable = new JTable(_tblModel);
		JScrollPane scroll = new JScrollPane(obsTable);
		scroll.setPreferredSize(new Dimension(-1, 200));
		scroll.setMinimumSize(new Dimension(1000, 200));
		p.add(scroll);
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(p);

		// ########## button panel
		this.add(createButtonPanel());

		this.add(Box.createVerticalGlue());
	}

	private void refreshButtons() {
		_btnExecute.setEnabled(null != _observations
				&& _observations.size() > 0
				&& (!_rnrmRequired || null != getRnrmModel()));
		_tfMaxDistance.setEnabled(_btnExecute.isEnabled());
		_tfMaxTimespan.setEnabled(_btnExecute.isEnabled());
	}

	public void setBusyState(boolean busy) {
		if (busy) {
			_btnExecute.setText("Computing...");
			_btnExecute.setEnabled(false);
		} else {
			_btnExecute.setText("Execute");
			refreshButtons();
		}
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(Box.createHorizontalGlue());

		_tfMaxDistance = new JTextField("1000", 6);
		_tfMaxDistance.setPreferredSize(new Dimension(50, 20));
		_tfMaxDistance.setMinimumSize(new Dimension(50, 20));
		_tfMaxDistance.setMaximumSize(new Dimension(50, 20));
		JLabel l = new JLabel("Max Distance (m):");
		p.add(l);
		p.add(Box.createRigidArea(new Dimension(5, 0)));
		p.add(_tfMaxDistance);
		p.add(Box.createRigidArea(new Dimension(15, 0)));

		_tfMaxTimespan = new JTextField("315360000", 10);
		_tfMaxTimespan.setPreferredSize(new Dimension(50, 20));
		_tfMaxTimespan.setMinimumSize(new Dimension(50, 20));
		_tfMaxTimespan.setMaximumSize(new Dimension(50, 20));
		l = new JLabel("Max Timespan (s):");
		p.add(l);
		p.add(Box.createRigidArea(new Dimension(5, 0)));
		p.add(_tfMaxTimespan);

		_btnExecute = new JButton("Execute");
		_btnExecute.setActionCommand("Execute");
		_btnExecute.addActionListener(_executionListener);

		p.add(Box.createRigidArea(new Dimension(10, 0)));
		p.add(_btnExecute);

		refreshButtons();
		return p;
	}

	private JPanel createConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));

		// load the default parameters from file
		Properties props = ConfigLoader.getInstance().getPintParameters();

		_tblParams = new ConfigPropertiesTableModel(props);
		_jtblConfigProps = new JTable(_tblParams);
		_jtblConfigProps.setDefaultRenderer(Object.class,
				new PresizedTableCellRenderer());
		_jtblConfigProps.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		_jtblConfigProps.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scroll = new JScrollPane(_jtblConfigProps);
		scroll.setMinimumSize(new Dimension(300, 80));
		scroll.setPreferredSize(new Dimension(300, 80));
		scroll.setMaximumSize(new Dimension(300, 80));
		// scroll.setAlignmentX(LEFT_ALIGNMENT);
		p.add(scroll);
		p.add(Box.createRigidArea(new Dimension(200, 0)));
		p.add(Box.createHorizontalGlue());
		p.setAlignmentY(TOP_ALIGNMENT);

		return p;
	}

	public int getMaxDistanceMeters() throws NumberFormatException {
		return Integer.parseInt(_tfMaxDistance.getText());
	}

	public double getMaxTimespanSeconds() throws NumberFormatException {
		return Double.parseDouble(_tfMaxTimespan.getText());
	}

	public Properties getParameters() {
		return _tblParams.getProperties();
	}

	private JPanel createComputationPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		JRadioButton rbLocal = new JRadioButton("Local");
		rbLocal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				computationSourceChanged(true);
			}
		});
		rbLocal.setSelected(true);
		JRadioButton rbServer = new JRadioButton("PINT Server");
		rbServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				computationSourceChanged(false);
			}
		});
		ButtonGroup bgComputation = new ButtonGroup();
		bgComputation.add(rbLocal);
		bgComputation.add(rbServer);

		_cbWsdl = new JComboBox(DEFAULT_WSDLS);
		_cbWsdl.setEditable(true);
		_cbWsdl.setEnabled(false);
		_cbWsdl.setMaximumSize(new Dimension(-1, 25));

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.add(rbLocal);
		top.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		top.add(rbServer);
		top.add(Box.createHorizontalGlue());

		JPanel btm = new JPanel();
		btm.setLayout(new BoxLayout(btm, BoxLayout.LINE_AXIS));
		_lWsdl = new JLabel("WSDL: ");
		_lWsdl.setEnabled(false);
		btm.add(Box.createRigidArea(new Dimension(5, 0)));
		btm.add(_lWsdl);
		btm.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		btm.add(_cbWsdl);
		btm.add(Box.createHorizontalGlue());

		p.add(top);
		p.add(btm);
		p.setAlignmentY(TOP_ALIGNMENT);

		return p;
	}

	public boolean loadRnrm(String url) {
		boolean rv = false;
		Model m = ModelFactory.createDefaultModel();
		try {
			m.read(url);
		} catch (Exception e) {
			MainFrame.showError(this, String
					.format("Error reading model from %1$s.%n%2$s", url, e
							.getMessage()));
			rv = false;
		}
		if (null != m) {
			_tblModel.setRnrmModel(m);
			rv = (m.size() > 0);
		}
		refreshButtons();
		return rv;
	}

	private void computationSourceChanged(boolean local) {
		_rnrmRequired = local;
		_cbWsdl.setEnabled(!local);
		_lWsdl.setEnabled(!local);
		_jtblConfigProps.setEnabled(local);
		refreshButtons();
	}

	public void setObservations(List<SimpleObservation> observations) {
		_observations = observations;
		_tblModel.setObservations(observations);
		refreshButtons();
	}

	public Model getRnrmModel() {
		return _tblModel.getRnrmModel();
	}

	public List<SimpleObservation> getObservations() {
		return _observations;
	}

	public IProcessFinderClient getProcessFinderClient() {
		IProcessFinderClient rv = null;
		if (_lWsdl.isEnabled()) {
			try {
				rv = new PintServerClient(_cbWsdl.getSelectedItem().toString());
			} catch (Exception e) {
				MainFrame
						.showError(
								this,
								String
										.format(
												"Error occurred while establishing connection to web service.%n%1$s",
												e.getMessage()));
			}
		} else {
			rv = new LocalClient(getRnrmModel());
		}
		return rv;
	}

	public String getWsdlUrl() {
		return _cbWsdl.getSelectedItem().toString();
	}
}
