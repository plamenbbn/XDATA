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
 * Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
 * 
 ******************************************************************************/

package com.bbn.xdata.twitter;

//
// these lines intentionally left blank.
//
//
//
//
// end lines left blank
//
public enum TwitterDataColumn {
	id,
	text,
	created_at,
	rt_count,
	point_geom,
	hashtag,
	mentions,
	in_reply_to_id,
	in_reply_user_id,
	in_reply_user_screen_name,
	link,
	place_country,
	place_country_code,
	place_full_name,
	place_id,	
	place_name,
	place_street_address,
	place_type,
	place_url,
	user_description,
	user_id,
	user_location,
	user_name,
	user_screen_name,
	translated,
	source,
	text_translated,
	text_translit,
	hashtag_translit,
	mentions_translit,
	in_reply_to_user_screen_name_translit,
	user_description_translit,
	user_location_translit,
	user_name_translit,
	user_screen_name_translit,
	lang_primary,
	lang_secondary,
	date_translated,
	geo_country,
	geo_cocom,
	sentiment,
	longitude,
	latitude,
	;

	
	public static String toCSVString() {
		return toDelimitedString( "," );
	}

	public static String toTSVString() {
		return toDelimitedString( "\t" );
	}
		
	public static String toDelimitedString(String delim) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for( TwitterDataColumn s : TwitterDataColumn.values() ) {
			if( first ) {
				sb.append( s.toString() );
				first = false;
			}
			else {
				sb.append( delim );
				sb.append( s.toString() );
			}
		}
		return sb.toString();
	}
	
	public int toIndex( ) {
		return this.ordinal();
	}
	
	public static TwitterDataColumn fromIndex( int index ) {
		return TwitterDataColumn.values()[index];
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println( toCSVString() );
		int index = 0;
		for( TwitterDataColumn s : TwitterDataColumn.values() ) {
			System.out.println( s.toIndex() + "," + s.toString() + "," + TwitterDataColumn.fromIndex(index++) );
		}

	}

}
