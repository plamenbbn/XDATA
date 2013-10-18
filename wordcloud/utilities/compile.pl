#!/usr/bin/perl

my $hadoopPath = shift;

#test that the directory target exists
if (! -d "target") {
	mkdir "target";
}

#commands to compile and jar the hadoop file
$javac = "javac -cp $hadoopPath -d target CSVtoSeq.java";
$jar = "jar -cvf CSVtoSeq.jar -C target .";

&run_cmdline($javac);
&run_cmdline($jar);

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
