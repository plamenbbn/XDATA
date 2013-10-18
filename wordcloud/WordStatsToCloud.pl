#!/usr/bin/env perl

BEGIN { unshift(@INC, "."); }

use WordCloud;
use strict;
use Getopt::Long;

my $infn;
my $outfn;
my $cloudsize = 30;
my $min_size = 12;
my $max_size = 52;
my $help = 0;

GetOptions('in=s'          => \$infn,
           'out=s'         => \$outfn,
           'cloudsize=i'   => \$cloudsize,     
           'max_font_size=i' => \$max_size,
           'min_font_size=i' => \$min_size,
           'help'          => \$help);

if ($help ||  (! defined($outfn)) ||  (! defined($infn))) {
    print(join(" \n",
               "usage: getCloud.pl -in <input-file> -out <output-file>",
               "[-cloudsize <num words, default=30]  [-min_font_size <size>] [-max_font_size <size>]",
          ) . "\n");

    exit($help ? 0 : -1);
}

#create cloud object
my $cloud = new WordCloud();
$cloud->font_size_min($min_size);
$cloud->font_size_max($max_size);
load_table($cloud, $infn);
$cloud->auto_scale_size();

#write cloud to html file
print "writing out to $outfn\n";
open(OUTFH, ">:utf8", $outfn) or die("Error opening $outfn for writing: $!");
binmode(STDOUT, ":utf8");
print OUTFH WordCloud::html_header() . "\n";
print OUTFH $cloud->get_html_cloud($cloudsize) . "\n";
print OUTFH WordCloud::html_footer() . "\n";
close(OUTFH) or die("Error closing $outfn: $!");
exit 0;

sub load_table {
    # input file format: one term per line, tab-delimited
    # fields: tfidf term tf df
    my $cloud = shift;
    my $fn = shift;

	#input from stdin
    if ($fn eq "-") {
		open(FH, '<&STDIN') or die("Cannot dup stdin: $!");
		binmode(FH, ':utf8');
    } 
	#file
	else {
		open(FH, '<:utf8', $fn) or die("Cannot open $fn for reading: $!");
    }
	
	#add tf idf counts to cloud object
	my %params = ();
    while(<FH>) {        
        chomp;
        my ($tfidf, $word, $tf, $df, $junk) = split(/\s+/, $_);
		warn "Unexpected contents ($fn line $.), ignoring\n" if ($junk ne "");
		
		#use the tfidf from the file instead of recalculating
		$params{TFIDF} = $tfidf;
		
		$cloud->add_term($word, $tf, $df, %params);
    }
    close(FH) or die("Error closing $fn: $!");
}
