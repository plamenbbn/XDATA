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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PINT Configuration class.
 * 
 * This class is a container for all of the properties that are used throughout
 * the PINT system. This class is a wrapper for the {@link Properties} class
 * that adds some convenience methods for accessing values of specific data
 * types and for getting subsets of the properties. The main way to construct
 * this configuration instance is to pass in a {@link Properties} object
 * containing all of the key/value pairs for the PINT configuration.
 * 
 * @author reblace
 * 
 */
public class PintConfiguration implements Serializable {

	private static final long serialVersionUID = -6233092224947928115L;
	protected static Logger _logger = LoggerFactory
			.getLogger(PintConfiguration.class);
	private Properties props;

	/**
	 * Create a new PINT configuration instance. Properties will be loaded from
	 * the {@link Properties} instance.
	 * 
	 * @param props
	 *            Contains the key/value pairs of the PINT configuration
	 */
	public PintConfiguration(Properties props) {
		this.props = props;
	}

	/**
	 * Gets the configuration value corresponding to the key. If the property is
	 * not found, the default value is returned.
	 * 
	 * @param key
	 *            The property to return
	 * @param defaultValue
	 *            The default value to return if the property is not found
	 * @return The value corresponding to the key, or the default value if the
	 *         key is not found
	 */
	public String getString(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * Gets the double representation of the value corresponding to the key
	 * 
	 * @param key
	 *            The property to return
	 * @param defaultValue
	 *            The default value to return if the property is not found
	 * @return A double value corresponding to the property, or the default
	 *         value if the property is not found
	 */
	public double getDouble(String key, double defaultValue) {
		double rv = defaultValue;
		String val = props.getProperty(key);
		try {
			rv = Double.parseDouble(val);
		} catch (Exception e) {
			_logger.warn(String.format("Invalid or missing parameter '%1$s'. "
					+ "The value '%2$s' is not a valid Double. Using default "
					+ "value '%3$s' instead.", key, val, defaultValue));
		}
		return rv;
	}

	/**
	 * Gets the Integer representation of the value corresponding to the key
	 * 
	 * @param key
	 *            The property to return
	 * @param defaultValue
	 *            The default value to return if the property is not found
	 * @return An integer value corresponding to the property, or the default
	 *         value if the property is not found
	 */
	public int getInt(String key, int defaultValue) {
		int rv = defaultValue;
		String val = props.getProperty(key);
		try {
			rv = Integer.parseInt(val);
		} catch (Exception e) {
			_logger.warn(String.format("Invalid or missing parameter '%1$s'. "
					+ "The value '%2$s' is not a valid Integer. Using default "
					+ "value '%3$s' instead.", key, val, defaultValue));
		}
		return rv;
	}

	/**
	 * Gets the subset of properties that start with the specified prefix. A new
	 * {@link PintConfiguration} is returned with only the subset of properties
	 * 
	 * @param prefix
	 *            The prefix to use to generate the property subset
	 * @return A new {@link PintConfiguration} with the subset of properties
	 */
	protected PintConfiguration getSubset(String prefix) {
		PintConfiguration rv = null;

		Properties newProps = new Properties();
		boolean addAll = (prefix == null || prefix.length() < 1);

		for (String key : props.stringPropertyNames()) {
			if (addAll || key.startsWith(prefix)) {
				newProps.setProperty(key.substring(prefix.length()), props
						.getProperty(key));
			}
		}

		rv = new PintConfiguration(newProps);
		return rv;
	}

	/**
	 * @return the number of properties in the configuration
	 */
	public int size() {
		return props.size();
	}

	/**
	 * Returns the collection of config parameter names.
	 * 
	 * @return Names of all config parameters
	 */
	public Collection<String> getKeys() {
		ArrayList<String> rv = new ArrayList<String>(props.size());
		for (Object o : props.keySet()) {
			rv.add((String) o);
		}
		return rv;
	}
}
