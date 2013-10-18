/*
# DARPA XDATA licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with 
# the License.  You may obtain a copy of the License at 
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and  
# limitations under the License.
#
# Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
#
*/

package auction;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.giraph.io.formats.*;
import java.io.IOException;

//import com.bbn.auction.giraph.*;
import auction.*;
import fileformats.*;

public class IdDoubleMatrixVertexValueInputFormat extends
    TextVertexValueInputFormat<LongWritable, AuctionVertexValue, Writable, AuctionMessage> {
	
	//should be customizable
	private static final String delimiter = "\t";

	@Override
	public TextVertexValueReader createVertexValueReader (InputSplit split, TaskAttemptContext context) throws IOException {
		return new DMTextVertexValueReader();
	}

 	protected class DMTextVertexValueReader extends
       TextVertexValueReaderFromEachLine {
	   
		@Override 
		protected LongWritable getId(Text line) throws IOException {
			String[] values = line.toString().split(delimiter);
					
			return new LongWritable(Long.parseLong(values[0]));
		}
	   
		@Override 
		protected AuctionVertexValue getValue(Text line) throws IOException {
			String[] values = line.toString().split(delimiter);
		
			AuctionVertexValue row = new AuctionVertexValue(values.length - 1, true);
		
			for (int i = 1; i < values.length; i++) {
				row.setBenefit(i-1,Double.parseDouble(values[i]));
			}
		
		return row;
		}
	}
 }