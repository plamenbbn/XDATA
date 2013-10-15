

PINT Execution Wrapper for BitCoin Data


I. Quick Start

Build the BitCoinData project as an executable JAR file (let's name it "bitcoin-pint.jar"), packaged with all dependencies. This project depends on:

	1. the process-alignment project (the main PINT algorithm implementation)
	2. the PintTestData project (testing and logging dependencies)
	
Assuming the bitcoin-pint.jar file is in the root BitCoinData project directory, you can then run Bitcoin PINT as follows:

> java -jar bitcoin-pint data/pint-bitcoin.properties 

This should run out-of-the-box and will use the default set of parameters and an input file that contains a GZipped version of the BitCoin transaction data.
 

II. Building the project

We recommend building the project in Eclipse. After cloning the Git repository and importing the project from the Git working directory into Eclipse, 
make sure you add the two dependencies listed above to the project's  Build Path. Go to:
	Project > Properties > Java Build Path > Project tab > Add 
Then select the "process-alignment" and "PintTestData" projects as dependencies in the Java Build Path. 

Next, you can build and export the project as an executable JAR by selecting the BitCoinData project and going to:
	File > Export > Runnable Jar File > Next > 
		- select BitCoinPintRunner Launch Configuration 
		- type the output JAR filename in Export Destination, such as "bitcoin-pint.jar"
		- select "Package required libraries into generated JAR"

The resulting file should be a self-contained executable JAR file that you can run as shown above.    


III. Configuration

The execution is configured using a Java properties file. A default configuration is provided in the file data/pint-bitcoin.properties

The following options are available:

1. PINT Configuration

The following PINT process matching algorithm parameters are available. 
We recommend leaving those parameters as-is because they require more advanced knowledge of the PINT algorithm.  
  
	pf.clusterer.agreement-threshold=0.5
	pf.generators.hconsistent.num-solutions=20
	pf.generators.hconsistent.hcon-threshold=3.0
	pf.generators.hconsistent.spatial-weight=1.0
	pf.generators.clusterfilter.percent-filled=0.66

There are two parameters that impose spatial and temporal constraints on the process matches that PINT discovers. 
Those can be changed easily, in order to explore different conditions. Note the units are kilometers and hours, repsectively:

	# Max distance in kilometers (not used currently)
	process.max.distance.km=350.0
	# Max timespan in hours
	process.max.timespan.ms=96.0

The spatial constraint parameter is currently not used with the BitCoin data, but it could be, in the future.

2. BitCoin transaction parser configuration

The following parameters control the BitCoin transaction data parsing process, including the source data file (GZipped or plain), the activities and process definition files and the number of lines to read. 
The last three parameters control values for various transaction filters, which generate observations for the PINT algorithm. Below we list additional information:

* Specify the file name of the PINT activities definition file
	pint.bitcoin.activities.filename=data/activities.txt 
	
* Specify the file name of the PINT process definition file
	pint.bitcoin.procses.filename=data/process.txt

* Specify the file name of the BitCoin transactions data (the user_edges file)
	pint.bitcoin.transactions.filename=user_edges.txt.gz 

* Specify the maximum number of transactions to process from the above file (16M is more than the provided data file contains, so it will read all transactions) 	
	pint.bitcoin.transactions.maxlines_toread=16000000 
	
* Specify the minimum value of any transaction component for a transaction to be considered "large value" 
	pint.bitcoin.transactions.largevaluefilter.minvalue=20000
	
* Specify the minimum value of a circular transaction (transaction to self)  
	pint.bitcoin.transactions.circularfilter.minvalue=1000
	
* Specify the minimum number of transaction components for a transaction to be considered "complex" 
	pint.bitcoin.transactions.complexfilter.minvalue=100 


3. Command line parameters

The BitCoin PINT process finder is used from the command line, as follows:

	> java -jar bitcoin-pint.jar propertiesFile [activitiesFile [processFile [transactions-file [maxLinesToRead]]]]
	
Only the <propertiesFile> file name is a required parameter. Additional parameters* can be optionally listed on the command line as indicated above.
The order of the parameters is significant. 

*Note: command line parameters override the corresponding configuration file properties.


IV. Activities and Process definition files

[THIS SECTION IS NOT YET COMPLETE]

1. Activities definition file

Example: data/activities.txt


2. Process definition file

Example: data/process.txt


V. Output

BitCoin PINT outputs a number of useful data items to standard output. It is best if standard output is redirected to a file for further examination, as follows:

	> java -jar bitcoin-pint.jar  propertiesFile > outputFile

Significant sections of the output are preceded by the ">>>" token, for example:

	>>> Loaded Properties File data/pint-bitcoin.properties 
	>>> Will use the following PINT configuration:
	....
	and so on...

Here are several outputs that might be of more interest to the user:

* Lists all activities from the activities definition file:
	>>> Created <n> activities:

* Lists all observables defined in the activities file:
	>>> Created <n> observables:

* Presents a compact version of the process model to be matched, consisting of activities and their partial ordering: 
	>>> Created process: 
	...

* Lists the number of transactions parsed:
	>>> Loaded <n> Transactions.

* Shows the number of transactions and the filters that were used to generate observations:
	>>> Filtering <n> transactions with 3 filters...
		IndividualValueFilter (minValue=20000.0) -> count = <p>
		TransactionToSelfFilter (minValue=1000.0) -> count = <q>
		NumberOfElementsFilter (minCount=100) -> count = <r>

* Shows the number of accepted transactions after filtering: 
	>>> Filtered down to <m> Transactions.

* Shows the number of observations generated from accepted transactions:
	>>> Generated <n> observations.

* Shows details of the procsess finding algorithm:
	>>> Looking for Process instances...
		...Processing <m> clusters...
		...Filtered down to <n> valid clusters...

* Shows timing information and the total number of solutions found:
	>>> Done looking for Process instances in <t> ms. Found <n> solutions.

* Lists the number of solutions that contain matching IDs across some of the observations that were part of the solution. 
	>>> Found <n> matching-user-id solutions.

* Lists all solutions in descending order of their matching score:
	>>> Solution reports:
		....

* A solution report represents a match between the process model we are searching for a a number of observations (transactions) that fit the model. 
A sample solution report is provided below.

Report 16.

[[[16]]] High value transaction (749478) target user (69021) was the source for Circular transaction (749479).

   Detected Process: Suspicious-Sequence
   [#SuspiciousTransactions-01]
   Score: 66.66666666666666
|    ----ACTIVITY----              |   ----OBSERVATION----                     |    ----TIME STAMP----            |    ----LOCATION----              |
|    High_Value                    |   Obs_IndividualValueFilter_749478        |    2013-03-19T05:44:21-0400      |    0.000000, 0.000000            |
|    Obfuscated                    |   UNBOUND                                 |                                  |                                  |
|    Circular                      |   Obs_TransactionToSelfFilter_749479      |    2013-03-19T06:24:46-0400      |    0.000000, 0.000000            |

Ordered Activity-Observation pairs: 
Act=High_Value Obs=Obs_IndividualValueFilter_749478 :-- Transaction 749478 on 2013-03-19 05:44:21
	 [749478:0]  $1000.000000 69021 -> 1683627
	 [749478:1]  $4365.000000 69021 -> 69021

Act=Obfuscated -- No Observation Bound. (binding: NONE {102, null})

Act=Circular Obs=Obs_TransactionToSelfFilter_749479 :-- Transaction 749479 on 2013-03-19 06:24:46
	 [749479:0]  $1000.000000 69021 -> 1683627
	 [749479:1]  $2500.000000 69021 -> 69021

-------------End Report 16-----------------------

In this case the High_Value and the Circular activities were matched by two observations - transactions with IDs [749478] and [749479], respectively.
The Obfuscated activity was not matched by any observable (transaction).

In addition to the process activities, and the matching observations, the time and location of the observation are listed (location is not currently used). 
The report also gives details on the transactions in the case when there is a matching User ID between two or more transactions, as is the case above.

Transaction [749478] is a High_Value transaction involving user ID 69021; Transaction [749479] is circular transaction involving the same user. 




