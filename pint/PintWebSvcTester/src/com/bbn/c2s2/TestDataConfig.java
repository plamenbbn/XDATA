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

import java.util.Properties;

public class TestDataConfig {
	
	public static Properties getProperties() {
		Properties p = new Properties();
		p.setProperty("pf.clusterer.agreement-threshold", "0.4");
		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
		p.setProperty("process.max.distance.km", "2.3");
		return p;
	}

}
