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
import org.apache.hadoop.filecache.DistributedCache;
import java.net.URI;
import org.apache.hadoop.util.*;

import com.bbn.fileformats.*;
import com.bbn.algebra.hadoop.mappers.*;
import com.bbn.algebra.hadoop.reducers.*

public class MatrixBlockMult extends Configured implements Tool {

  public int run(String[] args) throws Exception {  
  
	Configuration conf = getConf();
  
	
	conf.setFloat("SCALAR",Float.parseFloat(args[3]));
	
	
	conf.setBoolean("LTRANS", Boolean.parseBoolean(args[4]));
	conf.setBoolean("RTRANS", Boolean.parseBoolean(args[5]));
	
	conf.setInt("NRL", Integer.parseInt(args[6]));
	conf.setInt("NCL", Integer.parseInt(args[7]));
	conf.setInt("NRR", Integer.parseInt(args[8]));
	conf.setInt("NCR", Integer.parseInt(args[9]));
	
	//set # of reducers
	conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[10]));
	
	//Get optional blocksize parameters
	if (args.length >= 12)
		conf.setInt("SRL", Integer.parseInt(args[11]));
		
	if (args.length >= 13)
		conf.setInt("SCL", Integer.parseInt(args[12]));
		
	if (args.length >=14)
		conf.setInt("SRR", Integer.parseInt(args[13]));
		
	if (args.length >=15)
		conf.setInt("SCR", Integer.parseInt(args[14]));
		
	conf.set("LEFTNAME", args[0]);
	conf.set("RIGHTNAME", args[1]);
	conf.set("RESNAME", args[2]);
		
	//heap space - should be entered with the -D format and not dealt with by the program.    
	conf.set("mapred.map.child.java.opts","-Xmx3G");
	conf.set("mapred.reduce.child.java.opts","-Xmx3G");
	
	//job
	Job job1 = new Job(conf, "MatrixBlockMult");
	job1.setJarByClass(MatrixBlockMult.class);
	
	// Map
	FileInputFormat.addInputPath(job1, new Path(args[0]));
	FileInputFormat.addInputPath(job1, new Path(args[1]));
	job1.setInputFormatClass(SequenceFileInputFormat.class);
    job1.setMapperClass(BlockMultiplicationGroupingMapper.class);
    job1.setMapOutputKeyClass(Text.class);
    job1.setMapOutputValueClass(MatrixBlock.class);
		 
	//Reduce		 
	job1.setReducerClass(MatrixBlockMultReducer.class);
	job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(MatrixBlock.class);
	FileOutputFormat.setOutputPath(job1, new Path(args[2]));
	job1.setOutputFormatClass(SequenceFileOutputFormat.class);
	//job1.setOutputFormatClass(TextOutputFormat.class);
		 
	return job1.waitForCompletion(false)? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	int res = ToolRunner.run(conf, new MatrixBlockMult(), args);
	System.exit(res);
  }
}

