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



public class NumberOfElementsFilter extends AbstractTransactionFilter {

	// initial setup to accept everything
	private int minCount = 0;
	private int maxCount = Integer.MAX_VALUE;
	
	public NumberOfElementsFilter( int min_elements ) {
		minCount = min_elements;
	}

	public NumberOfElementsFilter( int min_elements, int max_elements ) {
		minCount = min_elements;
		maxCount = max_elements;
	}
	
	@Override
	public boolean accept( BitCoinTransaction t  ) {
		if( null == t )
			return false;		
		int count = t.elementCount();
		if( count <= maxCount && count >= minCount )
				return true;
		return false;
	}
	
	public String toString( ) {
		if( maxCount == Integer.MAX_VALUE )
			return this.getClass().getSimpleName() +  " (minCount=" + minCount + ")";
		else
			return this.getClass().getSimpleName() +  " (minCount=" + minCount + ", maxCount=" + maxCount + ")";
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
