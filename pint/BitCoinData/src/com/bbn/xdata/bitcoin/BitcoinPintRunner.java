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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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


public class BitcoinPintRunner {
	public static DateFormat LONG_DF = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public static List<BitCoinTransaction> transactions;
	public static ObservableSet observables = new ObservableSet();
	public static List<Activity> activities = new ArrayList<Activity>();
	static List<Observation> observations = null;
	
	static Map<String,BitCoinTransaction> observationUriToTransaction = new HashMap<String,BitCoinTransaction>();
	
	public static void processActivityLine( String line ) {
//		System.out.println( "DEBUG: Line = '" + line + "'" );
		
		// ignore blank lines;
		if( line.trim().length() < 2 )
			return;
		
		// ignore comment lines
		if( line.trim().startsWith("#") )
			return;
		
		String[] split = line.split("\\|");
		String label = split[0];		
		int id = Integer.parseInt( label.substring( 1 ) );
		String uri = split[1].replace(" ", "_");
		
		Activity act = new Activity(id, uri, label);
		activities.add( act );
		
		// include the first item at split[1] as both the label and an observable
		for( int i=1; i<split.length; i++ ) {
			if( split[i].length() > 0 ) {
				String observableUri = split[i].replace(" ", "_");
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
		int startingAtLine = 3;
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

	public static Activity getActivityByUri( String aUri ) {
		for( Activity a : activities ) 
			if( a.getActivityURI().equals(aUri) ) { 
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
			
			if( precedesStr != null && precedesStr.length() > 0 ) {
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
			if( followsStr != null && followsStr.length() > 0 ) {
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
			if( opposesStr != null && opposesStr.length() > 0  ) {
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
		p.setProperty("pf.generators.hconsistent.spatial-weight", "1.0");
		p.setProperty("pf.generators.clusterfilter.percent-filled", "0.66");
		p.setProperty("process.max.distance.km", "350.0"); // in kilometers
		p.setProperty("process.max.timespan.ms", "96.0"); // in hours

		p.setProperty("pint.bitcoin.activities.filename", "data/activities.txt"); 
		p.setProperty("pint.bitcoin.procses.filename", "data/process.txt"); 
		p.setProperty("pint.bitcoin.transactions.filename", "user_edges.txt.gz"); 
		p.setProperty("pint.bitcoin.transactions.maxlines_toread", "16000000"); 
		p.setProperty("pint.bitcoin.transactions.largevaluefilter.minvalue", "2000"); 
		p.setProperty("pint.bitcoin.transactions.circularfilter.minvalue", "1000"); 
		p.setProperty("pint.bitcoin.transactions.complexfilter.minvalue", "100"); 
		
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
				// toReturn = new Properties();
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



	
	public static  List<Observation>  generateObservations(  List<BitCoinTransaction> transactions, List<TransactionFilter> filters ) {
		List<Observation> rv = new ArrayList<Observation>();
		
		for( BitCoinTransaction t : transactions ) {
			
			for( TransactionFilter f : filters ) {
				
				if( f.accept(t) ) {
					
					Date date = t.getDate();	
					String id = ""+t.getID();
					
					String uri = f.getClass().getSimpleName();  //  .getName();
					
					double lon = 0;
					double lat = 0;
					
					//long uid = Long.parseLong( record.get( TwitterDataColumn.user_id ) );      // might be needed later							
					//System.out.println( "]]] " + DF.format(date) + id + "," + "," + txt + "," + uid + "," + lon + "," + lat );
					
					// URI for this observation: concatenate the transaction ID and the observable URI
					String obsUri = uri + ":" + id;
					Observation obs = new Observation( obsUri, uri, lat, lon, date);
					obs.setLabel( "Obs_" + uri + "_" + id );
					rv.add( obs );
					observationUriToTransaction.put( obsUri, t);
					
				}
				
			}
			
		}
		
		return rv; 
		
	}
	
	
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws InvalidProcessException 
	 */
	public static void main(String[] args) throws IOException, ParseException, InvalidProcessException {
		System.out.println( "BitCoin Pint (Process Finder) Runner version built on " + Version.buildDate + " at " + Version.buildTime + "\n");
		
		String transactionsFilename = "user_edges.txt.gz";
		long maxLinesToRead = 20000000;

		
		String propsFilename = "data/pint-bitcoin.properties";
		String activitiesFilename = "data/activities.txt";
		String processFilename = "data/process.txt";		

//		String twitterFilename = "data/tweets-chunk-00000145.tsv.gz";
//		long twitterLinesToRead = 2000000;
//		long twitterStartAtLine = 1;

		if( args.length < 1 ) {
			System.out.println( "\nUsage:  java -jar bitcoin-pint.jar propertiesFile [activitiesFile [processFile [transactions-file [maxLinesToRead]]]]\n" );
			System.exit(0);
		}
		else {
			propsFilename = args[0];
		}
		
		
		System.out.println( "Starting ActivitiesLoader/ProcessFinder at " + (new Date()) + "\n" );
		
		
		Properties pintProps = getProperties( propsFilename );
		PintConfiguration config = new PintConfiguration( pintProps );		
		activitiesFilename = pintProps.getProperty( "pint.bitcoin.activities.filename", activitiesFilename );
		processFilename = pintProps.getProperty( "pint.bitcoin.procses.filename", processFilename );
		transactionsFilename = pintProps.getProperty( "pint.bitcoin.transactions.filename", transactionsFilename );
		maxLinesToRead = Long.parseLong( pintProps.getProperty( "pint.bitcoin.transactions.maxlines_toread", ""+maxLinesToRead ).trim() );


		// Overriding the properties from the command line
		//		
		if( args.length > 1 ) {
			System.out.println( ">>> Overriding parameters from command line:" );
			activitiesFilename = args[1];
			System.out.println( "\t Activities Def File = " + activitiesFilename );
			if( args.length > 2 ) {
				processFilename = args[2];
				System.out.println( "\t Process Def File    = " + processFilename );
				if( args.length > 3 ) {
					transactionsFilename = args[3];
					System.out.println( "\t Transaction File    = " + transactionsFilename );
					if( args.length > 4 ) {
						maxLinesToRead = Long.parseLong( args[4] );
						System.out.println( "\t Max Lines to Read   = " + maxLinesToRead );
					}
				}
			}
			System.out.println( );
		}
		
		System.out.println( ">>> Will use the following parameters:" );
		System.out.println( "\t Properties File = " + propsFilename );
		System.out.println( "\t Activities Def File = " + activitiesFilename );
		System.out.println( "\t Process Def File    = " + processFilename );
		System.out.println( "\t Transaction File    = " + transactionsFilename );
		System.out.println( "\t Max Lines to Read   = " + maxLinesToRead );

		
		System.out.println( "\n>>> Reading in activies from file " + activitiesFilename );		
		readActivitiesFile( activitiesFilename );
		
		System.out.println( "\n>>> Created " + activities.size() + " activities:" );
		for( Activity a : activities ) {
			System.out.println( "\t " + a.toString() );
		}

		System.out.println( "\n>>> Created " + observables.size() + " observables:" );		
		Iterator<String> it = observables.iterator(); 
		while( it.hasNext() ) {
			String observable = it.next();
			Activity act = observables.getActivityFor( observable );
			System.out.printf( "\t %1$-30s -->  %2$-30s \n", observable, act.toString() );
		}
		
		System.out.println( "\n>>> Reading in Processes from file " + processFilename + "\n" );		
		RnrmProcess process = readProcessFile( processFilename );
		
		System.out.println( "\n>>> Created process: \n" + process.toCsvString() );

		List<TransactionFilter> filters = new ArrayList<TransactionFilter>();
		filters.add( new IndividualValueFilter( Double.parseDouble( pintProps.getProperty( "pint.bitcoin.transactions.largevaluefilter.minvalue", "2000.0").trim() ) ) );
		filters.add( new TransactionToSelfFilter( Double.parseDouble( pintProps.getProperty( "pint.bitcoin.transactions.circularfilter.minvalue", "1000.0").trim()  ) ) );
		filters.add( new NumberOfElementsFilter( Integer.parseInt( pintProps.getProperty( "pint.bitcoin.transactions.complexfilter.minvalue", "100").trim()  ) ) );
		
		System.out.println( "\n>>> Reading a maximum of " + maxLinesToRead + " BitCoin transactions from: " + transactionsFilename );
				
		transactions = BitCoinTransaction.readTransactionFile( new File( transactionsFilename ), maxLinesToRead ); 

		System.out.println( "\n>>> Loaded " + transactions.size() + " Transactions.\n>>> Will Filter transactions with " + filters.size() + " filters:" );
		for( TransactionFilter f : filters ) {
			System.out.println( "\t" + f.toString() );
		}

		transactions = BitCoinTransaction.filterTransactions(transactions, 20000000, filters);

		System.out.println( "\n>>> Filtered down to " + transactions.size() + " Transactions." );
		
		
		
		observations = BitcoinPintRunner.generateObservations(transactions, filters);
		
		
		System.out.println( "\n>>> Generated " + observations.size() + " observations.\n" );
//		int i=0;
//		for( Observation obs : observations ) {
//			i++;
//			System.out.println( i + ". >>> " + obs.toCsvString() );
//			if( i >= 100 ) break;
//		}
//		System.out.println( "\n>>> Generated " + observations.size() + " observations.\n" );
//		System.exit(0);
		
		List<Binding> bindings = BindingGroup.createBindingList( process, observations, observables );
		BindingGroup bindingGroup = new BindingGroup( bindings );

		System.out.println( ">>> Number of Activity-Observation Bindings in Group " + bindingGroup.bindingCount() + " (" + bindings.size() + " raw bindings).\n" );

		System.out.println( "\n>>> Looking for Process instances...\n" );

		long startTime = System.currentTimeMillis();
		ProcessFinder processFinder = new ProcessFinder(process, bindingGroup, config);
		List<SolutionReport> reports = processFinder.find();
		Collections.sort(reports);
		long endTime = System.currentTimeMillis();

		System.out.println( "\n>>> Done looking for Process instances in " + (endTime-startTime) + " ms. Found " + reports.size() + " solutions.\n" );

		int count=0;
		List<SolutionReport> selectedReports = new ArrayList<SolutionReport>();
		for( SolutionReport report : reports ) {
			count++;
			if( isSolutionInteresting(report, count) ) {
				selectedReports.add( report );
			}
		}

		System.out.println( ">>> Found " + selectedReports.size() + " matching-user-id solutions.\n" );

		
		// Print solution only in case it matches certain conditions...
		System.out.println( ">>> Solution reports:\n" );
		
		int interestingCount = 0;
		count = 0;
		for( SolutionReport report : reports ) {			
			count++;

			System.out.println( "Report " + count + "." );
			boolean interesting = isSolutionInteresting(report, count); // this call prints out an info string with the type of user-id match found.
			System.out.println( report.toString() );
			
			boolean shouldPrint = interesting; //  || count <= 10;
			if( shouldPrint ) {
				interestingCount++;
				System.out.println( "Ordered Activity-Observation pairs: " );
				for( IBinding b : report.getOrderedBindings() ) {
					IObservation iobs = b.getObservation();	
					Activity a = getActivityByUri( b.getActivityUri() );
					if( iobs == null ) {
						System.out.println( "Act=" + a.getLabel() + " -- No Observation Bound. (binding: " + b.toString() + ")\n");
						continue;
					}
					
					BitCoinTransaction t = observationUriToTransaction.get( iobs.getUri() );
					
					System.out.println( "Act=" + a.getLabel() + " Obs=" + iobs.getLabel() + " :-- " + t.toString() );
						
				}
			}
			
			System.out.println( "-------------End Report " + count + "-----------------------\n" );			
		}
		
		System.out.println( ">>> Printed " + interestingCount + " matching-user-id solutions details.\n" );


		System.out.println( "\nDone with ActivitiesLoader/ProcessFinder at " + (new Date()) );

		
		
	}
	
	
	public static boolean isSolutionInteresting( SolutionReport report, int count ) {
		if( report == null )
			return false;
		
		boolean isInteresting = false;
		
		List<IBinding> matches = report.getOrderedBindings();
		if( matches.size() == 3 ) {
			IBinding ib1 = matches.get(0); // high value
			IBinding ib2 = matches.get(1); // obfuscated 
			IBinding ib3 = matches.get(2); // circular
			BitCoinTransaction t1 = null;
			BitCoinTransaction t2 = null;
			BitCoinTransaction t3 = null;
			if( ib1 != null && ib1.getObservation() != null ) {
				t1 = observationUriToTransaction.get( ib1.getObservation().getUri() ); // high_value
				if( t1 == null )
					System.err.println( "ERROR: could not find Transactions for observation " + ib1.getObservation().getUri() );
			}
			if( ib2 != null && ib2.getObservation() != null  ) {
				t2 = observationUriToTransaction.get( ib2.getObservation().getUri() ); // obfuscated
				if( t2 == null )
					System.err.println( "ERROR: could not find Transactions for observation " + ib2.getObservation().getUri() );
			}
			if( ib3 != null && ib3.getObservation() != null  ) {
				t3 = observationUriToTransaction.get( ib3.getObservation().getUri() ); // circular
				if( t3 == null )
					System.err.println( "ERROR: could not find Transactions for observation " + ib3.getObservation().getUri() );
			}
			
			if( ib1.getObservation() != null && ib2.getObservation() != null ) {
				if( t1 != null && t2 != null ) {
					// want two different transactions, not the same one
					if( t1.getID() != t2.getID() )
//					if( t1.isUserKeyInTargets( t2.getSourceUserKey() ) ) {
					if( t1.getHighestValueTargetUserKey() == t2.getSourceUserKey() ) {
						System.out.println( "\n[[[" + count + "]]] High value transaction (" + t1.getID() + ")" +
											" target user (" + t2.getSourceUserKey()  + ") was the source for an Obfuscated transaction (" +
											t2.getID() + ").\n" );
						isInteresting = true;
					}
				}
			}
			
			if( ib1.getObservation() != null && ib3.getObservation() != null ) {
				if( t1 != null && t3 != null ) {
					// want two different transactions, not the same one
					if( t1.getID() != t3.getID() )
//					if( t1.isUserKeyInTargets( t3.getSourceUserKey() ) ) {
					if( t1.getHighestValueTargetUserKey() == t3.getSourceUserKey() ) {
						System.out.println( "\n[[[" + count + "]]] High value transaction (" + t1.getID() + ")" +
											" target user (" + t3.getSourceUserKey()  + ") was the source for Circular transaction (" +
											t3.getID() + ").\n" );
						isInteresting = true;
					}
				}
			}
			
			if( ib2.getObservation() != null && ib3.getObservation() != null ) {
				if( t2 != null && t3 != null ) {
					// want two different transactions, not the same one
					if( t2.getID() != t3.getID() )
					if( t2.isUserKeyInTargets( t3.getSourceUserKey() ) ) {
						System.out.println( "\n[[[" + count + "]]] Obfuscated transaction (" + t2.getID() + ")" +
											" target user (" + t3.getSourceUserKey()  + ") was the source for Circular transaction (" +
											t3.getID() + ").\n" );
						isInteresting = true;
					}
				}
			}
			
		}
		else {
			System.err.println( "ERROR: wrong number of bindings for process report " + count + "." );
		}

		return isInteresting;		
	}
	
	

}
