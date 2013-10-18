package com.bbn.fileformats;

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