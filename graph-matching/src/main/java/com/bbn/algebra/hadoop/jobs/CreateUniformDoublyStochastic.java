package com.bbn.algebra.hadoop.jobs;

import java.io.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.Job;

import com.bbn.fileformats.*;
import com.bbn.algebra.hadoop.mappers.*;
import com.bbn.algebra.hadoop.reducers.*

public class CreateUniformDoublyStochastic extends Configured implements Tool {

  public int run(String[] args) throws Exception {  
  
	Configuration conf = getConf();
  
	int N = Integer.parseInt(args[2]);
	conf.setInt("N",N);
	int sR = Integer.parseInt(args[3]);
	conf.setInt("SR", sR);
	int sC = Integer.parseInt(args[4]);
	conf.setInt("SC", sC);
	String delim = args[5];
	conf.set("DELIM",delim); 
	conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[6]));
	
	conf.set("RESNAME", args[1]);
		
	//heap space - should be entered with the -D format and not dealt with by the program.    
	conf.set("mapred.map.child.java.opts","-Xmx3G");
	conf.set("mapred.reduce.child.java.opts","-Xmx3G");
	
	//Create the File that We are mapping.

	//open the file in hdfs
	Path outFile = new Path(args[0]);
	FileSystem fs = FileSystem.get(conf);
	FSDataOutputStream out = fs.create(outFile);
	
	//write out an entry for the block
	int nR = N/sR + (N%sR > 0? 1: 0);
	int nC = N/sC + (N%sC > 0? 1: 0);
	
	for (int r = 0; r < nR; r++) {
		for (int c = 0; c < nC; c++) {
			out.writeUTF(String.valueOf(r) + delim + String.valueOf(c) + "\n");
		}
	}
		
	//close file
	out.close();

	//job
	Job job1 = new Job(conf, "CreateUniformDoubleStochastic");
	job1.setJarByClass(CreateUniformDoublyStochastic.class);
	
	// Map
	FileInputFormat.addInputPath(job1, outFile);
	job1.setInputFormatClass(TextInputFormat.class);
    job1.setMapperClass(UniformDoublyStochasticMapper.class);
		 
	//Reduce
	job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(MatrixBlock.class);
	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
	job1.setOutputFormatClass(SequenceFileOutputFormat.class);
	//job1.setOutputFormatClass(TextOutputFormat.class);
		 
	return job1.waitForCompletion(false)? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	int res = ToolRunner.run(conf, new CreateUniformDoublyStochastic(), args);
	System.exit(res);
  }
}

