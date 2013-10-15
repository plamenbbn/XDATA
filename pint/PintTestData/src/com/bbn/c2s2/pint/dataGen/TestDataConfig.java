/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
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
package com.bbn.c2s2.pint.dataGen;

import java.util.Properties;

public class TestDataConfig {
	
	public static Properties getProperties() {
		Properties p = new Properties();
		p.setProperty("pf.clusterer.agreement-threshold", "0.4");
		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
		p.setProperty("process.max.distance.km", "1.3");
		p.setProperty("process.max.timespan.ms", "2.3");
		return p;
	}

}
