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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

public class ConfigPropertiesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6015822808761387527L;
	// Property | Value
	private final String[] _columnNames = { "Parameter", "Value" };
	private List<List<String>> _params;

	public ConfigPropertiesTableModel(Properties props) {
		_params = new ArrayList<List<String>>(props.size());
		Iterator<Object> it = props.keySet().iterator();
		while (it.hasNext()) {
			ArrayList<String> prop = new ArrayList<String>(2);
			String key = it.next().toString();
			if(!key.startsWith("pf")) {
				continue;
			}
			String val = props.getProperty(key);
			prop.add(key);
			prop.add(val);
			_params.add(prop);
		}
		Collections.sort(_params, new Comparator<List<String>>() {
			public int compare(List<String> list1, List<String> list2) {
				return list1.get(0).compareTo(list2.get(0));
			}
		});
	}

	public Properties getProperties() {
		Properties rv = new Properties();
		for (List<String> prop : _params) {
			rv.put(prop.get(0), prop.get(1));
		}
		return rv;
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	@Override
	public int getRowCount() {
		return _params.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		List<String> prop = _params.get(row);
		return prop.get(col);
	}

	@Override
	public String getColumnName(int column) {
		return _columnNames[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex > 0) {
			List<String> prop = _params.get(rowIndex);
			prop.remove(1);
			prop.add(aValue.toString());
		}
	}
}
