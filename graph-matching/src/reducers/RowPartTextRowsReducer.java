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

	public class RowPartTextRowsReducer extends Reducer<Text,RowPart,NullWritable,Text> {
	
		String[] section;
		Text outVal;
	
		protected void setup (Context context) {
			Configuration conf = context.getConfiguration();
			
			int nC = conf.getInt("NC",0);
			section = new String[nC];
			
			outVal = new Text();
		}
			
        public void reduce(Text key, Iterable<RowPart> values, Context context) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			
			int N = conf.getInt("N",0);
			int sC = conf.getInt("SC",0);
	
			//get string for each
			for (RowPart val : values) {
				section[val.colBlock] = String.valueOf(val.get(0));
				for (int i = 1; i < val.length; i++) {
					section[val.colBlock]+= "\t" + String.valueOf(val.get(i));
				}
			}
			
			//build output string
			String outString = key.toString();

			for (int i = 0; i<section.length-1;i++) {
				outString += "\t" + section[i];
			}
			
			String[] last = section[section.length-1].split("\t"); 
			for (int i = 0; (section.length - 1) * sC + i < N; i++) {
				outString += "\t" + last[i];
			}
			
			outVal.set(outString);
			context.write(NullWritable.get(),outVal);
		}
	}