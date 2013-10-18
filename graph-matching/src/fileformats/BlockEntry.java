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

package fileformats;

import java.io.*;
import org.apache.hadoop.io.*;

public class BlockEntry implements Writable {
	public int idx1;
	public int idx2;
	public double val;
	
	public BlockEntry () {idx1 = idx2 = 0; val = 0;}
	public BlockEntry (int i1, int i2, double v) {idx1 = i1; idx2 = i2; val = v;}
	
	public void set(int i1, int i2, double v) {idx1 = i1; idx2 = i2; val = v;}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		idx1 = in.readInt();
		idx2 = in.readInt();
		val = in.readDouble();
	}

	@Override
	public void write (DataOutput out) throws IOException {
		out.writeInt(idx1);
		out.writeInt(idx2);
		out.writeDouble(val);
	}
}