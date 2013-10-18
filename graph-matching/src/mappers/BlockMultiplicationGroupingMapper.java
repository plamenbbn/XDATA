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

public class BlockMultiplicationGroupingMapper<T> extends Mapper<Text, T, Text, T>{

	public Text outKey = new Text ();

     public void map(Text key, T value, Context context ) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			int nRL = conf.getInt("NRL",0);
			int nCL = conf.getInt("NCL",0);
			int nRR	= conf.getInt("NRR",0);
			int nCR = conf.getInt("NCR",0);
			
			boolean lTrans = conf.getBoolean("LTRANS",false);
			boolean rTrans = conf.getBoolean("RTRANS",false);
			
			String lName = conf.get("LEFTNAME");
			String rName = conf.get("RIGHTNAME");
			
			//can get the block indices from the text key or from the .nR and .nC fields
			String line = key.toString();
			String[] parts = line.split(",");
		    String name = parts[0];
			
			//key format is "i,j,k" 
			if (name.equals(lName)) {

				System.out.println("left" + key.toString());
				for (int j = 0; j < (rTrans ? nRR : nCR); j++) {
					outKey.set((lTrans ? parts[2] : parts[1]) + "," + String.valueOf(j) + "," + (lTrans ? parts[1] : parts[2]));
					context.write(outKey,value);
				}
			}
			else {	
				System.out.println("right" + key.toString());
				for (int i = 0; i < (lTrans ? nCL: nRL); i++) {		
					outKey.set(String.valueOf(i) + "," + (rTrans ? parts[1] : parts[2]) + "," + (rTrans ? parts[2] : parts[1]));
					context.write(outKey,value);
				}
			}
		}
	}