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
package com.bbn.c2s2.pint.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bbn.c2s2.pint.Observation;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;

public class WsToPintConversionFactory {

	/**
	 * Convert a SimpleObservation object from the Web Service API to the
	 * SimpleObservation implementation of the Observation interface. This is a
	 * deep copy.
	 * 
	 * @param observations
	 *            The list of observations to convert
	 * @return The converted list of the observations in the same order
	 */
	public static Collection<Observation> convertObservations(
			Collection<SimpleObservation> observations) {
		Collection<Observation> toReturn = new ArrayList<Observation>(
				observations.size());

		// short circuit out if null
		if (null == observations) {
			return null;
		}

		for (SimpleObservation so : observations) {
			Observation o = new Observation(so.getUri(), so.getObservableUri(),
					so.getLocation().getLatitude(), so.getLocation()
							.getLongitude(), convertCalendar(
							so.getObservationTime()).getTime());
			toReturn.add(o);
		}

		return toReturn;
	}

	/**
	 * Convert an XMLGregorianCalendar instance to a regular Java Calendar.
	 * 
	 * @param xmlCalendar
	 *            The calendar to convert
	 * @return The converted calendar
	 */
	public static Calendar convertCalendar(XMLGregorianCalendar xmlCalendar) {
		Calendar rv = null;
		if (xmlCalendar != null) {
			String dateString = xmlCalendar.toXMLFormat();
			rv = DatatypeConverter.parseTime(dateString);
		}
		return rv;
	}
}
