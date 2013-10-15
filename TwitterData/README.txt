

PINT Execution Wrapper for Twitter Data


I. Quick Start

Build the TiwtterData project as an executable JAR file (let's name it "twitter-pint.jar"), packaged with all dependencies. This project depends on:

	1. the process-alignment project (the main PINT algorithm implementation)
	2. the PintTestData project (testing and logging dependencies)
	
Assuming the twitter-pint.jar file is in the root TwitterData project directory, you can then run Twitter PINT as follows:

> java -jar tiwtter-pint data/twitterdata.properties 

This should run out-of-the-box and will use the default set of parameters and an input file containing a subset of the Twitter data from DataSet 1.
 

II. Building the project

We recommend building the project in Eclipse. After cloning the Git repository and importing the project from the Git working directory into Eclipse, 
make sure you add the two dependencies listed above to the project's  Build Path. Go to:
	Project > Properties > Java Build Path > Project tab > Add 
Then select the "process-alignment" and "PintTestData" projects as dependencies in the Java Build Path. 

Next, you can build and export the project as an executable JAR by selecting the TwitterData project and going to:
	File > Export > Runnable Jar File > Next > 
		- select TwitterPintRunner Launch Configuration 
		- type the output JAR filename in Export Destination, such as "twitter-pint.jar"
		- select "Package required libraries into generated JAR"

The resulting file should be a self-contained executable JAR file that you can run as shown above.    


III. Configuration

The execution is configured using a Java properties file. A default configuration is provided in the file data/twitterdata.properties

The following options are available:

1. PINT Configuration

The following PINT process matching algorithm parameters are available. 
We recommend leaving those parameters as-is because they require more advanced knowledge of the PINT algorithm.  
  
	pf.clusterer.agreement-threshold=0.5
	pf.generators.hconsistent.num-solutions=20
	pf.generators.hconsistent.hcon-threshold=3.0
	pf.generators.hconsistent.spatial-weight=2.0
	pf.generators.clusterfilter.percent-filled=0.4

There are two parameters that impose spatial and temporal constraints on the process matches that PINT discovers. 
Those can be changed easily, in order to explore different conditions. Note the units are kilometers and hours, repsectively:

	# max distance in kilometers
	process.max.distance.km=35.0
	# max timespan in hours
	process.max.timespan.ms=24.0


2. Twitter parser configuration

The following parameters control the Twitter parsing process, including the source data file (can be a regular file or a directory containing data files), 
the activities and process definition files and the number of lines to read. 

Finally, the last parameter specifies the languages that will be accepted for generating observations for process matching. 
The language parameter can specify a single language, as in the case below, or a comma-separated list of multiple languages. 
The special token "\N" should be used for unknown/unlabeled language in the Twitter data set.   

	pint.twitter.activities.filename=data/activities.txt
	pint.twitter.procses.filename=data/process.txt 
	pint.twitter.tweets.filename=data/tweets-chunk-00000000.tsv.gz 
	pint.twitter.tweets.maxlines_toread=3000000
	pint.twitter.tweets.start_at_line=1
	pint.twitter.filter.languages.accepted=English 


3. Command line parameters

The Twitter PINT process finder is used from the command line, as follows:

	> java -jar twitter-pint.jar  propertiesFile [activitiesFile [processFile [twitterFile/Dir [linesToRead [startAtLine]]]]]
	
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

Twitter PINT outputs a number of useful data items to standard output. It is best if standard output is redirected to a file for further examination, as follows:

	> java -jar twitter-pint.jar  propertiesFile > outputFile

Significant sections of the output are preceded by the ">>>" token, for example:

	>>> Loaded Properties File data/twitterdata.properties 
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

* Lists the number of tweets processed:
	>>> Total Tweets Read: <T> Tweets Filtered Out: <O>  Tweets Accepted: <A>

* Shows the number of observations generated from accepted tweets:
	>>> Generated <n> observations.

* Shows details of the procsess finding algorithm:
	>>> Looking for Process instances...
		...Processing <m> clusters...
		...Filtered down to <n> valid clusters...

* Shows timing information and the total number of solutions found:
	>>> Done looking for Process instances in <t> ms. Found <n> solutions.

* Lists all solutions in descending order of their matching score:
	>>> Solution reports:
		....

* A solution report represents a match between the process model we are searching for a a number of observations that fit the model. 
A sample solution report is provided below.

Report 1.
   Detected Process: Protest-model
   [#protest-model001]
   Score: 100.0
|    ----ACTIVITY----              |   ----OBSERVATION----                     |    ----TIME STAMP----            |    ----LOCATION----              |
|    Violence                      |   Obs_break_167178117788602368            |    2012-02-08T09:29:03-0500      |    29.314962, 47.963864          |
|    Protest                       |   Obs_demand_167353035683659776           |    2012-02-08T21:04:07-0500      |    29.303847, 48.075749          |
|    Demonstrate                   |   Obs_march_167483151332032512            |    2012-02-09T05:41:09-0500      |    29.058950, 48.116979          |
|    Occupy                        |   Obs_occupy_167512298074804224           |    2012-02-09T07:36:58-0500      |    29.316475, 48.050905          |
|    Education                     |   UNBOUND                                 |                                  |                                  |
|    Labor                         |   Obs_wage_167646587122753537             |    2012-02-09T16:30:35-0500      |    29.306798, 47.990722          |

Ordered observations: 
Act=#violence Obs=Obs_break_167178117788602368 :-- [id:167178117788602368] @f_alkhamis [Location:kuwait - yarmouk] 'Alwogayan wave 10 Ana 5 oo 6ag break hal mara 10  ym3wda  ya7oshk alah ywfgch oo ywfgna wyach oo nt5rj wn5l9' [Language:English]
Act=#protest Obs=Obs_demand_167353035683659776 :-- [id:167353035683659776] @ChefSamiSherida [Location:Kuwait] 'modrs11 Obama Demands you aalfze"t and helpful from Arabic And, http t co wpof90zo' [Language:English]
Act=#demonstration Obs=Obs_march_167483151332032512 :-- [id:167483151332032512] @xMishoO [Location:kuwait] 'iOS 5 1 likely coming on March 9th http t co 7SXBwd2z' [Language:English]
Act=#occupy Obs=Obs_occupy_167512298074804224 :-- [id:167512298074804224] @zazooss [Location:kuwait] 'TheBiscuits never get tried of doing little things for others sometimes those little things occupy the biggest part of their heart' [Language:English]
Act=#education -- No Observation Bound. (binding: NONE {102, null})
Act=#labor Obs=Obs_wage_167646587122753537 :-- [id:167646587122753537] @VoTcHkA [Location:QUrTuBa-Kuwait] '“ Q8EvAnEsCeNcE Shortage be possible wyaah Dagger RT shaytoonkuwait Q8EvAnEsCeNcE In wage byzHf with his secret weapon http t co hwiWPdih” kaaaaaak” bTnyyyy ??' [Language:English]
-------------End Report 1-----------------------

In addition to the process activities, the matching observations, the time and location, the report lists the original text of the tweets that generated the matching observations.
The tweets are listed below "Ordered observations:" and are preceded by the ID of the Activity and the Observation object that contains the tweet. 
Sevral additional fields are also provided, in addition to the text of the tweet: tweet ID, the author's user name, the listed location, and the language tag.   





