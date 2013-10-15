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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import edu.jhuapl.c2s2.processfinderenterprisebus.DcmiType;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleGeocode;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleProvenance;

public class JhuToWsConversionFactory {
	public static final DateFormat DATE_FORMAT_XSD = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	/**
	 * Convert a SimpleObservation object from the Web Service API to the
	 * SimpleObservation implementation of the Observation interface. This is a
	 * deep copy.
	 * 
	 * @param observations
	 *            The list of observations to convert
	 * @return The converted list of the observations in the same order
	 */
	public static Collection<SimpleObservation> convertObservations(
			Collection<edu.jhuapl.c2s2.pp.observation.Observation> observations)
			throws Exception {
		Collection<SimpleObservation> toReturn = new LinkedList<SimpleObservation>();

		// short circuit out if null
		if (null == observations) {
			return null;
		}

		for (edu.jhuapl.c2s2.pp.observation.Observation o : observations) {
			SimpleObservation so = new SimpleObservation();
			so.setUri(o.getUri());
			so.setObservableUri(o.getObservableUri());
			so.setClassification(o.getClassification());
			so.setObservationTime(convertCalendar(o.getObservationTimestamp()));
			so.setLocation(convertLocation(o.getGeocode()));
			so.setCreator(o.getCreator());
			so.setSource(convertProvenance(o.getSource()));
			so.setCreationTime(convertCalendar(o.getCreationTime()));
			so
					.setOriginatingSource(convertProvenance(o
							.getOriginatingSource()));

			toReturn.add(so);
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
	public static XMLGregorianCalendar convertCalendar(Calendar calendar)
			throws Exception {
		XMLGregorianCalendar rv = null;
		if (null != calendar) {
			String lexicalDate = DATE_FORMAT_XSD.format(calendar.getTime());
			lexicalDate = lexicalDate.substring(0, lexicalDate.length() - 2)
					+ ":00";
			rv = DatatypeFactory.newInstance().newXMLGregorianCalendar(
					lexicalDate);
		}
		return rv;
	}

	/**
	 * Convert a SimpleGeocode from the Web Service API to the SimpleGeocode
	 * that implements the Geocode interface.
	 * 
	 * @param geocode
	 *            The Web Service geocode to convert
	 * @return The converted geocode
	 */
	public static SimpleGeocode convertLocation(
			edu.jhuapl.c2s2.pp.observation.Geocode geocode) {
		SimpleGeocode rv = null;
		if (geocode != null) {
			rv = new SimpleGeocode();
			rv.setLongitude(geocode.getLongitude());
			rv.setLatitude(geocode.getLatitude());
		}
		return rv;
	}

	/**
	 * Convert a SimpleProvenance from the Web Service API to the
	 * SimpleProvenance object that implements the Provenance interface
	 * 
	 * @param provenance
	 *            The object to convert
	 * @return The converted object
	 */
	public static SimpleProvenance convertProvenance(
			edu.jhuapl.c2s2.pp.Provenance provenance) throws Exception {
		SimpleProvenance rv = null;
		if (null != provenance) {
			rv = new SimpleProvenance();
			rv.setUri(provenance.getUri());
			rv.setCreationTime(convertCalendar(provenance.getCreationTime()));
			rv.setCreator(provenance.getCreator());
			rv.setLastModified(convertCalendar(provenance.getLastModified()));
			rv.setFormat(provenance.getFormat());
			rv.setDcmiType(convertDCMIType(provenance.getDcmiType()));
			rv.setIdentifier(provenance.getIdentifier());
			rv.setPublisher(provenance.getPublisher());
			rv.setTitle(provenance.getTitle());
			rv.setClassification(provenance.getClassification());
			rv.setOriginalSource(convertProvenance(provenance.getSource()));
		}
		return rv;
	}

	/**
	 * Convert the DCMIType from the Web Service interface to the one used in
	 * the system interface
	 * 
	 * @param type
	 *            The enum instance to convert
	 * @return The converted enum instance
	 */
	public static DcmiType convertDCMIType(edu.jhuapl.c2s2.pp.DcmiType type) {
		// short circuit out if null
		if (null == type) {
			return null;
		}

		DcmiType toReturn = null;
		String name = type.name();

		for (DcmiType nt : DcmiType.values()) {
			if (nt.name().equals(name)) {
				toReturn = nt;
				break;
			}
		}

		return toReturn;
	}
}
