package com.bbn.algebra.hadoop.mappers;

import org.apache.hadoop.conf.Configuration;
import java.io.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import com.bbn.fileformats.*;


public class EdgeListBlockEntryMapper extends Mapper<LongWritable, Text, Text, BlockEntry>{

	Text outKey = new Text();
	BlockEntry outVal = new BlockEntry();


    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			//get configuration variables
			Configuration conf = context.getConfiguration();
			int sR = conf.getInt("SR",1);
			int sC = conf.getInt("SC",1);
			
			String name = conf.get("RESNAME");
			
			//input to change a 1-indexed matrix to a 0-indexed matrix
			int one = conf.getInt("ONE",0);
			
			//delimeter for the text edge file
			String delim = conf.get("DELIM");
			
			//assumes text in row<delim>col<delim>value format
			String line = value.toString();
			String[] parts = line.split(delim);
		    
			int row = Integer.parseInt(parts[0]) - one;
			int col = Integer.parseInt(parts[1]) - one;
			double valD = Double.parseDouble(parts[2]);
			
			//write
			String out = name + "," + String.valueOf(row/sR) + "," + String.valueOf(col/sC);
			outKey.set(out);
			outVal.set(row%sR,col%sC,valD);
			context.write(outKey,outVal);
		}
	}