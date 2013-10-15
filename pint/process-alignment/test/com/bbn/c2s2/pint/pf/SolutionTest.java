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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.testdata.SolutionFactory;
import com.bbn.c2s2.pint.testdata.TestRnrmProcessFactory;

/**
 * @author tself
 * 
 */
public class SolutionTest extends TestCase {
	private Solution slnOneUnbound;
	private Solution slnAllUnbound;
	private Solution slnFullBound;
	private RnrmProcess process;

	protected void setUp() throws Exception {
		super.setUp();
		process = TestRnrmProcessFactory.createSerialProcess();
		slnOneUnbound = SolutionFactory.createOrderedSolutionWithOneUnbound(
				process, 1);
		slnAllUnbound = SolutionFactory.createUnboundSolution(process);
		slnFullBound = SolutionFactory.createOrderedColocatedSolution(process);
	}

	public void testSolutionSolution() {
		Solution sln = new Solution(slnFullBound);
		// process
		assertSame("Same Process", process, sln.getProcess());

		// bound
		assertSame("Same Bindings", slnFullBound.getNonNullBindings(), sln
				.getNonNullBindings());

		// map
		assertSame("Same Act/Bind Map", slnFullBound.getActivityToBindingMap(),
				sln.getActivityToBindingMap());
	}

	public void testSolutionRnrmProcess() {
		Solution sln = new Solution(process);
		// process
		assertSame("Same Process", process, sln.getProcess());

		// map
		assertEquals("Activity Size", process.size(), sln
				.getActivityToBindingMap().size());

		// validate map contents
		validateActivities(process.getActivities(), sln
				.getActivityToBindingMap(), true);
	}

	private void validateActivities(Set<Activity> actualActs,
			Map<Integer, Binding> map, boolean allUnbound) {
		for (Binding b : map.values()) {
			if (allUnbound) {
				assertNull("Null Observation", b.getObservation());
			}
			assertTrue("Contains Activity", actualActs
					.contains(b.getActivity()));
		}
	}

	private void validateNoNullObservations(String msg,
			Collection<IObservation> objects) {
		for (IObservation o : objects) {
			assertNotNull(msg, o);
		}
	}

	public void testGetActivityToBindingMap() {
		// validate full contents
		validateActivities(process.getActivities(), slnFullBound
				.getActivityToBindingMap(), false);

		// works for unbound
		validateActivities(process.getActivities(), slnAllUnbound
				.getActivityToBindingMap(), true);

		// works for partial-bound
		validateActivities(process.getActivities(), slnOneUnbound
				.getActivityToBindingMap(), false);
	}

	public void testGetNonNullBindings() {
		// full
		assertEquals("All Bound", process.size(), slnFullBound
				.getNonNullBindings().size());

		// unbound
		assertEquals("All Unbound", 0, slnAllUnbound.getNonNullBindings()
				.size());

		// partial-bound
		assertTrue("Partial-Bound",
				slnOneUnbound.getNonNullBindings().size() < process.size());
		Solution sln = SolutionFactory.createOrderedSolutionWithOneUnbound(process, 0);
		assertTrue("Partial-Bound",
				sln.getNonNullBindings().size() < process.size());
		sln = SolutionFactory.createOrderedSolutionWithOneUnbound(process, process.size() - 1);
		assertTrue("Partial-Bound",
				sln.getNonNullBindings().size() < process.size());
	}

	public void testGetObservations() {
		// full
		Collection<IObservation> obs = slnFullBound.getObservations();
		assertEquals("All Bound", process.size(), obs.size());
		validateNoNullObservations("All Bound", obs);

		// unbound
		assertEquals("All Unbound", 0, slnAllUnbound.getObservations().size());

		// partial-bound
		assertTrue("Partial-Bound",
				slnOneUnbound.getObservations().size() < process.size());
	}

	public void testGetOrderedBindings() {
		// full
		List<Binding> bindings = slnFullBound.getOrderedBindings();
		assertEquals("All Bound", process.size(), bindings.size());

		// unbound
		bindings = slnAllUnbound.getOrderedBindings();
		assertNotNull("All Unbound", bindings);
		assertEquals("All Unbound", process.size(), bindings.size());

		// partial-bound
		bindings = slnOneUnbound.getOrderedBindings();
		assertEquals("Partial-Bound", process.size(), bindings.size());
	}

	public void testSize() {
		assertEquals("All Bound", process.size(), slnFullBound.size());
		assertEquals("All Unbound", process.size(), slnAllUnbound.size());
		assertEquals("Partial-Bound", process.size(), slnOneUnbound.size());
	}

	public void testGetBindings() {
		// correct size
		assertEquals(process.size(), slnFullBound.getBindings().size());

		// correct size after adding 1
		Solution sln = new Solution(process);
		Set<Activity> acts = process.getActivities();
		int i = 0;
		for (Activity act : acts) {
			Observation o = new Observation("http:" + i++, "", 0.0, 0.0,
					new Date());
			sln.addBinding(new Binding(act, o, i));
			// check that it's in there
			validateObservationInBindings(sln.getBindings(), o);
		}
	}

	private void validateObservationInBindings(Collection<Binding> bindings,
			Observation o) {
		boolean found = false;
		for (IBinding b : bindings) {
			if (null != b.getObservation() && b.getObservation().equals(o)) {
				found = true;
				break;
			}
		}
		assertTrue("Find Observation in Bindings", found);
	}

	public void testGetProcess() {
		assertNotNull(slnFullBound.getProcess());
	}

	public void testGetBinding() {
		// existing activity
		Activity act = process.get(0);
		Binding b = slnFullBound.getBinding(act);
		assertNotNull("Existing Activity",b);
		assertSame("Existing Activity", act, b.getActivity());
		
		// non-existent activity
		Activity nonAct = new Activity(process.size() + 3, "");
		b = slnFullBound.getBinding(nonAct);
		assertNull("Non-existent Activity", b);
	}

	public void testAddBinding() {
		// normal add
		Activity firstAct = process.getOrderedActivities().get(1);
		Observation o = new Observation("test:AddBinding", "", 0.0, 0.0, new Date());
		Binding b = new Binding(firstAct, o, process.size() + 3);
		assertNull("Normal Add", slnOneUnbound.getBinding(firstAct).getObservation());
		slnOneUnbound.addBinding(b);
		assertNotNull("Normal Add", slnOneUnbound.getBinding(firstAct).getObservation());
		assertSame("Normal Add", slnOneUnbound.getBinding(firstAct).getActivity(), firstAct);
		
		// add more than process contains
		Activity newAct = new Activity(process.size() + 3, "");
		b = new Binding(newAct, o, process.size() + 5);
		boolean caught = false;
		try {
			slnFullBound.addBinding(b);
		}
		catch(IllegalArgumentException e) {
			caught = true;
		}
		assertTrue("Add Invalid Activity", caught);
		
		// add same activity twice
		IBinding currentBinding = slnFullBound.getBinding(firstAct);
		b = new Binding(firstAct, o, process.size() + 10);
		slnFullBound.addBinding(b);
		assertEquals("Same Activity Twice", process.size(), slnFullBound.getNonNullBindings().size());
		IBinding newBinding = slnFullBound.getBinding(firstAct);
		assertNotSame("Same Activity Twice", currentBinding.getObservation(), newBinding.getObservation());
	}

	/**
	 * Test method for {@link com.bbn.c2s2.pint.pf.Solution#toString()}.
	 */
	public void testToString() {
		assertNotNull("Empty Solution", slnAllUnbound.toString());
		assertNotNull("Bound Solution", slnFullBound.toString());
		assertNotNull("Partial-Bound Solution", slnOneUnbound.toString());
	}

}
