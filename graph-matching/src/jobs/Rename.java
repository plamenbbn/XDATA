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

import mappers.*;
import reducers.*;
import fileformats.*;

public class Rename extends Configured implements Tool {

  public static class RenameMapper extends Mapper <Text, MatrixBlock, Text, MatrixBlock> {
  
	 public void map(Text key, MatrixBlock value, Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String newName = conf.get("RESNAME");
		
		value.name = newName;
		
		String[] parts = key.toString().split(",");
		key.set(newName + "," + parts[1] + "," + parts[2]);
		
		context.write(key,value);
		}
	}

  public int run(String[] args) throws Exception { 
	Configuration conf = getConf();
  
	conf.set("RESNAME", args[1]);

	//job
	Job job1 = new Job(conf, "Rename");
	job1.setJarByClass(Rename.class);
	
	// No Map
	FileInputFormat.addInputPath(job1, new Path(args[0]));
	job1.setInputFormatClass(SequenceFileInputFormat.class);
	job1.setMapperClass(RenameMapper.class);
    	 
	//Reduce
	job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(MatrixBlock.class);
	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
	job1.setOutputFormatClass(SequenceFileOutputFormat.class);
		 
	return job1.waitForCompletion(true)? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	int res = ToolRunner.run(conf, new Rename(), args);
	System.exit(res);
  }
}

