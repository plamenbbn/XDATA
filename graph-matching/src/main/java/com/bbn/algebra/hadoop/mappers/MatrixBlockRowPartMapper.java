package com.bbn.algebra.hadoop.mappers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import com.bbn.fileformats.*;

public class MatrixBlockRowPartMapper extends Mapper<Text, MatrixBlock, Text, RowPart>{

	Text outKey = new Text();

	//could pre-allocate the whole array
	// protected void setup (Context context) {
		// Configuration conf = context.getConfiguration();
		
		// int sR = conf.getInt("SR",0);
		
		// outVal = new outVal(-1,-1,sR);
		// outKey = new Text();
	// }
	
    public void map(Text key, MatrixBlock value, Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		int N = conf.getInt("N",0);
	
		String[] parts = key.toString().split(",");
		    
		int row = Integer.parseInt(parts[1]);
		int col = Integer.parseInt(parts[2]);
				
		for (int r = 0; r < value.sR; r++) {
			
			if (r + row * value.sR < N) {
				RowPart current = new RowPart(r + row * value.sR, col, value.sR);
				outKey.set(String.valueOf(r + row * value.sR));
			
				for (int c = 0; c < value.sC; c++) {
					current.put(c,value.get(r,c));
				}
				
				context.write(outKey, current);
			}
		}
	}
}