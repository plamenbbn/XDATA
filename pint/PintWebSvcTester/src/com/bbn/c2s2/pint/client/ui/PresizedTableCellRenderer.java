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

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class PresizedTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 2802698774542878171L;

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Component c = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);

		if (c instanceof JLabel) {
			JLabel l = (JLabel) c;
			Insets insets = l.getInsets();
			int w = l.getPreferredSize().width + insets.left + insets.right;
			TableColumn col = table.getColumnModel().getColumn(column);
			col.setPreferredWidth(Math.max(col.getPreferredWidth(), w));
		}
		return c;
	}
}
