/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
 ******************************************************************************/


package com.bbn.xdata.bitcoin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.bbn.c2s2.pint.testdata.chart.ScatterPlot;



public class BitCoinTransaction {
	
	public static DateFormat LONG_DF = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	public static DateFormat SHORT_DF = new SimpleDateFormat( "yyyyMMddHHmmss" );

	private long transactionKey;
	private Date date;
	
	public static class TransactionElement {
		public TransactionElement( long from, long to, long dateTime, double val ) {
			fromUserKey = from;
			toUserKey = to;
			value = val;
			dateTimeMillis = dateTime;
		}
		BitCoinTransaction transaction;
		int subID;
		long fromUserKey;
		long toUserKey;
		double value;	
		long dateTimeMillis;
		public String getID() { return (transaction==null) ? ("null:" + subID) : (transaction.transactionKey + ":" + subID); }
	}
	
	private ArrayList<TransactionElement> elements;
	private ArrayList<Long> inputTransactionKeys;
		
	
	private BitCoinTransaction( ) {
		elements = new ArrayList<TransactionElement>();
		inputTransactionKeys = new ArrayList<Long>();
	}

	public BitCoinTransaction( long tid, Date on ) {
		this();
		this.transactionKey = tid;
		this.date = on;
	}
	
	public void addElement( TransactionElement e ) {
		e.subID = elements.size();
		e.transaction = this;
		elements.add( e );
	}

	public Iterator<TransactionElement> elements() {
		return elements.iterator();
	}

	public int elementCount() {
		return elements.size();
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public long getID() {
		return this.transactionKey;
	}
	
	public long getSourceUserKey() {
		if( elements.size() < 1 )
			return 0;
		return elements.get(0).fromUserKey;
	}
	
	public long getHighestValueTargetUserKey() {
		double highest = -1;
		long userKey = 0;
		for( TransactionElement e : elements ) {
			if( e.value > highest ) {
				highest = e.value;
				userKey = e.toUserKey;
			}
		}
		return userKey;
	}
	
	public boolean isUserKeyInTargets( long userKey ) {
		for( TransactionElement e : elements ) {
			if( e.toUserKey == userKey )
				return true;
		}
		return false;
	}

	public boolean isUserKeyInSources( long userKey ) {
		for( TransactionElement e : elements ) {
			if( e.fromUserKey == userKey )
				return true;
		}
		return false;
	}

	
	public String toString( ) {
		String rv = "Transaction " + this.transactionKey + " on " + LONG_DF.format( this.date ) + "\n";
		for( TransactionElement e : elements ) {
			rv += "\t" + " [" + e.getID() + "]  $" + String.format("%1$8.6f", e.value) + " " + e.fromUserKey + " -> " + e.toUserKey + "\n";
		}
			
		return rv;
	}
	
	
	
	public static BufferedReader getReader( File file ) {
		
		String filename = file.getAbsolutePath();
		InputStream is=null;
		
		try {
			is = new FileInputStream( file );
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file '" + filename + "'.  Exiting." );
			System.exit(-1);
		}
		if( filename.endsWith(".gz") ) {
			GZIPInputStream gis = null;
			try {
				gis = new GZIPInputStream(is);
			} catch(IOException e) {
				System.err.println( "Caught an IO exception while trying to open '" + filename + "'.  The file is missing or empty.\n" + e );
				System.err.println( "Exiting." );
				System.exit(-1);
			}
			is = gis;
		}
		
		if(null == is) {
			System.err.println("Could not create input stream for file " + filename + " Exiting." );
			System.exit(-1);
		}

		BufferedReader br=null;
		try {
			br = new BufferedReader( new InputStreamReader(is,"UTF-8") );
		} catch (UnsupportedEncodingException e1) {
			System.err.println( "Could not open input stream for buffered reading the file " + filename );
			e1.printStackTrace( System.err );
		}

		if(null == br) {
			System.err.println( "Could not crate Buffered Reader to read data from file " + filename + " Exiting." );
			System.exit(-1);
		}

		return br;
	}

	public static List<BitCoinTransaction> filterTransactions( List<BitCoinTransaction> transactions, long maxToProcess, List<TransactionFilter> filters ) {
		if( maxToProcess < 0 )
			maxToProcess = Long.MAX_VALUE;

		System.out.println( "\n>>> Filtering " + Math.min(transactions.size(), maxToProcess) + " transactions with " + filters.size() + " filters..." );

		ArrayList<BitCoinTransaction> filtered = new ArrayList<BitCoinTransaction>();
		long[] filterCounts = new long[ filters.size() ];
		long transCount = 0;
		for( BitCoinTransaction t : transactions ) {
			if( transCount >= maxToProcess )
				break;
			for( TransactionFilter f : filters ) {
				if( f.accept(t) ) {
					filtered.add( t );
					filterCounts[ filters.indexOf(f) ]++;
					break;
				}
			}
			transCount++;
		}
		
		for( TransactionFilter f : filters ) {
			System.out.println( "\t" + f.toString() + " -> count = " + filterCounts[ filters.indexOf(f)] );
		}
		
		return filtered;
	}
		
	
	public static List<BitCoinTransaction> readTransactionFile( File tFile, long maxToRead ) throws IOException, ParseException {
		
		ArrayList<BitCoinTransaction> list = new ArrayList<BitCoinTransaction>();
		
		BufferedReader br = getReader( tFile );
		
		String line;
		long transCount = 0;
		long transElementCount = 0;
		while( null != (line=br.readLine()) ) {
			String[] split = line.split(",");			
			long tid = Long.parseLong( split[0] );
			long fromKey = Long.parseLong( split[1] );
			long toKey = Long.parseLong( split[2] );
			Date date = SHORT_DF.parse( split[3] );
			double value = Double.parseDouble(split[4]);

			BitCoinTransaction t = null;
			if( tid <= list.size() )
				t = list.get((int)(tid-1));
			if( null == t ) {
				if( transCount >= maxToRead )
					break;
				
				transCount++;

				t = new BitCoinTransaction( tid, date );
				list.add(t);
				if( transCount % 100000 == 0 )
					System.out.println( "Read " + transCount + " transactions (" + list.size()  + ") and " + transElementCount + " elements from file " + tFile.getAbsolutePath() );
			}
			
//			testData[0][(int)transElementCount] = (double)fromKey; 
//			testData[1][(int)transElementCount] = (double)toKey; 
			
			transElementCount++;
			TransactionElement e = new TransactionElement( fromKey, toKey, date.getTime(), value );
			t.addElement(e);
			
		}
		
		System.out.println( "Done Reading " + transCount + " transactions (" + list.size()  + ") and " + transElementCount + " elements from file " + tFile.getAbsolutePath() );
		
		return list;
	}
	
	
	static double[][] testData;

	
	public static void main( String[] args ) throws IOException, ParseException {
		
		String filename = "user_edges.txt.gz";
		long maxCount = 20000000;

		if( args.length < 1 ) {
			System.out.println( "Usage:  java -jar bitcoin-transactions.jar  <user_edges_filename>  [<max_transactions_to_read>] " );
		}
		else {
			filename = args[0];
		}
		if( args.length > 1 ) {
			maxCount = Long.parseLong(args[1]);
		}

		testData = new double[2][(int)maxCount*7];

		List<BitCoinTransaction> tList = readTransactionFile( new File( filename ), maxCount ); 
		
		TransactionFilter f1 = new IndividualValueFilter( 20000.0 );  // minimum of $20K 
		TransactionFilter f2 = new TransactionToSelfFilter( 1000.0 ); // minimum of $1,000 transferred to self
		TransactionFilter f3 = new NumberOfElementsFilter( 50 ); // transactions with 50 or more elements
		
		int countSelf = 0;
		int countLarge = 0;
		int countComplex = 0;
		for( BitCoinTransaction t : tList ) {
			if( f1.accept( t ) ) {
				System.out.println( "LARGE " + t );
				countLarge++;
			}
			if( f2.accept( t ) ) {
				//System.out.println( "TO_SELF " + t );
				countSelf++;
			}
			if( f3.accept(t) ) {
				countComplex++;
			}
		}
		
		System.out.println( "\nTOTAL = " + tList.size() + "\nLARGE = " + countLarge  + 
							"\nTO_SELF = " + countSelf + "\nCOMPLEX = " + countComplex ); 
		
		
		ScatterPlot demo = new ScatterPlot( "From-To User Plot of BitCoin Transactions", "From User ID", "To User ID", testData );
		demo.toJpgFile( "from-to-plot.jpg", 6000, 4000);
		//demo.toScreen();

		
	}
}
