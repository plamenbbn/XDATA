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
 * 
 ******************************************************************************/

package com.bbn.xdata.twitter;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.bbn.c2s2.pint.ObservableSet;
import com.bbn.c2s2.pint.Observation;


public class TwitterDataParser {


	public static String propsFilename = "TwitterDataParser.props" ;
	public static DateFormat DF = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
	
	public static String LANGUAGES = "\\N,None,English,French,Spanish,German,Italian,Portuguese"; 
	
	private static TwitterDataSchema targetSchema = new TwitterDataSchema(
				TwitterDataColumn.created_at,
				TwitterDataColumn.id,
				TwitterDataColumn.text,
				TwitterDataColumn.user_id,
				TwitterDataColumn.user_screen_name,
				TwitterDataColumn.user_location,
				TwitterDataColumn.lang_primary,
				TwitterDataColumn.longitude,
				TwitterDataColumn.latitude
			);

	/*
	static {
		Properties props = new Properties();
		try {
			System.out.println( ">>> TwitterDataParser will load properies files: " + propsFilename + " ...\n" );
			props.load( new FileReader( propsFilename ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace( System.err );
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace( System.err );
			System.exit(-1);
		}
		LANGUAGES = props.getProperty( "twitter.languages.accepted", LANGUAGES );
		System.out.println( ">>> Languages to accept: \n\t" + LANGUAGES + "\n" );
	}
	*/
	
	
	public static void processLine( String line, PrintWriter out ) {
		out.println( line );
		
	}
		
	
	public static long tweetsProcessed = 0;
	public static long tweetsFilteredOut = 0;
	public static long tweetsFilteredIn = 0;
	
	public static TwitterDataRecord getRecord( String line ) {
		
		String[] split = line.split("\t");
		
		TwitterDataRecord record = null;
		record = targetSchema.createRecord( split );			
		// select the correct text field and update the record
		String text = split[TwitterDataColumn.text_translated.ordinal()];
		if( text.equalsIgnoreCase("\\N") || text.equals("None") )
			text = split[TwitterDataColumn.text_translit.ordinal()];
		if( text.equalsIgnoreCase("\\N") || text.equals("None") )
			text = split[TwitterDataColumn.text.ordinal()];
		record.set( TwitterDataColumn.text, text );
		
//		System.out.println( record.toCSVString() );
		
		return record;
	}


	public static List<Observation> getObservations( TwitterDataRecord record, ObservableSet observables ) throws ParseException {
		List<Observation> rv = new ArrayList<Observation>();

		String text = record.get( TwitterDataColumn.text );
		text = text.toLowerCase();
		text = text.replaceAll( "\"\'\\?\\.-+()@#:" , " ");
		
//		System.out.println( ">> Text To Search: '" + text + "'" );
//		System.out.print( "\t> " );
		
		Iterator<String> it = observables.iterator();
		while( it.hasNext() ) {
			String uri = it.next();
			String term = uri.substring(1).replace("_", " ");
			
			// insert blanks for simple tokeninzation
			if( ! term.startsWith("*") )
				term = " " + term;
			if( ! term.endsWith("*") )
				term = term + " ";
			
			// for now - ignore/remove wildcards 
			term = term.replace("*", "");
			
//			System.out.print( " '" + term + "'" );
			
			// simple search to see if this term is found anywhere in the string.
			if( text.indexOf(term) > 0 ) {				
				// Construct an observation here, based on the term's observable URI
				Date date = DF.parse( record.get( TwitterDataColumn.created_at ) );			
				String id = record.get( TwitterDataColumn.id );
				double lon = Double.parseDouble( record.get( TwitterDataColumn.longitude ) );
				double lat = Double.parseDouble( record.get( TwitterDataColumn.latitude ) );
				//long uid = Long.parseLong( record.get( TwitterDataColumn.user_id ) );      // might be needed later							
				//System.out.println( "]]] " + DF.format(date) + id + "," + "," + txt + "," + uid + "," + lon + "," + lat );
				
				// URI for this observation: concatenate the tweet ID and the observable URI
				String obsUri = uri + ":" + id;
				Observation obs = new Observation( obsUri, uri, lat, lon, date);
				obs.setLabel( "Obs_" + term.trim() + "_" + id );
				rv.add( obs );
			}
		}
//		System.out.println( " <." );
		
//		System.out.println( "Generated " + rv.size() + " observations." );
//		for( Observation o : rv ) {
//			System.out.println( "\t>> " + o.toCsvString() );
//		}
		return rv;
	}

	
	public static Map<String,TwitterDataRecord> observationUriToTwitterRecord = new HashMap<String,TwitterDataRecord>();
	
	public static List<Observation> generateObservations( ObservableSet observables, String filename, long linesToRead, long startingAtLine ) throws ParseException {
		List<Observation> rv = new ArrayList<Observation>();
		
		InputStream is=null;
		try {
			is = new FileInputStream(filename);
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file '" + filename + "'.  Exiting." );
			System.exit(-1);
		}
		if(null!=is && filename.endsWith(".gz")) {
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
		
		System.out.println( "Will read " + linesToRead + " lines from file " + filename + " starting at line " + startingAtLine );
		
		HashMap<String,Long> languagesFiltered = new HashMap<String,Long>();
		
		long lineNum = 0;
		long linesRead = 0;
		long linesFilteredOut = 0;
		long linesFilteredIn = 0;
		String line;
		try {
			while( (line=br.readLine()) != null  && linesRead < linesToRead ) {
				lineNum++;
				if( lineNum < startingAtLine ) {
					continue;
				}
				linesRead++;
				TwitterDataRecord record = getRecord( line );
				if( record != null ) {
					String lang = record.get(TwitterDataColumn.lang_primary);
					
					// filter for languages here - only common languages are admitted
					if( lang != null && LANGUAGES.toLowerCase().contains( lang.toLowerCase() ) ) {
						List<Observation> obsFromRecord = getObservations( record, observables );
						linesFilteredIn++;
						rv.addAll( obsFromRecord );
						// map observations to records for later use
						for( Observation obs : obsFromRecord ) {
							observationUriToTwitterRecord.put( obs.getUri(), record );
						}						
					}
					else {
						linesFilteredOut++;
						lang = "*" + lang + "_FilterdOut";
					}

					if( lang != null ) {
						if( languagesFiltered.get(lang) == null )
							languagesFiltered.put(lang, new Long(1));
						else
							languagesFiltered.put(lang, languagesFiltered.get(lang)+1);
					}
				
				}
				if( linesRead % 10000 == 0 ) {
					System.out.println( "Processed " + linesRead + " lines from " + filename );
				}
			}
			br.close();
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println( "\nTweets filtered out: " + linesFilteredOut + "  Tweets filtered in: " + linesFilteredIn );
		System.out.println( "Languages processed: " );
		for( String lang : languagesFiltered.keySet() ) {
			System.out.println( "\t" + lang + " : " + languagesFiltered.get(lang) );
		}
		
		System.out.println( "\nDone. Found " + rv.size() + " observations in " + linesRead + " Tweets. in " + filename + "\n" );
		
		tweetsProcessed = linesRead;
		tweetsFilteredOut = linesFilteredOut;
		tweetsFilteredIn = linesFilteredIn;
		
		return rv;
	}
	
	
	
	
	

	public static void skipLines( BufferedReader br, long linesToSkip ) {
		long lineNum = 0;
		try {
			while( lineNum < linesToSkip  &&  null != br.readLine() ) {
				lineNum++;
			}
		} catch (IOException e) {
			e.printStackTrace( System.err );
		}
	}
	
	public static boolean parseChunk( BufferedReader br, long linesToRead, PrintWriter output ) {
		long linesRead = 0;
		String line = null;;
		try {
//			output.println( TwitterDataColumn.toTSVString() );
//			output.flush();
			while( linesRead < linesToRead && null != (line=br.readLine()) ) {
				processLine( line, output );
				output.flush();
				linesRead++;
				if( 0 == linesRead % 10000 ) {
					System.err.println( "Processed " + linesRead + " tweets." );
				}
			}
			return line != null;
		} catch (IOException e) {
			e.printStackTrace( System.err );
			return false;
		}
	}
	
	
	public static InputStream getInputStream( String filename ) {
		InputStream is=null;
		try {
			is = new FileInputStream(filename);
		} catch (FileNotFoundException e1) {
			System.err.println("Could not find file '" + filename + "'.  Exiting." );
			System.exit(-1);
		}
		if(null!=is && filename.endsWith(".gz")) {
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

		return is;
	}
	
	public static BufferedReader getReader( InputStream is, String filename ) {
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
	
	public static PrintWriter getOutputWriter( String outFilename, boolean gZip, boolean multiple, int chunkNumber ) {
		
		OutputStream os = null;
		try {
			if( null == outFilename || outFilename.equals("System.out") ) {
				os = System.out;
			}
			else {
				if( multiple ) {
					outFilename += "-" + String.format( "%08d", chunkNumber );
				}
				outFilename += (gZip ? ".tsv.gz" : ".tsv");
				os = new FileOutputStream( outFilename );
			}
			if( gZip ) {
				os = new GZIPOutputStream( os, 256000 );
			}			
			System.err.println( "Created a new output stream: " + outFilename ); 
		} catch (FileNotFoundException e) {
			e.printStackTrace( System.err );
			System.err.println( "Could not crate output file for " + outFilename + " Exiting." );
			System.exit(-1);
		} catch (IOException e) {
			System.err.println( "IO Exception trying to crate output file for " + outFilename + " GZip = " + gZip + " Exiting.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		if( null == os ) {
			System.err.println( "Could not crate output stream for " + outFilename + " Exiting." );
			System.exit(-1);			
		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter( new OutputStreamWriter( os, "UTF-8" ), true );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace( System.err );
			System.err.println( "Unsupported character encoding 'UTF-8' for file " + outFilename + " Exiting." );
			System.exit(-1);
		}
		
		if( null == pw ) {
			System.exit( -1 );
		}
		
		return pw;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {

		if( args.length < 1 || args[0].length()==0 ) {
			System.err.println( "\nUsage:" + 
		                        "\n  java TwitterDataParser TwitterDataFilename[.gz] [maxLineCount] [startingAtLine#(1-based)] [outputFileName] [-gz] [-loop]" +
					            "\n\t  Options order must be preserved; later options must be preceded by earlier options.\n" );			
			System.exit(0);
		}

		String filename = args[0];
		InputStream is = getInputStream( filename );

		BufferedReader br = getReader( is, filename );

		
		long linesToRead = 1000;
		if( args.length > 1 )
			linesToRead = Long.parseLong(args[1]);
		if( linesToRead < 1 ) {
			System.err.println( "Cannot read only " + linesToRead + " lines. Exiting." );
			System.exit(-1);
		}
		
		long startingAtLine = 1;
		if( args.length > 2 )
			startingAtLine = Long.parseLong(args[2]);
		
		String outFilename = "System.out";		
		boolean gZip = false;
		boolean loop = false;
		if( args.length > 3 ) {			
			outFilename = args[3];			
			gZip = ( args.length > 4 ) && args[4].equalsIgnoreCase("-gz");			
			loop = ( args.length > 5 ) && args[5].equalsIgnoreCase("-loop");			
		}

		
		System.err.println( "Will read " + linesToRead + " lines from file " + filename + " starting at line " + startingAtLine );
		System.err.println( "Output will go to " + outFilename + " (GZip=" + gZip + ")" );
		

		skipLines( br, startingAtLine-1 );
		
		PrintWriter pw = getOutputWriter( outFilename, gZip, loop, 0 );
		boolean moreToRead = true;
		int chunkNumber = 0;
		while( moreToRead ) {
			moreToRead = parseChunk( br, linesToRead, pw );
			moreToRead = loop && moreToRead;
			System.err.println( "" + (new Date(System.currentTimeMillis())) + "  -- Processed chunk " + chunkNumber + ". More to read = " + moreToRead );
			pw.flush();
			pw.close();
			chunkNumber++;
			if( moreToRead )
				pw = getOutputWriter( outFilename, gZip, loop, chunkNumber );
		}
		
		try { br.close(); }
		catch( IOException e ) { e.printStackTrace( System.err );	}
		
		System.err.println( "Done." );		

		
	}

}
