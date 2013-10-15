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
package com.bbn.c2s2.pint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * a set of observable URIs, each mapped to its corresponding activity 
 * 
 * @author ppetrov
 *
 */
public class ObservableSet extends HashSet<String> {

	private HashMap<String,Activity> observableToActivity = new HashMap<String,Activity>();
	
	@Override
	public boolean add( String observableUri ) {
		// TODO: question!!
		return super.add( observableUri );
	}
	
	public boolean add( String observableUri, Activity activity ) {
		observableToActivity.put( observableUri, activity );
		return this.add( observableUri );
	}
	
	public Activity getActivityFor( String observableUri ) {
		return observableToActivity.get(observableUri);
	}

	public Set<String> getObservableUris() {
		Set<String> rv = new HashSet<String>();
		while( this.iterator().hasNext() ) {
			rv.add( this.iterator().next() );
		}
		return rv;
	}

	public Set<String> getObservableUrisFor( Activity activity ) {
		Set<String> rv = new HashSet<String>();
		while( this.iterator().hasNext() ) {
			String uri = this.iterator().next();
			Activity act = getActivityFor( uri );
			if( act.equals(activity) ) {
				rv.add( uri );
			}
		}
		return rv;
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
