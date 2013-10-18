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

	public class SquareMatrixBlockTraceMultReducer extends Reducer<Text,MatrixBlock,NullWritable,DoubleWritable> {
	
		MatrixBlock left;
		MatrixBlock right;
		DoubleWritable outVal;
		
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			
			int sRL = conf.getInt("SRL",0);
			int sCL = conf.getInt("SCL",0);
			int sRR = conf.getInt("SRR",0);
			int sCR = conf.getInt("SCR",0);
			
			left = new MatrixBlock(sRL,sCL);
			right = new MatrixBlock(sRR,sCR);
			
			outVal = new DoubleWritable();
		}
		
	
        public void reduce(Text key, Iterable<MatrixBlock> values, Context context) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
	
			String lName = conf.get("LEFTNAME");
			String rName = conf.get ("RIGHTNAME");
			String resName = conf.get("RESNAME");
			
			boolean lTrans = conf.getBoolean("LTRANS", false);
			boolean rTrans = conf.getBoolean("RTRANS", false);
			
			double scalar = conf.getFloat("SCALAR", 1);
			
			for (MatrixBlock val : values) {
			
				if (val.name.equals(lName)) {
					left.set(val);
				}
				else if (val.name.equals(rName)) {
					right.set(val);
				}
			}
			
			outVal.set(left.SquareTraceMult(right, scalar, lTrans,rTrans));
			context.write(NullWritable.get(), outVal);
        }
    }