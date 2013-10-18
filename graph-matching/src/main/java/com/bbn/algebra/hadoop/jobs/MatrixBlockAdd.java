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

public class MatrixBlockAdd extends Configured implements Tool {


  public static class NoNameMapper extends Mapper <Text, MatrixBlock, Text, MatrixBlock> {
  
	 public void map(Text key, MatrixBlock value, Context context) throws IOException, InterruptedException {
		String[] parts = key.toString().split(",");
		String newKey = "X," + parts[1]+","+parts[2];
		
		context.write(new Text(newKey),value);
		}
	}

  public int run(String[] args) throws Exception { 
	Configuration conf = getConf();
  
	conf.setFloat("ALPHA", Float.parseFloat(args[3]));
	conf.setFloat("BETA", Float.parseFloat(args[4]));
	conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[5]));
	
	if (args.length >= 7)
		conf.setInt("SR", Integer.parseInt(args[6]));
	
	if (args.length >= 8)
		conf.setInt("SC", Integer.parseInt(args[7]));
		
	conf.set("LEFTNAME", args[0]);
	conf.set("RESNAME", args[2]);
	
	//heap space - again - should be passed with the -D option
	conf.set("mapred.map.child.java.opts","-Xmx3G");
	conf.set("mapred.reduce.child.java.opts","-Xmx3G");
	
	//job
	Job job1 = new Job(conf, "MatrixBlockAdd");
	job1.setJarByClass(MatrixBlockAdd.class);
	
	// No Map
	FileInputFormat.addInputPath(job1, new Path(args[0]));
	FileInputFormat.addInputPath(job1, new Path(args[1]));
	job1.setInputFormatClass(SequenceFileInputFormat.class);
	job1.setMapperClass(NoNameMapper.class);
    	 
	//Reduce
	job1.setReducerClass(MatrixBlockAddReducer.class);
	job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(MatrixBlock.class);
	FileOutputFormat.setOutputPath(job1, new Path(args[2]));
	job1.setOutputFormatClass(SequenceFileOutputFormat.class);
	//job1.setOutputFormatClass(TextOutputFormat.class);
		 
	return job1.waitForCompletion(false)? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	int res = ToolRunner.run(conf, new MatrixBlockAdd(), args);
	System.exit(res);
  }
}

