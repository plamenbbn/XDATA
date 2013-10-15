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
 ******************************************************************************/


package com.bbn.xdata.bitcoin;

import java.util.Iterator;



public class IndividualValueFilter extends AbstractTransactionFilter {

	// initial setup to accept everything
	private double minValue = Double.NEGATIVE_INFINITY;
	private double maxValue = Double.POSITIVE_INFINITY;
	
	public IndividualValueFilter( double min_val ) {
		minValue = min_val;
	}

	public IndividualValueFilter( double min_val, double max_val ) {
		minValue = min_val;
		maxValue = max_val;
	}
	
	@Override
	public boolean accept( BitCoinTransaction t  ) {
		if( null == t )
			return false;
		
		Iterator<BitCoinTransaction.TransactionElement> it = t.elements(); 
		while( it.hasNext() ) {
			BitCoinTransaction.TransactionElement e = it.next();			
			if( e.value <= maxValue && e.value >= minValue )
				return true;
		}
		return false;
	}
	
	public String toString( ) {
		if( maxValue == Double.POSITIVE_INFINITY )
			return this.getClass().getSimpleName() +  " (minValue=" + minValue + ")";
		else
			return this.getClass().getSimpleName() +  " (minValue=" + minValue + ", maxValue=" + maxValue + ")";
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
