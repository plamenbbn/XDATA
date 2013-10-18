package com.bbn.auction.giraph;

import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.*;
import java.util.Set;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.conf.Configuration;
import java.io.*;

import com.bbn.auction.giraph;

public class AuctionVertex extends Vertex<LongWritable, AuctionVertexValue, Writable, AuctionMessage> {

	double epsilon;
	AuctionMessage bid = new AuctionMessage();
	LongWritable to = new LongWritable();
	
	@Override
	public void compute(Iterable<AuctionMessage> messages) throws IOException {
		Context context = getContext();
		Configuration conf = context.getConfiguration();
		double epsilon = (double) conf.getFloat("EPSILON", 0);
		int max = conf.getInt("MAX",Integer.MAX_VALUE);
		
		if (epsilon == 0) {
			epsilon = Double.MIN_VALUE;
		}
		
		// //even supersteps are bidding steps
		// //odd supersteps are updating steps
		long superstep = getSuperstep();
		
		//System.out.printf("Step: %d, ID: %d Value: %s\n", superstep, getId().get(), getValue().toString());
		
		if (superstep == max) {
			voteToHalt();
		}
		else if (superstep % 2 == 0) {
				
			// /////////////update the benefits based on the messages//////////////////////
			System.out.println("\t starting");
			
			for (AuctionMessage message : messages) {
				if (message.getBid() != -Double.MAX_VALUE && message.getBid() != Double.MAX_VALUE) {
				
					System.out.printf("\t\told benefit %s\n", String.valueOf(getValue().getBenefit((int) message.getSender().get())));
				
					getValue().setBenefit((int) message.getSender().get(), getValue().getBenefit((int) message.getSender().get()) - message.getBid());
				
					System.out.printf("\t\tnew benefit %s\n",  String.valueOf(getValue().getBenefit((int) message.getSender().get())));
					
				}
			
				else if (message.getBid() == Double.MAX_VALUE) {
				
					System.out.printf("\t\told column %s\n", String.valueOf(getValue().getColOwned().toString()));
					
					getValue().setColOwned(message.getSender());
					
					System.out.printf("\t\tnew column %s\n", String.valueOf(getValue().getColOwned().toString()));
				}
				
				else {
				
					System.out.printf("\tunowned now\n");
					getValue().setColOwned(-1);
				}
					
			}
			
			System.out.println("updated prices");
			
		
			// //only bid where you don't have a column already
			if (getValue().getColOwned().get() == -1) {
			
				System.out.println("\t preparing for loop");
			
				// //should store a max and a max column - and then when we re-calculate test
				/////////////////////get Max /////////////////////////////////////////////
				double[] maxBenefit = {-Double.MAX_VALUE, -Double.MAX_VALUE};
				long[] maxIdx = {-1,-1};
				for (int i = 0; i < getValue().N; i++) {
				
					if (getValue().getBenefit(i) > maxBenefit[0]) {
						maxBenefit[1] = maxBenefit[0];
						maxBenefit[0] = getValue().getBenefit(i);
						maxIdx[1] = maxIdx[0];
						maxIdx[0] = (long) i;
					}
					else if (getValue().getBenefit(i) > maxBenefit[1]) {
						maxBenefit[1] = getValue().getBenefit(i);
						maxIdx[1] = (long) i;
					}	
				}
				
				// //System.out.printf("\tmax1: %s\n", String.valueOf(maxBenefit[0]));
				// //System.out.printf("\tmax2: %s\n", String.valueOf(maxBenefit[1]));
				// //System.out.printf("\tmax1: %s\n", String.valueOf(maxIdx[0]));
				// //System.out.printf("\tmax2: %s\n", String.valueOf(maxIdx[1]));
				
				// //System.out.println("got maxes");
				
				//////////////////get bid////////////////////////////////////////////////
				double bidValue = maxBenefit[0] - maxBenefit[1] + epsilon;
				
				System.out.printf("\tbid value:%s\n",String.valueOf(bidValue));
				
				System.out.printf("\tbid on:%s\n",String.valueOf(maxIdx[0]));
				
				System.out.println("Got bid");
				
				///////////////////send bid/////////////////////////////////////////////
				to.set(maxIdx[0]);
				bid.set(getId(), bidValue);
				sendMessage(to, bid);
				
				System.out.println("sent bid");
			}
			
			voteToHalt();
			System.out.println("voted to halt");
		}
		
		else {
		
			///////////////////////get the maximum bid
			double maxBid = -Double.MAX_VALUE;
			long maxIdx = -1;

			// //System.out.println("starting mesage loop");
			
			for (AuctionMessage message : messages) {
				if (message.getBid() > maxBid) {
				
					
				
					maxBid = message.getBid();
					maxIdx = message.getSender().get();
					
					System.out.printf ("\tnew Bid: %s\n", String.valueOf(maxBid));
					System.out.printf("\tnew winner: %s\n", String.valueOf(maxIdx));
				}
			}
			
			// //System.out.println("got hihest message");
			
			if (maxIdx != -1) {
			
				System.out.println("need to update");
			
			
			/////////////////send a message to the winner////////////////////////////
				to.set(maxIdx);
				bid.set(getId(),Double.MAX_VALUE);
				sendMessage(to,bid);
				
				System.out.printf("\tsending message to winner %s\n", String.valueOf(maxIdx));
				System.out.println("sent message to winner");
				
			////////////////send a message to the loser//////////////////////////////
				if (getValue().getRowOwnedBy().get() != -1) {
					bid.set(getId(),-Double.MAX_VALUE);
					sendMessage(getValue().getRowOwnedBy(),bid);
				
					System.out.printf("\tsending message to loser %s\n", String.valueOf(getValue().getRowOwnedBy()));
					System.out.println("sent message to loser");
				
				}
				
				///////////////////update the price///////////////////////////////////////
				getValue().setPrice(maxBid + getValue().getPrice());
				getValue().setRowOwnedBy(maxIdx);
				
				System.out.printf("\tset the price to: %s\n", String.valueOf(getValue().getPrice()));
				System.out.printf("\tset ownership to: %s\n", String.valueOf(getValue().getRowOwnedBy().toString()));
				
				System.out.println("updated the price");
					
			
			//////////////////send a message to all indicating the price change/////////
				for (int i = 0; i < getValue().N; i++) {
					to.set(i);
					bid.set(getId(),maxBid);
					sendMessage(to,bid);
					
					System.out.printf("\tsent priceChange: %s message to row %s\n", String.valueOf(maxBid), String.valueOf(i));
					
				}
				
				System.out.println("sent message to all");
			}
			
			voteToHalt();
			System.out.println("voted to halt");
		}
	}
}
