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

import java.awt.Color;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.bbn.c2s2.pint.dataGen.DataGenConfig;

/**
 * GUI for viewint/setting data generation config parameters in a
 * {@link DataGenConfig} object
 * 
 * @author tself
 * 
 */
public class DataGenPanel extends JPanel {
	private static final long serialVersionUID = -8663249168106093896L;
	private final int RIGID_HEIGHT = 30;
	
	// components
	private JLabel lEndDate;
	private JLabel lTruthDuration;
	private JLabel lLat;
	private JLabel lLon;
	private JLabel lDataDiameter;
	private JLabel lTruthDiameter;
	private JLabel lSignalNoiseRatio;
//	private JLabel lNumTruths;
	
	private JSpinner spinRandomSeed;
	private JSpinner spinStartDate;
	private JSpinner spinEndDate;
	private JTextField tfLat;
	private JTextField tfLon;
	private JTextField tfDataDiameter;
	private JTextField tfTruthDiameter;
	private JTextField tfSignalNoiseRatio;
	private JTextField tfTruthDurationDays;
//	private JSpinner spinNumTruths;
	private JSpinner spinPercentVisible;
	
	private Color normalColor;
	
	public DataGenPanel(DataGenConfig config) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		initializeComponents(config);
	}

	private void initializeComponents(DataGenConfig config) {
		// date range
		JPanel outer = new JPanel();
		outer.setLayout(new BoxLayout(outer, BoxLayout.PAGE_AXIS));
		outer.setBorder(BorderFactory.createTitledBorder("Date Range"));
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		JLabel lStart = new JLabel("Start");
		p.add(lStart);
		normalColor = lStart.getForeground();
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		spinStartDate = createDateSpinner(config.getStartDate());
		p.add(spinStartDate);
		p.add(Box.createRigidArea(new Dimension(20, RIGID_HEIGHT)));
		lEndDate = new JLabel("End");
		p.add(lEndDate);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		spinEndDate = createDateSpinner(config.getEndDate());
		p.add(spinEndDate);
		p.add(Box.createHorizontalGlue());
		
		outer.add(p);
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		lTruthDuration = new JLabel("Truth Duration (days)");
		p.add(lTruthDuration);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfTruthDurationDays = new JTextField(3);
		tfTruthDurationDays.setText(Double.toString(config.getTruthDurationDays()));
		p.add(tfTruthDurationDays);
		p.add(Box.createHorizontalGlue());
		
		outer.add(p);
		
		this.add(outer);
		this.add(Box.createRigidArea(new Dimension(0, 10)));

		// center point
		p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Geospatial Center Point"));
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		lLat = new JLabel("Latitude (dd)");
		p.add(lLat);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfLat = new JTextField(8);
		tfLat.setText(Double.toString(config.getLatitude()));
		p.add(tfLat);
		p.add(Box.createRigidArea(new Dimension(20, RIGID_HEIGHT)));
		lLon = new JLabel("Longitude (dd)");
		p.add(lLon);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfLon = new JTextField(8);
		tfLon.setText(Double.toString(config.getLongitude()));
		p.add(tfLon);
		p.add(Box.createHorizontalGlue());

		this.add(p);
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// diameter
		p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Geospatial Diameter"));
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		lDataDiameter = new JLabel("All Observations (km)");
		p.add(lDataDiameter);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfDataDiameter = new JTextField(5);
		tfDataDiameter.setText(Double.toString(config.getDataRadiusKm() * 2.0f));
		p.add(tfDataDiameter);
		p.add(Box.createRigidArea(new Dimension(20, RIGID_HEIGHT)));
		lTruthDiameter = new JLabel("Truth Observations (km)");
		p.add(lTruthDiameter);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfTruthDiameter = new JTextField(5);
		tfTruthDiameter.setText(Double.toString(config.getTruthRadiusKm() * 2.0f));
		p.add(tfTruthDiameter);
		p.add(Box.createHorizontalGlue());

		this.add(p);
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// data size
		outer = new JPanel();
		outer.setBorder(BorderFactory.createTitledBorder("Data Size"));
		outer.setLayout(new BoxLayout(outer, BoxLayout.PAGE_AXIS));
//		p = new JPanel();
//		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
//		lNumTruths = new JLabel("Number of Truth Sets");
//		p.add(lNumTruths);
//		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
//		spinNumTruths = createIntegerSpinner(1, 0, 1000);
//		p.add(spinNumTruths);
//		p.add(Box.createHorizontalGlue());
//		
//		outer.add(p);
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		lSignalNoiseRatio = new JLabel("Signal/Noise (Truth/Distractors)");
		p.add(lSignalNoiseRatio);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		tfSignalNoiseRatio = new JTextField(3);
		tfSignalNoiseRatio.setText(Double.toString(config.getSignalNoiseRatio()));
		p.add(tfSignalNoiseRatio);
		p.add(Box.createHorizontalGlue());

		outer.add(p);
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		JLabel l = new JLabel("Visibility (%)");
		p.add(l);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		spinPercentVisible = createIntegerSpinner(100, 10, 100);
		p.add(spinPercentVisible);
		p.add(Box.createHorizontalGlue());
		
		outer.add(p);

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		l = new JLabel("Random Seed for Data Generation");
		p.add(l);
		p.add(Box.createRigidArea(new Dimension(10, RIGID_HEIGHT)));
		spinRandomSeed = createIntegerSpinner((int)config.getRandomSeed(), 1, Integer.MAX_VALUE);
		p.add(spinRandomSeed);
		p.add(Box.createHorizontalGlue());
		outer.add(p);

		this.add(outer);
		this.add(Box.createRigidArea(new Dimension(0, 10)));
	}

	private JSpinner createDateSpinner(Date time) {
		JSpinner rv = null;
		SpinnerModel model = new SpinnerDateModel(time, null, null, Calendar.MONTH);
		rv = new JSpinner(model);
		rv.setEditor(new JSpinner.DateEditor(rv, "MM/dd/yyyy"));


		return rv;
	}
	
	private JSpinner createIntegerSpinner(int current, int min, int max) {
		JSpinner rv = null;
		SpinnerModel model = new SpinnerNumberModel(current, min, max, 1);
		rv = new JSpinner(model);
		return rv;
	}
	
	public boolean validateValues() {
		boolean rv = true;
		// end must >= start
		Date start = (Date)spinStartDate.getValue();
		Date end = (Date)spinEndDate.getValue();
		if(start.compareTo(end) > 0) {
			setRed(lEndDate);
			rv = false;
		} else {
			setNormal(lEndDate);
		}
		
		boolean caught = false;
		// valid doubles
		try {
			Double.parseDouble(tfLat.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
		}
		if(caught) {
			setRed(lLat);
			rv = false;
		} else {
			setNormal(lLat);
		}
		
		caught = false;
		try {
			Double.parseDouble(tfLon.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
			setRed(lLon);
			rv = false;
		}
		if(!caught) {
			setNormal(lLon);
		}
		
		caught = false;
		try {
			Double.parseDouble(tfDataDiameter.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
			setRed(lDataDiameter);
			rv = false;
		}
		if(!caught) {
			setNormal(lDataDiameter);
		}
		
		caught = false;
		try {
			Double.parseDouble(tfTruthDiameter.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
			setRed(lTruthDiameter);
			rv = false;
		}		
		if(!caught) {
			setNormal(lTruthDiameter);
		}
		
		caught = false;
		try {
			Double.parseDouble(tfTruthDurationDays.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
			setRed(lTruthDuration);
			rv = false;
		}
		if(!caught) {
			setNormal(lTruthDuration);
		}
		
		caught = false;
		try {
			Double.parseDouble(tfSignalNoiseRatio.getText());
		} catch (NumberFormatException nfe) {
			caught = true;
			setRed(lSignalNoiseRatio);
			rv = false;
		}
		if(!caught) {
			setNormal(lSignalNoiseRatio);
		}

		return rv;
	}
	
	private void setRed(JLabel l) {
		l.setForeground(Color.RED);
	}
	
	private void setNormal(JLabel l) {
		l.setForeground(normalColor);
	}
	
	public DataGenConfig getConfig() {
		DataGenConfig rv = new DataGenConfig();
		rv.setDataRadiusKm(Double.parseDouble(tfDataDiameter.getText()) / 2.0f);
		rv.setTruthRadiusKm(Double.parseDouble(tfTruthDiameter.getText()) / 2.0f);
		rv.setEndDate((Date)spinEndDate.getValue());
		rv.setLatitude(Double.parseDouble(tfLat.getText()));
		rv.setLongitude(Double.parseDouble(tfLon.getText()));
//		rv.setNumTruths(((Integer)spinNumTruths.getValue()).intValue());
		rv.setPercentVisible(((Integer)spinPercentVisible.getValue()).intValue() / 100.0f);
		rv.setSignalNoiseRatio(Double.parseDouble(tfSignalNoiseRatio.getText()));
		rv.setStartDate((Date)spinStartDate.getValue());
		rv.setTruthDurationDays(Double.parseDouble(tfTruthDurationDays.getText()));
		return rv;
	}
}
