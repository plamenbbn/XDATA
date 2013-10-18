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

package auction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.commons.cli.CommandLine;
import org.apache.giraph.combiner.FloatSumCombiner;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.edge.IntNullArrayEdges;
import org.apache.giraph.io.formats.*;
import org.apache.giraph.io.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.apache.giraph.job.GiraphJob;
import com.google.common.collect.Sets;
import org.apache.giraph.benchmark.*;
import org.apache.giraph.combiner.*;
import org.apache.giraph.edge.*;
import org.apache.giraph.conf.GiraphConstants;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;
import java.util.Set;

//import com.bbn.auction.giraph.*;
import auction.*;
import fileformats.*;



public class Auction extends Configured implements Tool {
 
        public int run(String[] args) throws Exception {
		
	    int workers = Integer.parseInt(args[4]);
		
        GiraphJob job = new GiraphJob(super.getConf(), getClass().getName());
	                 
        GiraphConfiguration conf = job.getConfiguration();		
		conf.setBoolean("giraph.useSuperstepCounters",false); 
		conf.setFloat("EPSILON", Float.parseFloat(args[2]));
		conf.setInt("MAX", Integer.parseInt(args[3]));

		conf.setWorkerConfiguration(workers, workers, 100.0f);	
			
		conf.setVertexInputFormatClass(IdDoubleMatrixVertexValueInputFormat.class);
		GiraphFileInputFormat.addVertexInputPath(conf, new Path(args[0]));
			
		conf.setVertexClass(AuctionVertex.class);
						
		conf.setVertexOutputFormatClass(EdgeListAuctionVertexOutputFormat.class);
		FileOutputFormat.setOutputPath(job.getInternalJob(), new Path(args[1]));
			
		if (job.run(true)) {
                return 0;
            }
		else {
                 return -1;
           }
        }
       
        public static void main(final String[] args) throws Exception {
               System.exit(ToolRunner.run(new Auction(), args));
        }
}