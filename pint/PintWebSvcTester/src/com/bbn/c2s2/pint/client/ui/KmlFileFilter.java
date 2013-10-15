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

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class KmlFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		return file.isDirectory()
				|| getExtension(file).equalsIgnoreCase("kml");
	}

	@Override
	public String getDescription() {
		return "Google Earth (*.kml)";
	}

	public static String getExtension(File f) {
		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
