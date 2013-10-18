package com.bbn.auction.giraph.*;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import classes.auction.*;
import org.apache.giraph.io.formats.*;
import java.io.IOException;

import com.bbn.auction.giraph.*;

public class IdDoubleMatrixVertexValueInputFormat extends
    TextVertexValueInputFormat<LongWritable, AuctionVertexValue, Writable, AuctionMessage> {
	
	//should be customizable
	private static final String delimiter = "\t";

	@Override
	public TextVertexValueReader createVertexValueReader (InputSplit split, TaskAttemptContext context) throws IOException {
		system.out.println("I'm here");
		return new DMTextVertexValueReader();
	}

 	protected class DMTextVertexValueReader extends
       TextVertexValueReaderFromEachLine {
	   
		@Override 
		protected LongWritable getId(Text line) throws IOException {
			String[] values = line.toString().split(delimiter);
			
			System.out.println("value" + String.valueOf(values.length));
			
			
			return new LongWritable(Long.parseLong(values[0]));
		}
	   
		@Override 
		protected AuctionVertexValue getValue(Text line) throws IOException {
			String[] values = line.toString().split(delimiter);
		
			AuctionVertexValue row = new AuctionVertexValue(values.length - 1, true);
		
			for (int i = 1; i < values.length; i++) {
				row.setBenefit(i-1,Double.parseDouble(values[i]));
			}
		
		return row;
		}
	}
 }