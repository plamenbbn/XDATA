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
package com.bbn.c2s2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
	private static ConfigLoader _instance;
	private Properties _props;
	
	private ConfigLoader() {}
	
	public static ConfigLoader getInstance() {
		if(null == _instance) {
			_instance = new ConfigLoader();
			_instance.loadPintParameters();
		}
		return _instance;
	}
	
	public Properties getPintParameters() {
		return _props;
	}
	
	private void loadPintParameters() {
		// load the default parameters from file
		Properties props = new Properties();
		InputStream defaultPropertyStream = null;
		// is the file location in the property, pint.properties?
		String fileLocation = System.getProperty("pint.properties");
		// if not, try finding it on the classpath
		if (null == fileLocation) {
			defaultPropertyStream = getClass().getClassLoader()
					.getResourceAsStream("pint.properties");
		} else {
			try {
				defaultPropertyStream = new FileInputStream(fileLocation);
			} catch (IOException ioe) {
				System.err.println("Cannot find pint.properties file at "
						+ fileLocation);
				System.err.println(ioe.getStackTrace());
			}
		}
		if (null != defaultPropertyStream) {
			try {
				props.load(defaultPropertyStream);
				defaultPropertyStream.close();
			} catch (IOException ioe) {
				System.err.println("Error while loading pint.properties.");
				System.err.println(ioe.getStackTrace());
			}
		} else { // specify defaults
			Properties p = new Properties();
			p.setProperty("pf.clusterer.agreement-threshold", "0.4");

			p.setProperty("pf.generators.hconsistent.num-solutions", "20");
			p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
			p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");

			p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");

			p.setProperty("pf.generators.gphconsistent.num-solutions", "1");
			p.setProperty("pf.generators.gphconsistent.hcon-threshold", "0.0");
			p.setProperty("pf.generators.gphconsistent.spatial-weight", "1.0");

			props = p;
		}
		_props = props;
	}
}
