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

public class SquareBlockTraceMultiplicationGroupingMapper extends Mapper<Text, MatrixBlock, Text, MatrixBlock>{

	public Text outKey = new Text ();

    public void map(Text key, MatrixBlock value, Context context ) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		boolean lTrans = conf.getBoolean("LTRANS",false);
		boolean rTrans = conf.getBoolean("RTRANS",false);
			
		String lName = conf.get("LEFTNAME");
			
		//can get the block indices from the text key or from the .nR and .nC fields
		String line = key.toString();
		String[] parts = line.split(",");
		String name = parts[0];
			
		//key format is "i,j,k" 
		if (name.equals(lName)) {
			
			int row = lTrans? Integer.parseInt(parts[2]) : Integer.parseInt(parts[1]);
			int col = lTrans? Integer.parseInt(parts[1]) : Integer.parseInt(parts[2]);
			
			outKey.set(String.valueOf(row) + "," + String.valueOf(row) + "," + String.valueOf(col));
			context.write(outKey,value);
		}
			
		else {	
		
			int row = rTrans? Integer.parseInt(parts[2]) : Integer.parseInt(parts[1]);
			int col = rTrans? Integer.parseInt(parts[1]) : Integer.parseInt(parts[2]);
			
			outKey.set(String.valueOf(col) + "," + String.valueOf(col) + "," + String.valueOf(row));
			context.write(outKey,value);
		}
	}
}