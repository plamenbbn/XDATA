package com.bbn.auction.giraph.*;

import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import java.io.IOException;

import com.bbn.auction.giraph.*;

public class EdgeListAuctionVertexOutputFormat extends TextVertexOutputFormat<LongWritable, AuctionVertexValue, Writable> {

  /** Specify the output delimiter */
  public static final String LINE_TOKENIZE_VALUE = "output.delimiter";
  /** Default output delimiter */
  public static final String LINE_TOKENIZE_VALUE_DEFAULT = ",";
  
 
  @Override
  public TextVertexWriter createVertexWriter(TaskAttemptContext context) {
    return new AuctionValueVertexWriter();
  }
  
  protected class AuctionValueVertexWriter extends TextVertexWriterToEachLine {
    /** Saved delimiter */
    private String delimiter;
   
    @Override
    public void initialize(TaskAttemptContext context) throws IOException,
        InterruptedException {
      super.initialize(context);
      delimiter = getConf().get(
          LINE_TOKENIZE_VALUE, LINE_TOKENIZE_VALUE_DEFAULT);
    }

    @Override
    protected Text convertVertexToLine(Vertex<LongWritable, AuctionVertexValue, Writable, ?> vertex)
      throws IOException {
		Text line = new Text(vertex.getId().toString() + delimiter + vertex.getValue().getColOwned().toString() + delimiter + "1");
		System.out.println(vertex.getId().toString() + delimiter + vertex.getValue().getColOwned().toString() + delimiter + "1");
		return line;
    }
  }
}