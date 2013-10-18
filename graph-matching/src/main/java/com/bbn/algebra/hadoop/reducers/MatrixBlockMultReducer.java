package com.bbn.algebra.hadoop.reducers;

import java.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import com.bbn.fileformats.*;

	public class MatrixBlockMultReducer extends Reducer<Text,MatrixBlock,Text,MatrixBlock> {
	
		MatrixBlock left;
		MatrixBlock right;
		Text outKey;
		
		protected void setup(Context context) {
		
			Configuration conf = context.getConfiguration();
			
			int sRL = conf.getInt("SRL",0);
			int sCL = conf.getInt("SCL",0);
			int sRR = conf.getInt("SRR",0);
			int sCR = conf.getInt("SCR",0);
			
			left = new MatrixBlock(sRL,sCL);
			right = new MatrixBlock(sRR,sCR);
					
			outKey = new Text();
		}
		
	
        public void reduce(Text key, Iterable<MatrixBlock> values, Context context) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
	
			String lName = conf.get("LEFTNAME");
			String rName = conf.get ("RIGHTNAME");
			String resName = conf.get("RESNAME");
			
			boolean lTrans = conf.getBoolean("LTRANS", false);
			boolean rTrans = conf.getBoolean("RTRANS", false);
			
			double scalar = conf.getFloat("SCALAR", 1);
	
			for (MatrixBlock val : values) {
						
				if (val.name.equals(lName)) {
					left.set(val);
				}
				else if (val.name.equals(rName)) {
					right.set(val);
				}
			}
			
			String[] parts = key.toString().split(",");
			String out = resName + "," + parts[0] + "," + parts[1];
			outKey.set(out);
				
			//context.write(outKey,left.simpleMult(right,scalar, lTrans,rTrans, resName));
			context.write(outKey,left.dGemm(right,scalar, lTrans,rTrans, resName));
        }
    }