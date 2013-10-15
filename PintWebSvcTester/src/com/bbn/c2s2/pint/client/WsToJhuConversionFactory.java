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

import edu.jhuapl.c2s2.pp.DcmiType;
import edu.jhuapl.c2s2.pp.Provenance;
import edu.jhuapl.c2s2.pp.impl.SimpleProvenance;
import edu.jhuapl.c2s2.pp.observation.Geocode;
import edu.jhuapl.c2s2.pp.observation.SimpleGeocode;
import edu.jhuapl.c2s2.pp.observation.SimpleObservation;

/**
 * @author tself
 * 
 */
public class WsToJhuConversionFactory {

	public static Collection<SimpleObservation> convertObservations(
			Collection<edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation> observations) {
		Collection<SimpleObservation> rv = new ArrayList<SimpleObservation>();
		if (null != observations) {
			for (edu.jhuapl.c2s2.processfinderenterprisebus.SimpleObservation so : observations) {
				String uri = so.getUri();
				String observableUri = so.getObservableUri();
				String classification = so.getClassification();
				String creator = so.getCreator();
				Provenance source = convertProvenance(so.getSource());
				Provenance origSource = convertProvenance(so
						.getOriginatingSource());
				Calendar creationTime = so.getCreationTime().toGregorianCalendar();
				Calendar obsTime = so.getObservationTime().toGregorianCalendar();
				Geocode location = convertGeocode(so.getLocation());

				SimpleObservation converted = new SimpleObservation(uri,
						observableUri, classification, obsTime, location,
						creationTime, creator, source, origSource);
				rv.add(converted);
			}
		}
		return rv;
	}

	private static Geocode convertGeocode(edu.jhuapl.c2s2.processfinderenterprisebus.SimpleGeocode geo) {
		SimpleGeocode rv = null;
		if(null != geo) {
			rv = new SimpleGeocode(geo.getLatitude(), geo.getLongitude());
		}
		return rv;
	}
	private static Provenance convertProvenance(
			edu.jhuapl.c2s2.processfinderenterprisebus.SimpleProvenance toConvert) {
		Provenance rv = null;
		if (null != toConvert) {
			String uri = (null == toConvert.getUri()) ? "http://totally#filler" : toConvert.getUri();
			Calendar creationTime = (null == toConvert.getCreationTime()) ? Calendar.getInstance() : toConvert.getCreationTime()
					.toGregorianCalendar();
			String creator = toConvert.getCreator();
			Calendar lastModified = (null == toConvert.getLastModified()) ? Calendar.getInstance() : toConvert.getLastModified()
					.toGregorianCalendar();
			String format = toConvert.getFormat();
			DcmiType dcmiType = convertDcmiType(toConvert.getDcmiType());
			String identifier = toConvert.getIdentifier();
			String publisher = toConvert.getPublisher();
			String title = toConvert.getTitle();
			String altTitle = "";
			String classification = toConvert.getClassification();
			Provenance source = convertProvenance(toConvert.getOriginalSource());
			rv = new SimpleProvenance(uri, creationTime, creator, lastModified,
					format, dcmiType, identifier, publisher, title, altTitle,
					classification, source);
		}
		return rv;
	}
	
	private static DcmiType convertDcmiType(edu.jhuapl.c2s2.processfinderenterprisebus.DcmiType dType) {
		DcmiType rv = null;
		if(null != dType) {
			String name = dType.name();
			for(DcmiType t : DcmiType.values()) {
				if(t.name().equals(name)) {
					rv = t;
					break;
				}
			}
		}
		return rv;
	}
}
