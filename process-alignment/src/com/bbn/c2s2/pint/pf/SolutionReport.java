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
package com.bbn.c2s2.pint.pf;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.ISolutionReport;

/**
 * This class provides all of the information for a particular solution,
 * including the score, list of ordered activities, bindings, and process No
 * internal cache data is included in this object, so it can be considered a
 * lightweight object suitable for transmission.
 * 
 * @author reblace
 * 
 */
public class SolutionReport implements Serializable, Comparable<ISolutionReport>, ISolutionReport {

	private static final long serialVersionUID = 1223182083892462430L;
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	private double solutionScore = -1.0;
	private List<Activity> orderedActivities;
	private Map<Integer, IBinding> bindings;
	private String processLabel;
	private String processUri;

	/**
	 * Creates a new solution report copying from an {@link EvaluatedSolution}.
	 * All of the significant data structures are copied over.
	 * 
	 * @param solution
	 *            The {@link EvaluatedSolution} to copy
	 */
	public SolutionReport(EvaluatedSolution solution) {

		bindings = convertBindingMap(solution.getActivityToBindingMap());
		solutionScore = solution.getScore();
		processLabel = solution.getProcess().getLabel();
		processUri = solution.getProcess().getProcessUri();
		orderedActivities = solution.getProcess().getOrderedActivities();
	}
	
	private Map<Integer, IBinding> convertBindingMap(Map<Integer, Binding> map) {
		Map<Integer, IBinding> rv = new HashMap<Integer, IBinding>(map.size());
		for(Integer i : map.keySet()) {
			rv.put(i, (IBinding)map.get(i));
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.ISolutionReport#getOrderedBindings()
	 */
	public List<IBinding> getOrderedBindings() {
		List<IBinding> out = new ArrayList<IBinding>(this.bindings.size());
		for (Activity a : this.orderedActivities) {
			out.add(this.bindings.get(a.getID()));
		}
		return out;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.ISolutionReport#getSolutionScore()
	 */
	public double getSolutionScore() {
		return solutionScore;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.ISolutionReport#getBindings()
	 */
	public Collection<IBinding> getBindings() {
		return bindings.values();
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.ISolutionReport#getProcessLabel()
	 */
	public String getProcessLabel() {
		return (null == processLabel) ? "" : processLabel;
	}

	/* (non-Javadoc)
	 * @see com.bbn.c2s2.pint.pf.ISolutionReport#getProcessUri()
	 */
	public String getProcessUri() {
		return processUri;
	}

	@Override
	public String toString() {
		String endl = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("   Detected Process: " + processLabel + endl);
		sb.append("   [" + processUri + "]" + endl);
		sb.append("   Score: " + solutionScore + endl);
		String format = "|    %1$-30s|   %2$-40s|    %3$-30s|    %4$-30s|%n";
		sb.append(String
				.format(format, "----ACTIVITY----", "----OBSERVATION----",
						"----TIME STAMP----", "----LOCATION----"));
		for (Activity act : this.orderedActivities) {
			IBinding tca = bindings.get(act.getID());
			String actName = act.getLabel();
			String obsLabel = (tca.getObservation() != null) ? tca
					.getObservation().getLabel() : "UNBOUND";
			String timeString = (tca.getObservation() != null) ? DATE_FORMAT
					.format(tca.getObservation().getTimestamp()) : "";
			String locString = (tca.getObservation() != null) ? String.format(
					"%1$2.6f, %2$2.6f", tca.getObservation().getLat(), tca
							.getObservation().getLon()) : "";
			sb.append(String.format(format, actName, obsLabel, timeString,
					locString));
		}
		return sb.toString();
	}

	@Override
	public int compareTo(ISolutionReport arg) {
		if (solutionScore > arg.getSolutionScore()) {
			return -1;
		}
		if (solutionScore < arg.getSolutionScore()) {
			return 1;
		}
		return 0;
	}
}
