package com.bbn.algebra.hadoop.mappers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import com.bbn.fileformats.*;

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