#!/usr/bin/env perl

use Getopt::Long;
use Pod::Usage;

#Get command line options
my $streamingJar; #is there a default?
my $stoplist;
my $outputPrefix = 'wordcloud';
my $wordsInCloud = 25;
my $pythonExec = 'python';
my $language = 'en';
my $preProc = 1;
my $tfOption = 'count';
my $dfOption = '';
my $TOTAL_DOCS;
my $TOTAL_Terms;

GetOptions("jar=s" => \$streamingJar,
	"out=s" => \$outputPrefix,
	"n=i" => \$wordsInCloud,
	"python=s" => \$pythonExec,
	"language=s" => \$language,
    "pp!" => \$preProc,
	"lang!" => \$language,
	"stop=s" => \$stoplist,
	"tf=s" => \$tfOption,
	"df=s" => \$dfOption
);

my $TFHdfsPath=shift;
my $DFHdfsPath=shift;
my $OutHdfsPath=shift;

#check if langid mod file exists - if not will look for langid zip
# or langid directory
if ($language) {
	#check that langid.mod exists
	if (! -e 'langid.mod') {
		if (-e 'langid.zip') {
			print "Renaming langid.zip langid.mod...\n";
			&run_cmdline ('mv langid.zip langid.mod');
		}
		elsif (-d 'langid') {
			print "Creating langid.mod...\n";
			&run_cmdline ('zip -r langid.zip langid');
			&run_cmdline ('mv langid.zip langid.mod');
		}
		else {
			print "Could not find langid module - running without langauge detection...\n";
			$language = undef;
		}
	}
}

##run map reduce job to get term frequency
$tfCommand = "hadoop jar $streamingJar ".
   "-file tf_mapper.py tf_reducer.py ".($language? "langid.mod " : "")."$stoplist ".
   "-inputformat \'SequenceFileAsTextInputFormat\' ".
   "-mapper  \"$pythonExec tf_mapper.py $tfOption $stoplist $language $preProc\" ".
   "-reducer \"$pythonExec tf_reducer.py\" ".
   "-input $TFHdfsPath ".
   "-output $OutHdfsPath/tc";
&run_cmdline($tfCommand);

#get the number of words and print:
$totalWords = (split ",", (split "\t", `hadoop fs -cat $OutHdfsPath/tc/part-* | grep _TF_TOTAL_WORD_COUNT`)[1])[0];
print "Counted $totalWords words\n";

##run map reduce job to get document frequency
$dfCommand = "hadoop jar $streamingJar ".
   "-file df_mapper.py df_reducer.py ".($language? "langid.mod " : "")."$stoplist ".
   "-inputformat \'SequenceFileAsTextInputFormat\' ".
   "-mapper  \"$pythonExec df_mapper.py $stoplist $language $preProc\" ".
   "-reducer \"$pythonExec df_reducer.py\" ".
   "-input $DFHdfsPath ".
   "-output $OutHdfsPath/dc";
&run_cmdline($dfCommand);

#get the number of docs and print:
$totalDocs = (split ",", (split "\t", `hadoop fs -cat $OutHdfsPath/dc/part-* | grep _DF_TOTAL_DOC_COUNT`)[1])[0];
print "Counted $totalDocs docs\n";

##calculate and sort tf-idf scores
$tfidfCommand = "hadoop jar $streamingJar ".
   "-Dmapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator ".
   "-Dmapred.text.key.comparator.options=-k1nr ".
   "-Dnum.key.fields.for.partition=1 ".
   "-Dstream.num.map.output.key.fields=1 ".
   "-Dmapred.child.java.opts=-Xmx2G ". #remove or make configurable
   "-file tfidf_reducer.py ".
   "-mapper \"cat\" ".
   "-reducer \"$pythonExec tfidf_reducer.py $dfOption\" ". 
   "-partitioner org.apache.hadoop.mapred.lib.KeyFieldBasedPartitioner ".
   "-cmdenv DF_TOTAL_DOC_COUNT=$totalDocs ".
   "-cmdenv TF_TOTAL_WORD_COUNT=$totalWords ".
   "-input   $OutHdfsPath/tc ".
   "-input   $OutHdfsPath/dc ".
   "-output  $OutHdfsPath/tfidf";
&run_cmdline($tfidfCommand);
print "Created TF-IDF scores in $OutHdfsPath/tfidf\n";

#create the word cloud
$wcCommand = "hadoop fs -text $OutHdfsPath/tfidf/part-* | sort -k1nr | head -n $wordsInCloud | ".
	"perl WordStatsToCloud.pl -cloudsize $wordsInCloud -in - -out $outputPrefix"."_$wordsInCloud.html";
&run_cmdline($wcCommand);
print "Created HTML word cloud in $outputPrefix"."_$wordsInCloud.html\n";

#runs command line and prints error to stdout on failure
sub run_cmdline {
    my $cmdline = shift;

    print STDOUT "Running: $cmdline\n";

    my $status = system($cmdline);


    if ($status != 0) {
        if ($? == -1) {
            print STDOUT "... failed to execute: $!\n";
        } elsif ($? & 127) {
            printf STDOUT "... died with signal %d, %s coredump\n",
            ($? & 127),  ($? & 128) ? 'with' : 'without';
        } else {
            printf "... exited with value %d\n", $? >> 8;
        }
        exit($status);
    }
}			

# .
