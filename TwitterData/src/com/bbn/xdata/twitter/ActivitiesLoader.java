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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.ObservableSet;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.SolutionReport;



public class ActivitiesLoader {
	
	public static ObservableSet observables = new ObservableSet();
	public static List<Activity> activities = new ArrayList<Activity>();
	static List<Observation> observations = null;
	
	public static void processActivityLine( String line ) {
//		System.out.println( "DEBUG: Line = '" + line + "'" );

		// ignore blank lines;
		if( line.trim().length() < 2 )
			return;
		
		// ignore comment lines
		if( line.trim().startsWith("#") )
			return;
		
		System.out.println( "DEBUG: Line = '" + line + "'" );
		
		String[] split = line.split("\\|");
		String label = split[0];		
		int id = Integer.parseInt( label.substring( 1 ) );
		String uri = "#" + split[1].replace(" ", "_");
		
		Activity act = new Activity(id, uri, label);
		activities.add( act );
		
		// include the first item at split[1] as both the label and an observable
		for( int i=1; i<split.length; i++ ) {
			if( split[i].length() > 0 ) {
				String observableUri = "#" + split[i].replace(" ", "_");
				observables.add( observableUri, act );
			}
		}
		
//		System.out.println( ">>> " + act.toString() );
	}
	
	public static void readActivitiesFile( String inputFilename ) {
		
		BufferedReader br=null;
		try {
			br = new BufferedReader( new InputStreamReader( new FileInputStream( inputFilename ),"UTF-8") );
		} catch (UnsupportedEncodingException e) {
			System.err.println( "Could not open input stream for buffered reading of the file " + inputFilename );
			e.printStackTrace( System.err );
		} catch (FileNotFoundException e) {
			System.err.println( "Could find input file " + inputFilename );
			e.printStackTrace( System.err );
		}
		if(null == br) {
			System.err.println( "Could not crate Buffered Reader to read data from file " + inputFilename + " Exiting." );
			System.exit(-1);
		}

		String line;
		int lineNum = 0;
		int linesRead = 0;
		int startingAtLine = 1;
		try {
			while( (line=br.readLine()) != null  ) {
				lineNum++;
				if( lineNum < startingAtLine ) {
					continue;
				}
				processActivityLine( line );
				linesRead++;
			}
			br.close();

		} catch (IOException e) {
			System.err.println( "IO Exception while reading activities file " + inputFilename + " Ignoring..." );
			e.printStackTrace();
		}
		
	}
	
	
	public static Activity getActivity( String label ) {
		return getActivity( Integer.parseInt( label.substring(1) ) );
	}
	public static Activity getActivity( int id ) {
		for( Activity a : activities ) 
			if( a.getID() == id ) { 
				return a; 
			}
		return null;
	}
	
	public static RnrmProcess readProcessFile( String filename ) throws FileNotFoundException, IOException {
		
		Properties model = new Properties();
		model.load( new FileInputStream(filename) );

		String processUri = model.getProperty("URI");
		String processLabel = model.getProperty("Label");
		String actStr = model.getProperty("Activities");
		String[] actIDs = actStr.split(",");
		
		if( processUri != null )
			processUri = processUri.trim().replace(" ", "_");
		System.out.println( "Process '" + processLabel + "' (" + processUri + ") with Activities {" + actStr + "}" );

		
		Map<Activity, Set<Activity>> happensBeforeMap = new HashMap<Activity, Set<Activity>>();
		Map<Activity, Set<Activity>> happensAfterMap = new HashMap<Activity, Set<Activity>>();
		Map<Activity, Set<Activity>> opposesMap = new HashMap<Activity, Set<Activity>>();

		for( String idStr : actIDs ) {
			Activity act = getActivity( idStr );
			if( act == null ) {
				System.err.println( "Could not find Activity " + idStr + " - Ignoring it..." );
				continue;
			}

			//
			// Create empty sets (precedes,follows,opposes) for each Activity, regardless of whether a list was specified in the model or not
			//
			Set<Activity> precedesSet = happensBeforeMap.get( act );
			if( precedesSet == null ) {
				happensBeforeMap.put( act, precedesSet = new HashSet<Activity>() );
			}
			Set<Activity> folowsSet = happensAfterMap.get( act );
			if( folowsSet == null ) {
				happensAfterMap.put( act, folowsSet = new HashSet<Activity>() );
			}
			Set<Activity> opposesSet = opposesMap.get( act );
			if( opposesSet == null ) {
				opposesMap.put( act, opposesSet = new HashSet<Activity>() );
			}
			
			String newLabel = model.getProperty( idStr + ".label" );
			if( newLabel != null )
				act.setLabel( newLabel );

			
			//
			// No fill in those lists with values from the model, if any
			//
			String precedesStr = model.getProperty( idStr + ".precedes" );
			String followsStr = model.getProperty( idStr + ".follows" );
			String opposesStr = model.getProperty( idStr + ".opposes" );
			
			if( precedesStr != null ) {
				System.out.println( act + " precedes " + precedesStr );
				for( String aID : precedesStr.split(",") ) {
					Activity a = getActivity( aID );
					if( a != null ) {
						precedesSet.add( a );
					}
					else {
						System.err.println( "WARNING: Could not find Activity " + aID + " - Ignoring it..." );
					}
				}
			}
			if( followsStr != null ) {
				System.out.println( act + " follows " + followsStr );
				for( String aID : followsStr.split(",") ) {
					Activity a = getActivity( aID );
					if( a != null ) {
						folowsSet.add( a );
					}
					else {
						System.err.println( "WARNING: Could not find Activity " + aID + " - Ignoring it..." );
					}
				}
			}
			if( opposesStr != null ) {
				System.out.println( act + " opposes " + opposesStr );
				for( String aID : opposesStr.split(",") ) {
					Activity a = getActivity( aID );
					if( a != null ) {
						opposesSet.add( a );
					}
					else {
						System.err.println( "WARNING: Could not find Activity " + aID + " - Ignoring it..." );
					}
				}
			}
			
		}
		

		RnrmProcess rv = new RnrmProcess(happensBeforeMap, happensAfterMap, opposesMap, processUri, processLabel);

		return rv;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 * @throws InvalidProcessException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InvalidProcessException {

		System.out.println( "ActivitiesLoader test class. Version built on " + Version.buildDate + " at " + Version.buildTime + "\n");
		
		
		String activitiesFilename = "data/activities.txt";
		String processFilename = "data/process.txt";		
		String twitterFilename = "data/tweets-chunk-00000145.tsv.gz";
		long twitterLinesToRead = 2000000;
		long twitterStartAtLine = 1;

		if( args.length < 2 ) {
			System.out.println( "\nUsage:  java -jar activities-loader.jar  activitiesFile processFile [twitterFile/Dir [linesToRead [startAtLine]]]\n" );
			System.exit(0);
		}
		else {
			activitiesFilename = args[0];
			processFilename = args[1];
			if( args.length > 2 ) {
				twitterFilename = args[2];
				if( args.length > 3 ) {
					twitterLinesToRead = Long.parseLong( args[3] );
					if( args.length > 4 )
						twitterStartAtLine = Long.parseLong( args[4] );
				}
			}
		}
		
		System.out.println( "Starting ActivitiesLoader test at " + (new Date()) + "\n" );
		
		System.out.println( ">>> Will use the following parameters:" );
		System.out.println( "\t Activities Def File = " + activitiesFilename );
		System.out.println( "\t Process Def File    = " + processFilename );
		System.out.println( "\t Twitter File        = " + twitterFilename );
		System.out.println( "\t Lines to Read       = " + twitterLinesToRead );
		System.out.println( "\t Start at Line       = " + twitterStartAtLine );
		
		System.out.println( "\n>>> Reading in activies from file " + activitiesFilename );		
		ActivitiesLoader.readActivitiesFile( activitiesFilename );
		
		System.out.println( "\n>>> Created " + activities.size() + " activities:" );
		for( Activity a : activities ) {
			System.out.println( "\t " + a.toString() );
		}

		System.out.println( "\n>>> Created " + observables.size() + " observables:" );		
		Iterator<String> it = observables.iterator(); 
		while( it.hasNext() ) {
			String observable = it.next();
			Activity act = observables.getActivityFor( observable );
			System.out.printf( "\t%1$-30s -->  %2$-30s \n", observable, act.toString() );
		}
		
		System.out.println( "\n>>> Reading in Processes from file " + processFilename + "\n" );		
		RnrmProcess process = readProcessFile( processFilename );
		
		System.out.println( "\n>>> Created process: \n" + process.toCsvString() );
		
		System.out.println( "\n>>> This program does not read or process the Twitter files. Please use TwitterPintRunner to do that." );		

		System.out.println( "\nDone with ActivitiesLoader test at " + (new Date()) + ".\n" );

	}

}
