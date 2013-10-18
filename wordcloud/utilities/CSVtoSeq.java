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
import org.apache.hadoop.util.*;

public class CSVtoSeq extends Configured implements Tool {
	
	public static class CSVtoSeqMapper extends Mapper<LongWritable, Text, NullWritable, Text>{

		public Text outVal = new Text ();

		public void map(LongWritable key, Text value, Context context ) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
		
			String delim = conf.get("DELIM", "\t");
			int column = conf.getInt("COL",0);
			
			//prepare conditions array
			int numConditions = conf.getInt("NUMCOND",0);
			int cols[] = new int[numConditions];
			String terms[] = new String[numConditions];
			
			//get string valued conditions
			for (int i = 0; i < numConditions; i++) {
				String current = conf.get("COND"+i);
				cols[i] = Integer.parseInt(current.split(":")[0]);
				terms[i] = current.split(":")[1];
			}
					
			String[] row = value.toString().split(delim);
				
			if (row.length > column) {
				Boolean write = true;
				
				//test conditions
				for (int i = 0; i < numConditions && write; i++) {
					if (row.length > cols[i]) {
						if (! row[cols[i]].equals(terms[i])) {
							write = false;
						}
					}
				}
			
				//print
				if (write) {
					outVal.set(row[column]);
					context.write(NullWritable.get(), outVal);
				}
			}
		}
	}
	
	public int run(String[] args) throws Exception {  
		Configuration conf = new Configuration();
		
		//get params
		conf.set("DELIM", args[2]);
		conf.setInt("COL", Integer.parseInt(args[3]));
		
		//set # of reducers
		conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[4]));
		
		//check for conditions
		if (args.length > 5) {
			conf.setInt("NUMCOND", args.length - 5);
			
			//add condition
			for (int i = 5; i < args.length; i++) {
				conf.set("COND"+(i-5), args[i]);
			}
		}
		
		//heap space - this should be configurable
		conf.set("mapred.map.child.java.opts","-Xmx3G");
		conf.set("mapred.reduce.child.java.opts","-Xmx3G");
		
		//job
		Job job1 = new Job(conf, "CSVtoSeq");
		job1.setJarByClass(CSVtoSeq.class);
		
		// Map
		FileInputFormat.setInputPaths(job1, new Path(args[0]));
		job1.setInputFormatClass(TextInputFormat.class);
		job1.setMapperClass(CSVtoSeqMapper.class);
			 
		//Reduce		 
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));
		job1.setOutputKeyClass(NullWritable.class);
		job1.setOutputValueClass(Text.class);
		job1.setOutputFormatClass(SequenceFileOutputFormat.class);
			 
		return job1.waitForCompletion(true) ? 0 : 1;
    }
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int res = ToolRunner.run(conf, new CSVtoSeq(), args);
		System.exit(res);
	}
}

