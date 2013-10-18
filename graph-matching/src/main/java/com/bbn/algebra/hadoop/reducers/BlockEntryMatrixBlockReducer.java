package com.bbn.algebra.hadoop.reducers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;


import com.bbn.fileformats.*;

	 public class BlockEntryMatrixBlockReducer extends Reducer<Text,BlockEntry,Text,MatrixBlock> {
							
		public void reduce(Text key, Iterable<BlockEntry> values, Context context) throws IOException, InterruptedException {
		
			Configuration conf = context.getConfiguration();
			int sR = conf.getInt("SR",0);
			int sC = conf.getInt("SC",0);
			
			String line = key.toString();
			String[] parts = line.split(",");
			
			String name = parts[0];
			int row = Integer.parseInt(parts[1]);
			int col = Integer.parseInt(parts[2]);
			
			//as of now this is dense - need to add sparse package
			MatrixBlock outVal = new MatrixBlock(name, sR, sC, row, col);
			
			//add values
			for (BlockEntry val : values) {
				outVal.put(val.idx1,val.idx2,val.val);
			}
			
			context.write(key,outVal);
		}
	}