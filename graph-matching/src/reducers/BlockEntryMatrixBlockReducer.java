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

package reducers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;


//import com.bbn.fileformats.*;
import fileformats.*;

	 public class BlockEntryMatrixBlockReducer extends Reducer<Text,BlockEntry,Text,MatrixBlock> {
							
		public void reduce(Text key, Iterable<BlockEntry> values, Context context) throws IOException, InterruptedException {
		
			Configuration conf = context.getConfiguration();
			int sR = conf.getInt("SR",0);
			int sC = conf.getInt("SC",0);
			
			String line = key.toString();
			String[] parts = line.split(",");
			
			String name = parts[0];
			int row = Integer.parseInt(parts[1]);
			int col = Integer.parseInt(parts[2]);
			
			//as of now this is dense - need to add sparse package
			MatrixBlock outVal = new MatrixBlock(name, sR, sC, row, col);
			
			//add values
			for (BlockEntry val : values) {
				outVal.put(val.idx1,val.idx2,val.val);
			}
			
			context.write(key,outVal);
		}
	}