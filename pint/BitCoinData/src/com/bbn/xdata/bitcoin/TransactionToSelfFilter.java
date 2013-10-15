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


public class TransactionToSelfFilter extends AbstractTransactionFilter {

	private long targetUserKey = -1;
	
	private double minValue = -1.0;
	
	public TransactionToSelfFilter( double minValue ) {
		this.minValue = minValue;
	}

	
	public TransactionToSelfFilter( long targetUserKey, double minValue ) {
		this.targetUserKey = targetUserKey;
		this.minValue = minValue;
	}

	
	@Override
	public boolean accept( BitCoinTransaction t  ) {
		if( null == t )
			return false;
		
		Iterator<BitCoinTransaction.TransactionElement> it = t.elements(); 
		while( it.hasNext() ) {
			BitCoinTransaction.TransactionElement e = it.next();			
			if( targetUserKey < 1 ) {
				if( e.fromUserKey == e.toUserKey && e.value >= minValue )
					return true;
			}
			else {
				if( targetUserKey == e.toUserKey && e.value >= minValue )
					return true;
			}
		}
		return false;
	}
	
	public String toString( ) {
		if( targetUserKey < 1 )
			return "TransactionToSelfFilter (minValue=" + minValue + ")";
		else
			return "TransactionToUserID " + targetUserKey + " Filter (minValue=" + minValue + ")";
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
