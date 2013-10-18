/*
# DARPA XDATA licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with 
# the License.  You may obtain a copy of the License at 
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and  
# limitations under the License.
#
# Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
#
*/

package jobs;

import java.io.*;
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

//import com.bbn.fileformats.*;
import fileformats.*;
//import com.bbn.algebra.hadoop.mappers.*;
import mappers.*;
//import com.bbn.algebra.hadoop.reducers.*
import reducers.*;

public class MatrixBlockSum extends Configured implements Tool {

  public int run(String[] args) throws Exception { 
	Configuration conf = getConf();
  
	conf.setInt("mapred.reduce.tasks",Integer.parseInt(args[2]));
	
	if (args.length >= 4)
		conf.setInt("SR", Integer.parseInt(args[3]));
	
	if (args.length >= 5)
		conf.setInt("SC", Integer.parseInt(args[4]));
		
	conf.set("RESNAME", args[1]);
	
	//heap space - again - should be passed with the -D option
	conf.set("mapred.map.child.java.opts","-Xmx3G");
	conf.set("mapred.reduce.child.java.opts","-Xmx3G");
	
	//job
	Job job1 = new Job(conf, "MatrixBlockSum");
	job1.setJarByClass(MatrixBlockSum.class);
	
	// No Map
	FileInputFormat.addInputPath(job1, new Path(args[0]));
	job1.setInputFormatClass(SequenceFileInputFormat.class);
    	 
	//Reduce		 
	job1.setCombinerClass(MatrixBlockSumReducer.class);
	job1.setReducerClass(MatrixBlockSumReducer.class);
	job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(MatrixBlock.class);
	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
	job1.setOutputFormatClass(SequenceFileOutputFormat.class);
	//job1.setOutputFormatClass(TextOutputFormat.class);
		 
	return job1.waitForCompletion(false)? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	int res = ToolRunner.run(conf, new MatrixBlockSum(), args);
	System.exit(res);
  }
}

