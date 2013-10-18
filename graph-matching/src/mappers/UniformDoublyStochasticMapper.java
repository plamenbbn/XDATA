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

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

//import com.bbn.fileformats.*;
import fileformats.*;

public class UniformDoublyStochasticMapper extends Mapper<LongWritable, Text, Text, MatrixBlock>{

	Text outKey;
	MatrixBlock outVal;
	
	protected void setup (Context context) {
		Configuration conf = context.getConfiguration();
		
		int N = conf.getInt("N",0);
		int sR = conf.getInt("SR", 0);
		int sC = conf.getInt("SC", 0);
		String name = conf.get("RESNAME");
		
		outKey = new Text();
		outVal = new MatrixBlock(name, sR, sC);
	}
	

    public void map(LongWritable key, Text value, Context context ) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		int N = conf.getInt("N",0);
		int sR = conf.getInt("SR", 0);
		int sC = conf.getInt("SC", 0);
		String name = conf.get("RESNAME");
		String delim = conf.get("DELIM");
		
		String[] parts = value.toString().split(delim);
				
		int row = (int) Float.parseFloat(parts[0]);
		int col = (int) Float.parseFloat(parts[1]);
		
		double v = 1 / (double) N;
		for (int i = 0; i < sR; i++) {
			for (int j = 0; j < sC; j++) {
				if (sR*row+i < N && sC*col+j < N)
					outVal.put(i,j,v);
				else 
					outVal.put(i,j,0);
			}
		}
		
		outVal.nR = row;
		outVal.nC = col;
		
		outKey.set(name + "," + String.valueOf(row) + "," + String.valueOf(col));
		context.write(outKey,outVal);
	}
}