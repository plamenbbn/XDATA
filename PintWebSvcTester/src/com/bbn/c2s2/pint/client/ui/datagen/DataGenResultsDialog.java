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
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.RnrmProcessFactory;
import com.bbn.c2s2.pint.vocab.RNRM;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author tself
 * 
 */
public class DataGenResultsDialog extends JDialog {
	private static final long serialVersionUID = -8613873199349124948L;

	JTextArea taActivities;

	public DataGenResultsDialog(JFrame parent, DataGenResult result) {
		super(parent, "Observation Generation Results", true);
		initializeComponents(result.getObservations().size());
		showActivities(result.getTruthModel());
		pack();
		setVisible(true);
	}

	private void initializeComponents(int obsSize) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		getContentPane().add(p);

		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
		p1.add(Box.createRigidArea(new Dimension(5, 30)));

		JLabel l = new JLabel(String.format(
				"%1$d Observations generated for the following Activities:",
				obsSize));
		p1.add(l);
		p1.add(Box.createRigidArea(new Dimension(20, 30)));
		p1.add(Box.createHorizontalGlue());
		p.add(p1);

		p.add(Box.createRigidArea(new Dimension(1, 10)));
		taActivities = new JTextArea();
		JScrollPane scroll = new JScrollPane(taActivities);
		scroll.setPreferredSize(new Dimension(-1, 400));
		scroll.setMinimumSize(new Dimension(300, 400));
		p.add(scroll);
		
		JButton btn = new JButton("Close");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		});
		p.add(btn);
	}

	private void showActivities(Model truthModel) {
		StmtIterator it = truthModel.listStatements((Resource) null, RDF.type,
				RNRM.Process);
		Resource proc = null;
		while (it.hasNext()) {
			proc = it.nextStatement().getSubject();
			break;
		}
		RnrmProcess rnrmProc = null;
		try {
			rnrmProc = RnrmProcessFactory.createProcess(truthModel, proc
					.getURI());
		} catch (Exception e) {
			// meh. Guess we won't display anything
		}
		if (null != rnrmProc) {
			List<Activity> orderedActs = rnrmProc.getOrderedActivities();
			String activityString = "";
			for (Activity a : orderedActs) {
				activityString += String.format("%1$s%n", a.getLabel());
			}
			taActivities.setText(activityString);
		}
	}
}
