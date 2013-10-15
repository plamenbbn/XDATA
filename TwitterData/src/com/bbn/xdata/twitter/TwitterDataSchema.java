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

import java.util.ArrayList;


public class TwitterDataSchema {

	private ArrayList<TwitterDataColumn> columns = new ArrayList<TwitterDataColumn>();
	
	public TwitterDataSchema() {
		// empty schema
	}
	
	public TwitterDataSchema( TwitterDataColumn... columns ) {
		for( TwitterDataColumn c : columns ) {
			this.columns.add( c );
		}
	}
	
	public TwitterDataRecord createRecord( String[] split ) {
		TwitterDataRecord record = new TwitterDataRecord( this );
		for( TwitterDataColumn c : columns ) {
			record.set( c, split[c.ordinal()] );
		}		
		return record;
	}
	
	public TwitterDataColumn getColumnAtIndex( int index ) {
		return columns.get(index);
	}
	
	public int getColIndex( TwitterDataColumn col ) {
		for( int i = 0; i < columns.size(); i++ ) {
			if( col == columns.get(i) )
				return i;
		}
		return -1;
	}
	
	public int size() {
		return columns.size();
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
		for( TwitterDataColumn c : columns ) {
			if( first ) {
				sb.append( c.toString() );
				first = false;
			}
			else {
				sb.append( delim );
				sb.append( c.toString() );
			}
		}
		return sb.toString();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwitterDataSchema fullSchema = new TwitterDataSchema( TwitterDataColumn.values() );
		System.out.println( fullSchema.toCSVString() );

		TwitterDataSchema partialSchema = new TwitterDataSchema( 
				TwitterDataColumn.id, TwitterDataColumn.created_at,
				TwitterDataColumn.user_name, TwitterDataColumn.text_translated, 
				TwitterDataColumn.longitude, TwitterDataColumn.latitude );
		System.out.println( partialSchema.toCSVString() );
	}

}
