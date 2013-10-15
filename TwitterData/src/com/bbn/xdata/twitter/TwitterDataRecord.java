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

public class TwitterDataRecord {

	private TwitterDataSchema schema = new TwitterDataSchema();
	
	private String[] rawData = new String[schema.size()];
	
	
	public TwitterDataRecord( ) {
		
	}
	
	public TwitterDataRecord( TwitterDataSchema schema ) {
		this.schema = schema;
		rawData = new String[schema.size()];
	}
	
	public boolean set( TwitterDataColumn col, String data ) {
		int index = schema.getColIndex( col );
		if( index >= 0 ) {
			rawData[index] = data;
			return true;
		}
		else {
			throw new IllegalArgumentException( "Column " + col + " is not part of the schema for this record. Data not inserted." );
		}
	}
	
	public String get( TwitterDataColumn col ) {
		int index = schema.getColIndex( col );
		if( index >= 0 ) {
			return rawData[index];
		}
		else {
			throw new IllegalArgumentException( "Column " + col + " is not part of the schema for this record. Cannot get data." );
		}		
	}

	public String get( int index ) {
		if( index >= 0 && index < rawData.length ) {
			return rawData[index];
		}
		else {
			throw new IllegalArgumentException( "Invalid index " + index + " for this record. Cannot get data." );
		}		
	}

	public String[] getRawData() {
		return rawData.clone();
	}
	
	
	public String toCSVString() {
		return toDelimitedString( "," );
	}

	public String toTSVString() {
		return toDelimitedString( "\t" );
	}
		
	public String toDelimitedString(String delim) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for( String data : rawData ) {
			if( first ) {
				sb.append( data );
				first = false;
			}
			else {
				sb.append( delim );
				sb.append( data );
			}
		}
		return sb.toString();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
