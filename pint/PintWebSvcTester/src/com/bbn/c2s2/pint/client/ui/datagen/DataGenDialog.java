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

package com.bbn.c2s2.pint.client.ui.datagen;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.c2s2.pint.dataGen.DataGenConfig;
import com.bbn.c2s2.pint.rdf.RdfProcess;
import com.bbn.c2s2.pint.rdf.RnrmWrapper;

/**
 * Dialog window for setting parameters before generating test data
 * 
 * @author tself
 * 
 */
public class DataGenDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7924330248668977308L;

	private boolean doGenerate = false;
	private RnrmWrapper rnrm;
	private DataGenConfig dataGenConfig;
	private String processUri;

	// components
	JComboBox cbProcesses;
	DataGenPanel pDataGen;
	JButton btnGenerate;
	JButton btnCancel;

	public DataGenDialog(JFrame parent, RnrmWrapper rnrm, DataGenConfig config, String processUri) {
		super(parent, "Observation Generator", true);
		this.processUri = processUri;
		this.dataGenConfig = config;
		this.rnrm = rnrm;
		initializeComponents();
		pack();
		setResizable(false);
		setVisible(true);
	}

	private void initializeComponents() {
		JPanel mainPanel = new JPanel();
		getContentPane().add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// processes
		mainPanel.add(createProcessPanel());

		// datagen config
		pDataGen = new DataGenPanel(dataGenConfig);
		mainPanel.add(pDataGen);

		// buttons
		mainPanel.add(createButtonPanel());
	}

	private JPanel createProcessPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		JLabel l = new JLabel("Process");
		p.add(l);
		p.add(Box.createRigidArea(new Dimension(10, 30)));

		// processes
		ArrayList<ProcessItem> items = new ArrayList<ProcessItem>();
		Collection<RdfProcess> rdfProcesses = rnrm.getRdfProcesses();
		for (RdfProcess rdfProc : rdfProcesses) {
			if (rdfProc.isValid()) {
				items.add(new ProcessItem(rdfProc.getProcessUri(), rdfProc
						.getLabel()));
			}
		}
		Collections.sort(items, new Comparator<ProcessItem>() {

			@Override
			public int compare(ProcessItem a, ProcessItem b) {
				return a.label.compareTo(b.label);
			}
		});

		Object[] procItems = items.toArray();
		cbProcesses = new JComboBox(procItems);
		if(null != processUri && processUri.length() > 0) {
			for(int i = 0; i < items.size(); i++) {
				if(items.get(i).uri.equals(processUri)) {
					cbProcesses.setSelectedIndex(i);
					break;
				}
			}
		}

		p.add(cbProcesses);
		p.add(Box.createHorizontalGlue());
		return p;
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(Box.createHorizontalGlue());

		btnGenerate = new JButton("Generate Observations");
		btnGenerate.setActionCommand("Generate");
		btnGenerate.addActionListener(this);
		p.add(btnGenerate);

		btnCancel = new JButton("Cancel");
		btnCancel.setActionCommand("Cancel");
		btnCancel.addActionListener(this);
		p.add(Box.createRigidArea(new Dimension(10, 0)));
		p.add(btnCancel);
		return p;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Generate")) {
			boolean valid = pDataGen.validateValues();
			if (valid) {
				doGenerate = true;
				dataGenConfig = pDataGen.getConfig();
				processUri = ((ProcessItem) cbProcesses.getSelectedItem()).uri;
				setVisible(false);
			}
		} else {
			doGenerate = false;
			setVisible(false);
		}
	}

	public boolean doGenerate() {
		return doGenerate;
	}

	public DataGenConfig getDataGenConfig() {
		return dataGenConfig;
	}

	public String getProcessUri() {
		return processUri;
	}

	class ProcessItem {
		private String uri;
		private String label;

		ProcessItem(String uri, String label) {
			this.uri = uri;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
