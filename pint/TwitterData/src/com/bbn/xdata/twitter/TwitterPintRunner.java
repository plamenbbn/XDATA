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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.c2s2.pint.Activity;
import com.bbn.c2s2.pint.Binding;
import com.bbn.c2s2.pint.BindingGroup;
import com.bbn.c2s2.pint.IBinding;
import com.bbn.c2s2.pint.IObservation;
import com.bbn.c2s2.pint.ObservableSet;
import com.bbn.c2s2.pint.Observation;
import com.bbn.c2s2.pint.configuration.PintConfiguration;
import com.bbn.c2s2.pint.exception.InvalidProcessException;
import com.bbn.c2s2.pint.pf.ProcessFinder;
import com.bbn.c2s2.pint.pf.RnrmProcess;
import com.bbn.c2s2.pint.pf.SolutionReport;

public class TwitterPintRunner {

	
	public static ObservableSet observables = new ObservableSet();
	public static List<Activity> activities = new ArrayList<Activity>();
	public static List<Observation> observations = null;
	

	
	public static Properties getProperties() {
		Properties p = new Properties();
//		p.setProperty("pf.clusterer.agreement-threshold", "0.4");
//		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
//		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
//		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
//		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
//		p.setProperty("process.max.distance.km", "1.3");
//		p.setProperty("process.max.timespan.ms", "2.3");

		p.setProperty("pf.clusterer.agreement-threshold", "0.5");
		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
		p.setProperty("pf.generators.hconsistent.spatial-weight", "2.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
		p.setProperty("process.max.distance.km", "35.0"); // in kilometers
		p.setProperty("process.max.timespan.ms", "24.0"); // in hours

		return p;
	}

	public static Properties getProperties( String propFilename ) {
		Properties p = new Properties();
//		p.setProperty("pf.clusterer.agreement-threshold", "0.4");
//		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
//		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
//		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
//		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
//		p.setProperty("process.max.distance.km", "1.3");
//		p.setProperty("process.max.timespan.ms", "2.3");
		
		p.setProperty("pf.clusterer.agreement-threshold", "0.5");
		p.setProperty("pf.generators.hconsistent.num-solutions", "20");
		p.setProperty("pf.generators.hconsistent.hcon-threshold", "3.0");
		p.setProperty("pf.generators.hconsistent.spatial-weight", "2.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.4");
		p.setProperty("process.max.distance.km", "35.0"); // in kilometers
		p.setProperty("process.max.timespan.ms", "24.0"); // in hours

		p.setProperty("pint.twitter.activities.filename", "data/activities.txt"); 
		p.setProperty("pint.twitter.procses.filename", "data/process.txt"); 
		p.setProperty("pint.twitter.tweets.filename", "data/tweets-chunk-00000000.tsv.gz"); 
		p.setProperty("pint.twitter.tweets.maxlines_toread", "3000000"); 
		p.setProperty("pint.twitter.tweets.start_at_line", "1"); 
		p.setProperty("pint.twitter.filter.languages.accepted", "English"); 
		
		Properties toReturn;
		
		File propFile = new File( propFilename );
		if( propFilename==null || propFile==null || !propFile.exists() || !propFile.isFile() ) {
			System.err.println( "Error: Properties file " + propFilename + " does not exist or not a regular file." );
			System.out.println( ">>> Using Pre-defined Properties...\n" );
			toReturn = p;
		}
		else {
			InputStream inStream;
			try {
				inStream = new FileInputStream( propFile );
				//toReturn = new Properties();
				// Using statically defined properties as a baseline - extend/overwrite with the properties loaded from file...
				toReturn = p;
				toReturn.load(inStream);
				System.out.println( ">>> Loaded Properties File " + propFilename + " \n");
			} catch (FileNotFoundException e) {
				System.err.println( "Error: Properties file " + propFile.getAbsolutePath() + " not found." );
				e.printStackTrace();
				toReturn = p;
				System.out.println( ">>> Using Pre-defined Properties...\n" );
			} catch (IOException e) {
				System.err.println( "Error: IO Exception loading properties file " + propFile.getAbsolutePath() + " ...." );
				e.printStackTrace();
				toReturn = p;
				System.out.println( ">>> Using Pre-defined Properties...\n" );
			}
		}
		
		System.out.println( ">>> Will use the following PINT configuration:" );		
		Iterator iter = toReturn.keySet().iterator() ;
		while( iter.hasNext() ) {
			String key = (String)iter.next();
			String value = toReturn.getProperty(key);
			System.out.println( "\t" + key + " = " + value );
		}
		System.out.println();
		
		return toReturn;
	}

	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 * @throws InvalidProcessException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InvalidProcessException {
		
		System.out.println( "Twitter PINT (Process Finder) Runner version built on " + Version.buildDate + " at " + Version.buildTime + "\n");
		
		
		String propsFilename = "twitterdata.properties";
		
		String activitiesFilename = "data/activities.txt";
		String processFilename = "data/process.txt";		
		String twitterFilename = "data/tweets-chunk-00000145.tsv.gz";
		long twitterLinesToRead = 2000000;
		long twitterStartAtLine = 1;

		if( args.length < 3 ) {
			System.out.println( "\nUsage:  java -jar twitter-pint.jar  propertiesFile [activitiesFile [processFile [twitterFile/Dir [linesToRead [startAtLine]]]]]\n" );
			System.exit(0);
		}
		else {
			propsFilename = args[0];
		}
		
		System.out.println( "Starting ActivitiesLoader/ProcessFinder at " + (new Date()) + "\n" );
		
		Properties pintProps = getProperties( propsFilename );
		PintConfiguration config = new PintConfiguration( pintProps );		
		activitiesFilename = pintProps.getProperty( "pint.twitter.activities.filename", activitiesFilename );
		processFilename = pintProps.getProperty( "pint.twitter.procses.filename", processFilename );
		twitterFilename = pintProps.getProperty( "pint.twitter.tweets.filename", twitterFilename );
		twitterLinesToRead = Long.parseLong( pintProps.getProperty( "pint.twitter.tweets.maxlines_toread", ""+twitterLinesToRead ).trim() );
		twitterStartAtLine = Long.parseLong( pintProps.getProperty( "pint.twitter.tweets.start_at_line", ""+twitterStartAtLine ).trim() );		
		TwitterDataParser.LANGUAGES = pintProps.getProperty( "pint.twitter.filter.languages.accepted", TwitterDataParser.LANGUAGES ); 

		
		if( args.length > 1 ) {			
			System.out.println( ">>> Overriding parameters from command line:" );
			activitiesFilename = args[1];
			System.out.println( "\t Activities Def File = " + activitiesFilename );
			if( args.length > 2 ) {
				processFilename = args[2];
				System.out.println( "\t Process Def File    = " + processFilename );
				if( args.length > 3 ) {
					twitterFilename = args[3];
					System.out.println( "\t Twitter File/Dir   = " + twitterFilename );
					if( args.length > 4 ) {
						twitterLinesToRead = Long.parseLong( args[4] );
						System.out.println( "\t Twitter MaxLines to Read   = " + twitterLinesToRead );
						if( args.length > 5 ) {
							twitterStartAtLine = Long.parseLong( args[5] );
							System.out.println( "\t Twitter Start at Line   = " + twitterStartAtLine );
						}
					}
				}
			}
		}
		
		System.out.println( ">>> Will use the following parameters:" );
		System.out.println( "\t Activities Def File = " + activitiesFilename );
		System.out.println( "\t Process Def File    = " + processFilename );
		System.out.println( "\t Twitter File        = " + twitterFilename );
		System.out.println( "\t Lines to Read       = " + twitterLinesToRead );
		System.out.println( "\t Start at Line       = " + twitterStartAtLine );
		System.out.println( "\t Languages to accept =" + TwitterDataParser.LANGUAGES );

		System.out.println( "\n>>> Reading in activies from file " + activitiesFilename );		
		ActivitiesLoader.readActivitiesFile( activitiesFilename );
		
		activities = ActivitiesLoader.activities;
		observables = ActivitiesLoader.observables;
		
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
		RnrmProcess process = ActivitiesLoader.readProcessFile( processFilename );
		
		System.out.println( "\n>>> Created process: \n" + process.toCsvString() );
		
		long tweetsProcessed = 0;
		long tweetsFilteredOut = 0;
		long tweetsFilteredIn = 0;
		
		File file = new File( twitterFilename );
		if( ! file.exists() ) {
			System.out.println( "\nError: Could not find Twitter file/directory " + twitterFilename + ". Exiting.\n" );
			System.exit(-1);
		}
		if( file.isDirectory() ) {
			System.out.println( "\n>>> Generating Observations from Twitter Directory " + twitterFilename + " :\n" );		
			observations = new ArrayList<Observation>();
			for( File tfile : file.listFiles() ) { 
				System.out.println( "\t.. Processing Twitter file " + tfile.getAbsolutePath() + " ...\n" );		
				observations.addAll( TwitterDataParser.generateObservations( observables, tfile.getAbsolutePath(), twitterLinesToRead, twitterStartAtLine ) );
				//Adding up all tweets that were parsed and all that were filtered in/out
				tweetsProcessed += TwitterDataParser.tweetsProcessed;
				tweetsFilteredOut += TwitterDataParser.tweetsFilteredOut;
				tweetsFilteredIn += TwitterDataParser.tweetsFilteredIn;
			}			
		}
		else {
			System.out.println( "\n>>> Generating Observations from Twitter file " + twitterFilename + " ...\n" );		
			observations = TwitterDataParser.generateObservations( observables, twitterFilename, twitterLinesToRead, twitterStartAtLine );
			tweetsProcessed += TwitterDataParser.tweetsProcessed;
			tweetsFilteredOut += TwitterDataParser.tweetsFilteredOut;
			tweetsFilteredIn += TwitterDataParser.tweetsFilteredIn;
		}

		//display the number of tweets that were parsed and the number that were filtered in/out
		System.out.println( ">>> Total Tweets Read: " + tweetsProcessed + "  Tweets Filtered Out: " + tweetsFilteredOut + "  Tweets Accepted: " + tweetsFilteredIn + "\n");

		System.out.println( ">>> Generated " + observations.size() + " observations.\n" );
//		for( Observation o : observations ) {
//			System.out.println( "\t>> " + o.toCsvString() );
//		}
		
		List<Binding> bindings = BindingGroup.createBindingList( process, observations, observables );
		BindingGroup bindingGroup = new BindingGroup( bindings );

		System.out.println( ">>> Number of Activity-Observation Bindings in Group " + bindingGroup.bindingCount() + " (" + bindings.size() + " raw bindings).\n" );

		System.out.println( ">>> Looking for Process instances..." );

		long startTime = System.currentTimeMillis();
		ProcessFinder processFinder = new ProcessFinder(process, bindingGroup, config);
		List<SolutionReport> reports = processFinder.find();
		Collections.sort(reports);
		long endTime = System.currentTimeMillis();

		System.out.println( "\n>>> Done looking for Process instances in " + (endTime-startTime) + " ms. Found " + reports.size() + " solutions.\n" );

		System.out.println( ">>> Solution reports:\n" );
		int count=0;
		for( SolutionReport report : reports ) {
			count++;
			System.out.println( "Report " + count + "." );
			System.out.println( report.toString() );
			System.out.println( "Ordered observations: " );
			for( IBinding b : report.getOrderedBindings() ) {
				IObservation iobs = b.getObservation();				
				if( iobs == null ) {
					System.out.println( "Act=" + b.getActivityUri() + " -- No Observation Bound. (binding: " + b.toString() + ")");
					continue;
				}
				TwitterDataRecord rec = TwitterDataParser.observationUriToTwitterRecord.get( iobs.getUri() );
				System.out.println( "Act=" + b.getActivityUri() + " Obs=" + iobs.getLabel() + 
						" :-- [id:" + rec.get(TwitterDataColumn.id) + "] @" + rec.get(TwitterDataColumn.user_screen_name) +
						" [Location:" + rec.get(TwitterDataColumn.user_location) + "] " +
						"'" + rec.get(TwitterDataColumn.text) + "' [Language:" + rec.get(TwitterDataColumn.lang_primary) + "]" );
			}
			
			System.out.println( "-------------End Report " + count + "-----------------------\n" );
		}

		System.out.println( "\n>>> Done with Twitter PINT (process finder) Runner at " + (new Date()) );


	}


}
