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

/**
 * Class that contains static constant values used throughout the PINT project,
 * such as property names.
 * 
 * @author tself
 * 
 */
public class Constants {
	/* Parameter names and defaults provided by user */
	public static final String KEY_PROCESS_MAX_DISTANCE_KM = "process.max.distance.km";
	public static final String KEY_PROCESS_MAX_TIMESPAN_MS = "process.max.timespan.ms";
	public static final double DEFAULT_PROCESS_MAX_DISTANCE_KM = 1.0;
	public static final double DEFAULT_PROCESS_MAX_TIMESPAN = Double.MAX_VALUE;

	public class WSStatus {
		public static final String NO_SUCH_GROUP = "NO_SUCH_GROUP";
		public static final String REQUEST_FAILED = "REQUEST_FAILED";
		public static final String PROCESSING = "PROCESSING";
		public static final String COMPLETED = "COMPLETED";
	}

	/**
	 * Array of 10 prime numbers that can be used by hash functions
	 */
	public static final int[] HASH_PRIMES = { 12289, 313, 1291, 300821, 3187,
			1801477, 2473, 2850997, 5783, 2101247 };

}
