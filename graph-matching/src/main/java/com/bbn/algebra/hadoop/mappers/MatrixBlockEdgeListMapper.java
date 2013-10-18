package com.bbn.algebra.hadoop.mappers;;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import com.bbn.fileformats.*;

public class MatrixBlockEdgeListMapper extends Mapper<Text, MatrixBlock, Text, RowPart>{

	public Text outKey = new Text ();

    public void map(Text key, MatrixBlock value, Context context ) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		//assumes text in row<delim>col<delim>value format
		String line = value.toString();
		String[] parts = line.split(delim);
		    
		int row = Integer.parseInt(parts[1]);
		int col = Integer.parseInt(parts[2]);
		double valD = Double.parseDouble(parts[2]);
		
		//can do this based on text key and config - or from the fields of the blocks
		int rStart = value.nR * value.sR + one;
		int cStart = value.nC * value.sC + one;
		
		System.out.printf("%d %d\n", rStart,cStart);
		
		for (int i = 0; i < value.sR; i++) {
			for (int j = 0; j < value.sR; j++) {
				outKey.set(String.valueOf(rStart+i) + delim + String.valueOf(cStart+j) + delim + String.valueOf(value.get(i,j)));
				context.write(outKey,NullWritable.get());
			}
		}			
	}
}