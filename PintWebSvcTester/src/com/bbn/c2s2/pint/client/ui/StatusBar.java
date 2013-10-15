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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class StatusBar extends JPanel {

	// private String rnrmUrl;
	// private int inputDataSize;
	// private String message;

	private static final long serialVersionUID = -855883684755398655L;
	private JLabel lRnrmUrl;
	private JLabel lInputData;
	private JLabel lMessage;

	final int PREFERRED_HEIGHT = 20;
	final int PREFERRED_WIDTH = 100;

	public StatusBar() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(createBorder());
		lRnrmUrl = new JLabel("");
		lRnrmUrl.setMinimumSize(new Dimension(300, -1));
		lRnrmUrl.setPreferredSize(new Dimension(300, -1));
		lInputData = new JLabel("");
		lInputData.setMinimumSize(new Dimension(140, -1));
		lInputData.setPreferredSize(new Dimension(140, -1));
		lMessage = new JLabel("");
		lMessage.setMinimumSize(new Dimension(200, -1));
		lMessage.setPreferredSize(new Dimension(200, -1));

		setRnrmUrl(null);
		setInputDataSize(0);
		setMessage(null);

		// lRnrmUrl.setAlignmentX(LEFT_ALIGNMENT);
		// lInputData.setAlignmentX(CENTER_ALIGNMENT);
		// lMessage.setAlignmentX(RIGHT_ALIGNMENT);
		add(createSeparator());
		add(lRnrmUrl);
		add(Box.createHorizontalGlue());
		add(createSeparator());
		add(lInputData);
		add(Box.createHorizontalGlue());
		add(createSeparator());
		add(lMessage);
	}

	private JComponent createSeparator() {
		JPanel rv = new JPanel();
		rv.setLayout(new BoxLayout(rv, BoxLayout.LINE_AXIS));
		JPanel line = new JPanel();
		line.setBorder(BorderFactory
				.createLineBorder(SystemColor.controlDkShadow));
		Dimension d = new Dimension(1, 16);
		line.setPreferredSize(d);
		line.setMinimumSize(d);
		line.setMaximumSize(d);

		d = new Dimension(10, 0);

		rv.add(Box.createRigidArea(d));
		rv.add(line);
		rv.add(Box.createRigidArea(d));

		return rv;
	}

	private Border createBorder() {
		Border rv = BorderFactory.createMatteBorder(1, 0, 0, 0,
				SystemColor.controlDkShadow);
		return rv;
	}

	public void setRnrmUrl(String rnrmUrl) {
		if (null == rnrmUrl || rnrmUrl.length() < 1) {
			lRnrmUrl.setText("Red Nodal Reference Model not loaded");
			lRnrmUrl.setForeground(Color.RED);
			lRnrmUrl.setToolTipText("None Loaded");
		} else {
			lRnrmUrl.setText("Red Nodal Reference Model loaded");
			lRnrmUrl.setForeground(SystemColor.textText);
			lRnrmUrl.setToolTipText(rnrmUrl);
		}
	}

	public void setInputDataSize(int inputDataSize) {
		lInputData.setText(String.format("Observation Count: %1$d",
				inputDataSize));
	}

	public void setMessage(String message) {
		String msg = null;
		if (null == message || message.trim().length() < 1) {
			msg = "Idle";
		} else {
			msg = message;
		}
		lMessage.setText(String.format("Status: %1$s", msg));
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, PREFERRED_HEIGHT);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
	}

}
