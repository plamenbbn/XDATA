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
package com.bbn.c2s2.pint.pf.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.configuration.PintConfigurable;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.Solution;
import com.bbn.c2s2.pint.pf.util.RingBasedSpatialViolationCalculator;
import com.bbn.c2s2.pint.pf.util.TemporalViolationCalculator;

/**
 * Generator that produces Solutions where all bindings meet the H-Consistency
 * metric, which uses a combination of temporal order and spatial distance.
 * 
 * @author tself
 * 
 */
public class HConsistent extends PintConfigurable implements ISolutionGenerator {

	/* Constants */
	private static final String CONFIG_PREFIX = "pf.generators.hconsistent.";
	private final String KEY_NUM_SOLUTIONS = CONFIG_PREFIX + "num-solutions";
	private final int DEFAULT_NUM_SOLUTIONS = 1000;

	private final String KEY_HCON_THRESHOLD = CONFIG_PREFIX + "hcon-threshold";
	private final double DEFAULT_HCON_THRESHOLD = 3.0;
	private final String KEY_SPATIAL_WEIGHT = CONFIG_PREFIX + "spatial-weight";
	private final double DEFAULT_SPATIAL_WEIGHT = 1.0;
	private final long RANDOM_SEED = 54;

	protected static Logger _logger = LoggerFactory
			.getLogger(HConsistent.class);

	private RnrmProcess process;
	private BindingGroup group;
	private Random rand;

	private double hConThreshold;
	private double spatialWeight;
	private int numSolutions;

	public HConsistent(RnrmProcess process, BindingGroup group,
			PintConfiguration config) {
		super(config);
		this.group = group;
		this.process = process;
		this.rand = new Random(this.RANDOM_SEED);

		hConThreshold = getConfig().getDouble(KEY_HCON_THRESHOLD,
				DEFAULT_HCON_THRESHOLD);
		spatialWeight = getConfig().getDouble(KEY_SPATIAL_WEIGHT,
				DEFAULT_SPATIAL_WEIGHT);
		numSolutions = getConfig().getInt(KEY_NUM_SOLUTIONS,
				DEFAULT_NUM_SOLUTIONS);
	}

	@Override
	public List<Solution> generateSolutions() {
		List<Solution> solutions = new ArrayList<Solution>();

		for (int count = 0; count < numSolutions; count++) {
			solutions.add(this.generateSolution());
		}
		return solutions;
	}

	public Solution generateSolution() {
		Solution out = new Solution(this.process);
		HashMap<Activity, Boolean> unboundActivities = new HashMap<Activity, Boolean>();
		for (Activity a : this.process.getActivities()) {
			unboundActivities.put(a, false);
		}
		Set<Integer> usedActivities = new HashSet<Integer>();
		do {
			Activity a = this.getActivityToBind(unboundActivities.keySet());
			List<Binding> hConSet = this.getHConsistentSet(out, a,
					usedActivities);
			if (hConSet.size() > 0) {
				Binding binding = this.getHConsistentActivity(hConSet);
				out.addBinding(binding);
				usedActivities.add(binding.getObservationID());
				unboundActivities.remove(a);
				for (Activity k : unboundActivities.keySet()) {
					unboundActivities.put(k, false);
				}
			} else {
				unboundActivities.put(a, true);
			}
		} while (unboundActivities.size() > 0
				&& atLeastOneActivityHasNotBeenCheckedSinceLastBind(unboundActivities));
		return out;
	}

	private boolean atLeastOneActivityHasNotBeenCheckedSinceLastBind(
			HashMap<Activity, Boolean> unboundActivities) {
		for (Activity k : unboundActivities.keySet()) {
			if (unboundActivities.get(k) == false)
				return true;
		}
		return false;
	}

	private List<Binding> getHConsistentSet(Solution cs, Activity a,
			Set<Integer> used) {
		List<Binding> out = new ArrayList<Binding>();
		if (this.group.getBindings(a) == null)
			return out;
		for (Binding t : this.group.getBindings(a)) {
			if (!used.contains(t.getObservationID())
					&& this.isHConsistent(cs, t))
				out.add(t);
		}
		return out;
	}

	private boolean isHConsistent(Solution cs, Binding binding) {
		double tv = TemporalViolationCalculator.temporalViolations(cs, binding);
		if (tv > hConThreshold)
			return false;
		double sv = RingBasedSpatialViolationCalculator.evaluateBinding(cs,
				binding, 2, 2);
		return (tv + spatialWeight * sv <= hConThreshold);
	}

	private Binding getHConsistentActivity(List<Binding> in) {
		int index = this.rand.nextInt(in.size());
		return in.get(index);
	}

	private Activity getActivityToBind(Set<Activity> s) {
		int index = this.rand.nextInt(s.size());
		int count = 0;
		for (Activity a : s) {
			if (index == count) {
				return a;
			}
			count++;
		}
		return null;
	}
}
