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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for components that accept a
 * <code>PintConfiguration</code> object for their literal parameters.
 * 
 * @author tself
 * 
 */
public class PintConfigurable {
	private PintConfiguration _config;
	protected static Logger _logger;

	public PintConfigurable(PintConfiguration config) {
		_logger = LoggerFactory.getLogger(this.getClass());
		_config = config;
	}

	protected PintConfiguration getConfig() {
		return _config;
	}
}
