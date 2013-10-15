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
package com.bbn.c2s2.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {
	public static final File DATA_DIRECTORY = new File(
			"../data/sample-dec14-2009");
	public static final DateFormat DATE_FORMAT_XSD = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");
	public static final DateFormat DATE_FORMAT_GREGORIAN = new SimpleDateFormat(
			"MMM dd yyyy HH:mm:ss 'GMT'Z");
	public static final DateFormat DATE_FORMAT_SHORT = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm");

	// test files
	public static final File FILE_EXAMPLES_RDF = new File(
			Constants.DATA_DIRECTORY, "resources/examples.rdf");
	public static final File FILE_OBSERVATIONS_DATA = new File(
			Constants.DATA_DIRECTORY, "observations.data");
	public static final File FILE_TRUTH_CSV = new File(
			Constants.DATA_DIRECTORY, "truth.csv");
	public static final File FILE_NOISE_CSV = new File(
			Constants.DATA_DIRECTORY, "noise.csv");

	public static final File DATA_DIRECTORY2 = new File(
			"../data/sample-jan29-2010");
	public static final File FILE_OBSERVATIONS_DATA2 = new File(
			Constants.DATA_DIRECTORY2, "observations.data");
	public static final File FILE_EXAMPLES_RDF2 = new File(
			Constants.DATA_DIRECTORY2, "resources/examples.rdf");
	public static final File FILE_RNRM_MODIFIED = new File(
			Constants.DATA_DIRECTORY2, "resources/examples2.rdf");

}
