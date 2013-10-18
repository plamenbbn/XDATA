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


public class AuctionMessage implements Writable {
	public LongWritable sender;
	public double bid;
	
	public AuctionMessage() {sender = new LongWritable(0); bid = 0;}
	public AuctionMessage(LongWritable s, double b) {sender = s; bid = b;}
	public AuctionMessage(long s, double b) {sender = new LongWritable(s); bid = b;}
	
	public void set(LongWritable s, double b) {sender = s; bid = b;}
	public void set(long s, double b) {sender.set(s); bid = b;}
	
	public void setSender (LongWritable s) {sender = s;}
	public void setSender (long s) {sender.set(s);}
	public LongWritable getSender () {return sender;}
	
	public void setBid (double b) {bid = b;}
	public double getBid () {return bid;}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		sender.readFields(in);
		bid = in.readDouble();
	}

	@Override
	public void write (DataOutput out) throws IOException {
		sender.write(out);
		out.writeDouble(bid);
	}
}
	
	
	
	