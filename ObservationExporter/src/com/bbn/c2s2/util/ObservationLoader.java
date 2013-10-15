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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.List;

import org.json.JSONException;

import edu.jhuapl.c2s2.pp.observation.Observation;
import edu.jhuapl.c2s2.pp.observation.ObservationEncoder;

/**
 * Handles loading and saving the edu.jhuapl...Observation objects from and to a
 * .data file
 * 
 * @author tself
 * 
 */
public class ObservationLoader {

	public static boolean saveObservations(File saveFile,
			List<Observation> obsList) {
		boolean rv = true;
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ObservationEncoder.encode(obsList).toString());
			oos.flush();
			oos.close();

		} catch (IOException ioe) {
			rv = false;
		} catch (JSONException je) {
			rv = false;
		}
		return rv;
	}

	public static List<Observation> loadObservations(File dataFile) throws Exception {
		
		System.out.println( "ObservationLoader: Loading Observations from file: " + dataFile.getAbsolutePath() );
		
		List<Observation> aplObs;
		ObjectInputStream ois = new ObjectInputStream(
				(InputStream) new FileInputStream(dataFile));
		Object obs = ois.readObject();
		System.out.println("Object type: " + obs.getClass().getCanonicalName());
		StringReader reader = new StringReader(obs.toString());

		aplObs = ObservationEncoder.decodeListObservation(reader);
		ois.close();
		return aplObs;
	}

	public static void main(String[] args) throws Exception {
		List<Observation> obsList = loadObservations(Constants.FILE_OBSERVATIONS_DATA);
		// for (Observation o : obsList) {
		// System.out.println(String.format("%1$s : %2$s (%3$s)", o.getUri(),
		// o.getObservableUri(), Constants.DATE_FORMAT_XSD.format(o
		// .getObservationTimestamp().getTime())));
		// }
		System.out.println("Observation count: " + obsList.size());
		File saveFile = new File("saved.data");
		saveObservations(saveFile, obsList);
	}

	public static String getLabel(String obsUri) {
		// 4 fields after the name, separated by underscores
		String rv = obsUri.substring(obsUri.indexOf('#') + 1);
		for (int i = 0; i < 4; i++) {
			int lastIndex = rv.lastIndexOf('_');
			if (lastIndex > 0) {
				rv = rv.substring(0, lastIndex);
			} else {
				break;
			}
		}
		rv = rv.replace('_', ' ');
		return rv;
	}
}
