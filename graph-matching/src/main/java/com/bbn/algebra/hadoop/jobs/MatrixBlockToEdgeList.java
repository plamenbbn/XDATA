package com.bbn.algebra.hadoop.jobs;

import java.io.*;
//import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.Partitioner;

import com.bbn.fileformats.*;
import com.bbn.algebra.hadoop.mappers.*;
import com.bbn.algebra.hadoop.reducers.*

public class MatrixBlockToEdgeList {
	
	public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
	conf.set("DELIM",args[2]);
	conf.setInt("One", Integer.parseInt(args[3]));
	
	//set # of reducers
	conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[4]));
	
	//heap space
	conf.set("mapred.map.child.java.opts","-Xmx3G");
	conf.set("mapred.reduce.child.java.opts","-Xmx3G");
	
	//job
	Job job1 = new Job(conf, "MatrixBlockToEdgeList");
	job1.setJarByClass(MatrixBlockToEdgeList.class);
	
	// Map
	FileInputFormat.setInputPaths(job1, new Path(args[0]));
	job1.setInputFormatClass(SequenceFileInputFormat.class);
    job1.setMapperClass(MatrixBlockEdgeListMapper.class);
	job1.setMapOutputKeyClass(Text.class);
    job1.setMapOutputValueClass(NullWritable.class);
    	 
	//Reduce		 
	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
	job1.setOutputFormatClass(TextOutputFormat.class);
		 
	job1.waitForCompletion(true);
    }
}

