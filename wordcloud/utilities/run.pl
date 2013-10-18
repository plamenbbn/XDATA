#!/usr/bin/perl

use Getopt::Long;

#test that the jar exists
if (-e "CSVtoSeq.jar") {
	
	#parse command line
	my $delim = "\'\\t\'";
	my $col = 0;
	my $numReducers = 1;
	my @conditions = ();
	
	GetOptions("col=i" => \$col,
	"delim=s" => \$delim,
	"reducers=i" => \$numReducers,
	"cond=s" => \@conditions,
	);

	my $input = shift;
	my $output = shift;
	
	#run hadoop job
	$hadoopJob = "hadoop jar CSVtoSeq.jar CSVtoSeq $input $output $delim $col $numReducers @conditions";
	&run_cmdline($hadoopJob);
}
else {
	print "Jar file does not exist.  Run compile.pl\n";
}

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
