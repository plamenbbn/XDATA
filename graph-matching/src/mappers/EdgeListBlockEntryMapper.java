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

package mappers;

import org.apache.hadoop.conf.Configuration;
import java.io.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

//import com.bbn.fileformats.*;
import fileformats.*;


public class EdgeListBlockEntryMapper extends Mapper<LongWritable, Text, Text, BlockEntry>{

	Text outKey = new Text();
	BlockEntry outVal = new BlockEntry();


    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			//get configuration variables
			Configuration conf = context.getConfiguration();
			int sR = conf.getInt("SR",1);
			int sC = conf.getInt("SC",1);
			
			String name = conf.get("RESNAME");
			
			//input to change a 1-indexed matrix to a 0-indexed matrix
			int one = conf.getInt("ONE",0);
			
			//delimeter for the text edge file
			String delim = conf.get("DELIM");
			
			//assumes text in row<delim>col<delim>value format
			String line = value.toString();
			String[] parts = line.split(delim);
		    
			int row = Integer.parseInt(parts[0]) - one;
			int col = Integer.parseInt(parts[1]) - one;
			double valD = Double.parseDouble(parts[2]);
			
			//write
			String out = name + "," + String.valueOf(row/sR) + "," + String.valueOf(col/sC);
			outKey.set(out);
			outVal.set(row%sR,col%sC,valD);
			context.write(outKey,outVal);
		}
	}