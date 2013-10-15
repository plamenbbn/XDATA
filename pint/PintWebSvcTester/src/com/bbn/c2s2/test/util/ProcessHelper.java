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

package com.bbn.c2s2.test.util;

import java.util.Map;
import java.util.Set;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.rdf.RDFHelper;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author tself
 *
 */
public class ProcessHelper {

	public static String toString(Map<Activity, Set<Activity>> map, Model model) {
		String rv = "";
		for(Activity key : map.keySet()) {
			String line = RDFHelper.getLabel(model, key.getActivityURI()) + " [";
			for(Activity act : map.get(key)) {
				line += RDFHelper.getLabel(model, act.getActivityURI()) + ", ";
			}
			line += "]";
			rv += String.format("%1$s%n", line);
		}
		return rv;
	}
}
