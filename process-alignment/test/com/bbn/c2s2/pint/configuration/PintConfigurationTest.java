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
package com.bbn.c2s2.pint.configuration;

import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

public class PintConfigurationTest extends TestCase {
	protected PintConfiguration configuration = null;
	int paramCount = -1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Properties props = new Properties();
		props.put("string", "value");
		props.put("integer", "1");
		props.put("double", "2.0");
		props.put("empty", "");

		props.put("a.b.c.1", "c1");
		props.put("a.b.c.2", "c2");
		props.put("a.b.1", "b1");
		props.put("a.b.2", "b2");
		props.put("a.1", "a1");
		props.put("a.2", "a2");

		paramCount = 10;
		configuration = new PintConfiguration(props);
	}

	public void testGetString() throws Exception {
		// test a normal get
		assertEquals("value", configuration.getString("string", "default"));

		// test where the value is empty
		assertEquals("", configuration.getString("empty", "default"));

		// test where the value is nonexistent
		assertEquals("default", configuration.getString("nonexistent",
				"default"));
	}

	public void testGetDouble() throws Exception {
		// test a normal get
		assertEquals(2.0, configuration.getDouble("double", 3.0));

		// test a default value
		assertEquals(3.0, configuration.getDouble("nonexistent", 3.0));

		// test where value is empty
		assertEquals(3.0, configuration.getDouble("empty", 3.0));

		// test where value is nonexistent
		assertEquals(3.0, configuration.getDouble("nonexistent", 3.0));

		// test where value is an int
		assertEquals(1.0, configuration.getDouble("integer", 3.0));
	}

	public void testInteger() throws Exception {
		// test a normal get
		assertEquals(1, configuration.getInt("integer", 4));

		// test a default value
		assertEquals(4, configuration.getInt("nonexistent", 4));

		// test where value is empty
		assertEquals(1, configuration.getInt("empty", 1));

		// test where value is nonexistent
		assertEquals(1, configuration.getInt("nonexistent", 1));

		// test where value is a double
		assertEquals(1, configuration.getInt("double", 1));
	}

	public void testGetSubset() throws Exception {
		PintConfiguration subset = configuration.getSubset("a");
		assertEquals(6, subset.size());

		subset = configuration.getSubset("a.b");
		assertEquals(4, subset.size());

		subset = configuration.getSubset("a.b.c");
		assertEquals(2, subset.size());
	}

	public void testGetSize() throws Exception {
		assertEquals(10, configuration.size());
	}
	
	public void testGetKeys() throws Exception {
		Collection<String> keys = configuration.getKeys();
		assertEquals(paramCount, keys.size());
		assertTrue(keys.contains("string"));
		assertTrue(keys.contains("a.b.1"));
	}
}
