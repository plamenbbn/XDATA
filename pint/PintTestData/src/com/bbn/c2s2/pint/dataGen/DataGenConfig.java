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

package com.bbn.c2s2.pint.dataGen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tself
 * 
 */
public class DataGenConfig {
	private Date startDate;
	private Date endDate;
	private double dataRadiusKm = 5;
	private double truthRadiusKm = 0.5;
	private double latitude = 0;
	private double longitude = 0;
	private double signalNoiseRatio = 1000000;
	private double truthDurationDays = 2;
	private int numTruths = 1;
	private double percentVisible = 1.0f;
	private long randomSeed = 1132405;

	public DataGenConfig() {
		Calendar cal = Calendar.getInstance();
		endDate = cal.getTime();
		cal.add(Calendar.DAY_OF_YEAR, -30);
		startDate = cal.getTime();
	}

	public DataGenConfig(Date start, Date end, Double truthDurationDays,
			Double dataRadiusKm, Double truthRadiusKm, Double latitude,
			Double longitude, Double signalNoiseRatio, double percentVisible,
			int numTruths) {
		this();
		startDate = start;
		endDate = end;
		this.truthDurationDays = truthDurationDays;
		this.dataRadiusKm = dataRadiusKm;
		this.truthRadiusKm = truthRadiusKm;
		this.latitude = latitude;
		this.longitude = longitude;
		this.signalNoiseRatio = signalNoiseRatio;
		this.percentVisible = percentVisible;
		this.numTruths = numTruths;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public double getDataRadiusKm() {
		return dataRadiusKm;
	}

	public void setDataRadiusKm(double dataRadiusKm) {
		this.dataRadiusKm = dataRadiusKm;
	}

	public double getTruthRadiusKm() {
		return truthRadiusKm;
	}

	public void setTruthRadiusKm(double truthRadiusKm) {
		this.truthRadiusKm = truthRadiusKm;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getSignalNoiseRatio() {
		return signalNoiseRatio;
	}

	public void setSignalNoiseRatio(double signalNoiseRatio) {
		this.signalNoiseRatio = signalNoiseRatio;
	}

	public double getTruthDurationDays() {
		return truthDurationDays;
	}

	public void setTruthDurationDays(double truthDurationDays) {
		this.truthDurationDays = truthDurationDays;
	}

	public int getNumTruths() {
		return numTruths;
	}

	public void setNumTruths(int numTruths) {
		this.numTruths = numTruths;
	}

	public double getPercentVisible() {
		return percentVisible;
	}

	public void setPercentVisible(double percentVisible) {
		this.percentVisible = percentVisible;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}
}
