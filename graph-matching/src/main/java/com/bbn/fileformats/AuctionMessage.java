package com.bbn.fileformats;

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
	
	
	
	