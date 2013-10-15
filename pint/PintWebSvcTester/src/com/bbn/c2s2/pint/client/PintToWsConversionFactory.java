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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bbn.c2s2.pint.Observation;

import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleGeocode;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation;
import edu.jhuapl.c2s2.processfinderenterprisebus.SimpleProvenance;

/**
 * @author tself
 * 
 */
public class PintToWsConversionFactory {
	public static final DateFormat DATE_FORMAT_XSD = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public static Collection<SimpleObservation> convertObservations(
			Collection<Observation> observations) throws Exception {
		Collection<SimpleObservation> rv = new ArrayList<SimpleObservation>();
		if (null != observations) {
			for (Observation o : observations) {
				SimpleObservation so = new SimpleObservation();
				so.setUri(o.getUri());
				so.setObservableUri(o.getObservableUri());
				so.setClassification("");
				so.setObservationTime(convertToXmlCalendar(o.getTimestamp()));
				so.setLocation(createGeocode(o.getLat(), o.getLon()));
				so.setCreator("");
				so.setSource(new SimpleProvenance());
				so.setCreationTime(convertToXmlCalendar(new Date()));
				so.setOriginatingSource(new SimpleProvenance());
				rv.add(so);
			}
		}
		return rv;
	}

	private static XMLGregorianCalendar convertToXmlCalendar(Date date)
			throws Exception {
		XMLGregorianCalendar rv = null;
		String lexicalDate = DATE_FORMAT_XSD.format(date);
		lexicalDate = lexicalDate.substring(0, lexicalDate.length() - 2)
				+ ":00";
		rv = DatatypeFactory.newInstance().newXMLGregorianCalendar(lexicalDate);
		return rv;
	}

	private static SimpleGeocode createGeocode(double lat, double lon) {
		SimpleGeocode rv = new SimpleGeocode();
		rv.setLatitude(lat);
		rv.setLongitude(lon);
		return rv;
	}
}
