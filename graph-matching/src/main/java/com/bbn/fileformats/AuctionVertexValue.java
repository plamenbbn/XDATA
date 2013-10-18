package com.bbn.fileformats;

import java.io.*;
import org.apache.hadoop.io.*;


public class AuctionVertexValue implements Writable {
	public int N;
	public double [] benefit;
	public boolean row;
	public LongWritable colOwned;
	public LongWritable rowOwnedBy;
	public double price;
	
	//constructors
	public AuctionVertexValue () {row = false; N = 0; benefit = new double[N]; colOwned = new LongWritable(-1); rowOwnedBy = new LongWritable(-1); price = 0;}
	public AuctionVertexValue (boolean r) {row = r; N = 0; benefit = new double[N]; colOwned = new LongWritable(-1); rowOwnedBy = new LongWritable(-1); price = 0;}
	public AuctionVertexValue (int n, boolean r) {row = r; N = n; benefit = new double[N]; colOwned = new LongWritable(-1); rowOwnedBy = new LongWritable(-1); price = 0;}
	public AuctionVertexValue (int n, boolean r, double[] b) {row = r; N = n; benefit = b; colOwned = new LongWritable(-1); rowOwnedBy = new LongWritable(-1); price = 0;}
	
	//accessors/mutators
	public void setBenefit(int i, double v) {benefit[i] = v;}
	public double getBenefit (int i) {return benefit[i];}
	
	public void setColOwned (LongWritable l) {colOwned.set(l.get());}
	public void setColOwned (long l) {colOwned.set(l);}
	public LongWritable getColOwned () {return colOwned;}
	
	public void setRowOwnedBy (LongWritable l) {rowOwnedBy.set(l.get());}
	public void setRowOwnedBy (Long l) {rowOwnedBy.set(l);}
	public LongWritable getRowOwnedBy () {return rowOwnedBy;}
	
	public void setPrice (double p) {price = p;}
	public double getPrice () {return price;}
	
	//not sure what needs to be done here - this will get called by the text output format - only need the permuation from one side - not sure which
	@Override
	public String toString() {
		String out = "Row?: " + String.valueOf(row) + " colOwned: " + colOwned.toString() + " rowOwnedBy: " + rowOwnedBy.toString() + ":";
		for (int i = 0; i < N-1; i++) {
			out+=String.valueOf(benefit[i]) + " ";
		}
		out+=benefit[N-1];
		
		return out;
	}
	
	//serialization
	@Override
	public void readFields(DataInput in) throws IOException {
	
		N = in.readInt();
		row = in.readBoolean();
		colOwned.readFields(in);
		rowOwnedBy.readFields(in);
		price = in.readDouble();
		
		if (row) {
			benefit = new double[N];
		
			for (int i = 0; i < N; i++) {
				benefit[i] = in.readDouble();
			}
		}
	}

	
	
	@Override
	public void write (DataOutput out) throws IOException {
	
		out.writeInt(N);
		out.writeBoolean(row);
		colOwned.write(out);
		rowOwnedBy.write(out);
		out.writeDouble(price);
		
		if (row) {
			for (int i = 0; i < N; i++) {
				out.writeDouble(benefit[i]);
			}
		}
	}
}