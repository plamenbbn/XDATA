package com.bbn.algebra.hadoop.reducers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;


import com.bbn.fileformats.*;

	public class MatrixBlockAddReducer extends Reducer<Text,MatrixBlock,Text,MatrixBlock> {
	
		Text outKey;
		MatrixBlock outVal;
		//would rather allocate it and set it - need to make sure that set works though - also need 
		//to pass correct parameters
		
		protected void setup (Context context) {
			Configuration conf = context.getConfiguration();
			int sR = conf.getInt("SR", 0);
			int sC = conf.getInt("SC", 0);
		
			outVal = new MatrixBlock(sR, sC);
			outKey = new Text();
		}
			
        public void reduce(Text key, Iterable<MatrixBlock> values, Context context) throws IOException, InterruptedException {
			Configuration conf =  context.getConfiguration();
			String name = conf.get("RESNAME");
			String lName = conf.get("LEFTNAME");
			double alpha = (double) conf.getFloat("ALPHA",1);
			double beta = (double) conf.getFloat("BETA",1);
					
			boolean first = false;
			boolean left = false;
			
			for (MatrixBlock val : values) {
				if (!first) {
					outVal.set(val);
					
					if (val.name.equals(lName))
						left = true;
					first = true;
				}
				else {
					if (left)
						outVal.simpleAddIP(val,alpha,beta);
					else
						outVal.simpleAddIP(val,beta,alpha);
				}
			}
				
			outVal.name = name;
				
			String[] parts = key.toString().split(",");
			outKey.set(outVal.name + "," + parts[1] + "," + parts[2]);
			
			context.write(outKey,outVal);
        }
    }