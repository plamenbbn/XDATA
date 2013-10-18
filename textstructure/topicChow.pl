#!/usr/bin/env perl

# DARPA XDATA licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with 
# the License.  You may obtain a copy of the License at 
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and  
# limitations under the License.
#
# Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
#

use Getopt::Long;
use Pod::Usage;
use POSIX;

use FindBin qw($Bin);
use lib "$Bin";
use ChowTree;

#locations of binaries
$plsa_estimation_binary = "topic/bin/plsa_estimation";
$chow_binary = "topic/bin/chowtest";

#common
my $plsa_option;
my $plsa_chow_option;
my $lda_option;
my $lda_chow_option;
my $output_prefix;
my $num_topics = 10;
my $max_iterations = 20;
my $input_file;
my $vocab_file;
my $binary_input;

#plsa
my $tf_cutoff; 
my $df_cutoff; 
my $stop_list;
my $out_all;
my $features_out;
my $summary_out;
my $z_given_d_out;
my $w_given_z_out;
my $w_out;
my $z_out;
my $plsa_model;

#lda
#dafualt values taken from the vw lda tutorial at:
#https://github.com/JohnLangford/vowpal_wabbit/wiki/lda.pdf
my $vw_path = '.';
my $alpha = .1; #Dirichlet prior on the per-document topic distribution
my $rho = .1; #Dirichlet prior on the per-topic word distribution
my $minibatch = 256; #number of features that are processed as a group
my $power_t = .5;  
my $initial_t = 1;
my $bit_width = 16;
my $text_topics_out;
my $bin_topics_out;
my $predictions_out;


#chow
my $threshold = .25; #arbitrary default
my $all_models;
my $z_given_d_model;
my $z_given_d_post;
my $z_given_w_post;


GetOptions(
	#general
	"plsa" => \$plsa_option,
	"plsa_chow" => \$plsa_chow_option,
	"lda" => \$lda_option,
	"lda_chow" => \$lda_chow_option,
	"out=s" => \$output_prefix,
	"input=s" => \$input_file,
	"vocab=s" => \$vocab_file,
	"max_iterations=i" => \$max_iterations,
	"num_topics=i" => \$num_topics,
	
	#plsa options
	"tf_cutoff=i" => \$tf_cutoff,
	"df_cutoff=f" => \$df_cutoff,
	"stop_list=s" => \$stop_list, #same question
	"features_out" => \$features_out,
	"summary_out" => \$summary_out,
	"out_all" => \$out_all,
	"z_given_d_out" => \$z_given_d_out,
	"w_given_z_out" => \$w_given_z_out,
	"w_out" => \$w_out,
	"z_out" => \$z_out,
	
	#lda options
	"vw=s" => \$vw_path,
	"alpha=f" => \$alpha, 
	"rho=f" => \$rho, 
	"minibatch=i" => \$minibatch, 
	"power_t=f" => \$power_t, #don't know what this is  
	"initial_t=f" => \$initial_t,
	"bid_width=i" => \$bit_width,
	"text_topics" => \$text_topics_out,
	"bin_topics" => \$bin_topics_out,
	"predictions" => \$predictions_out,
	
	#chow options
	"threshold=f" => \$threshold, 
	"all_topic_models" => \$all_models, 
	"z_given_d" => \$z_given_d_model,
	"z_given_d_post" => \$z_given_d_post,
	"z_given_w_post" => \$z_given_w_post
);

#basically just run plsa with a couple of output options including posterior probability tables
#output is ascii
if ($plsa_option) {
	#test for whether there are ample arguments to run - need to input something and output something
	if ($input_file) {
		
		#if no ouput is selected then standard tables will be return
		if (! ($out_all || $summary_out || $z_given_d_out || $w_given_z_out || $w_out || $z_out)) {
			$output_prefix = $output_prefix? $output_prefix : "plsa";
			$summary_out = $z_given_d_out = $w_given_z_out = 1;
		}
		
		#get arguments into string form
		#use defaults in the plsa code
		$counts_file = "-vector_list_in $input_file ";
		$num_topics = ($num_topics ? "-num_topics $num_topics " : "");
		$tf_cutoff = ($tf_cutoff ? "-tf_cutoff $tf_cutoff " : "");
		$df_cutoff = ($df_cutoff ? "-df_cutoff $df_cutoff " : "");
		$stop_list = ($stop_list ? "-stop_list_in $stop_list " : "");
		$max_iterations = ($max_iterations ? "-max_iter $max_iterations " : "");
		$features_out = ($features_out || $out_all? "-feature_list_out $output_prefix.plsa.features " : "");
		$summary_out = ($summary_out || $out_all? "-summary_out $output_prefix.plsa.summary " : "");
		$z_given_d_out = ($z_given_d_out || $out_all? "-z_given_d_out $output_prefix.plsa.zgd " : "");
		$w_given_z_out = ($w_given_z_out || $out_all? "-w_given_z_out $output_prefix.plsa.wgz " : "");
		$w_out = ($w_out || $out_all? "-w_out $output_prefix.plsa.w " : ""); 
		$z_out= ($z_out || $out_all? "-z_out $output_prefix.plsa.z " : ""); 
		
		#run the command
		$cmd = "$plsa_estimation_binary $counts_file$num_topics$tf_cutoff$df_cutoff$stop_list$max_iterations".
			"$features_out$summary_out$z_given_d_out$w_given_z_out$w_out$z_out -random";
		
		&run_cmdline($cmd);
	}
	else {
		print "command requires an input file specified with the -input flag.  For formatting details see readme\n";
	}
}

#run plsa then chow tree - 
elsif ($plsa_chow_option) {
	if ($input_file) {
		
		#if no model is selected - pick z|d posterior
		if (! ($z_given_d_model || $z_given_d_post || $z_given_w_post)) {
			$z_given_d_post = 1;
		}
		
		#if no output prefix
		$output_prefix = $output_prefix ? $output_prefix : "plsa-chow";
		
		#create plsa job
		#get arguments into string form
		$input_file = "-vector_list_in $input_file ";
		$summary_out = "-summary_out $output_prefix.plsa.summary ";
		
		#convert the variables to cmd line strings
		$num_topics = ($num_topics ? "-num_topics $num_topics " : "");
		$tf_cutoff = ($tf_cutoff ? "-tf_cutoff $tf_cutoff " : "");
		$df_cutoff = ($df_cutoff ? "-df_cutoff $df_cutoff " : "");
		$stop_list = ($stop_list ? "-stop_list_in $stop_list " : "");
		$max_iterations = ($max_iterations ? "-max_iter $max_iterations " : "");
		$features_out = ($features_out? "-feature_list_out $features_out " : "");
		$z_given_d_model = ($z_given_d_model || $all_models? "-z_given_d_out $output_prefix.plsa.zgdm " : "");
		$z_given_d_post = ($z_given_d_post || $all_models? "-z_given_d_post $output_prefix.plsa.zgdp " : "");
		$z_given_w_post = ($z_given_w_post || $all_models? "-z_given_w_post $output_prefix.plsa.zgwp" : "");
		  
		  
		#run the command - with random initialization
		$cmd = "$plsa_estimation_binary $input_file$num_topics$tf_cutoff$df_cutoff$stop_list$max_iterations".
			"$features_out$summary_out$z_given_d_model$z_given_d_post$z_given_w_post -random";
		&run_cmdline($cmd);
			
		#get the topic summaries - these will label the tree
		my @topic_summaries = ();
		open FILE, "<$output_prefix.plsa.summary" or die $!;
		$counter = 0;
		while ($topic = <FILE>) {
			chomp($topic);
			$topic_summaries[$counter] = $topic;
			$counter++;
		}
		close FILE;
		
	
		#make chow trees for three potential models
		if ($z_given_d_model || $all_models) {
			normalize_and_threshold("$output_prefix.plsa.zgdm", "$output_prefix.zgdm.observations", 0, $threshold);
			&run_cmdline("./$chow_binary $output_prefix.zgdm.observations $output_prefix.zgdm.chowtree $output_prefix.zgdm.mi $output_prefix.zgdm.nb");
			construct_dot_input("$output_prefix.zgdm.chowtree", "$output_prefix.zgdm.mi", \@topic_summaries, "$output_prefix.zgdm.dot");
			&run_cmdline("dot -Tpng -o $output_prefix.zgdm.png $output_prefix.zgdm.dot");				
		}
			
		if ($z_given_d_post || $all_models) {
			ChowTree::normalize_and_threshold("$output_prefix.plsa.zgdp", "$output_prefix.zgdp.observations", 0, $threshold);
			&run_cmdline("./$chow_binary $output_prefix.zgdp.observations $output_prefix.zgdp.chowtree $output_prefix.zgdp.mi $output_prefix.zgdp.nb");
			ChowTree::construct_dot_input("$output_prefix.zgdp.chowtree", "$output_prefix.zgdp.mi", \@topic_summaries, "$output_prefix.zgdp.dot");
			&run_cmdline("dot -Tpng -o $output_prefix.zgdp.png $output_prefix.zgdp.dot");				
		}
			
		if ($z_given_w_post || $all_models) {
			ChowTree::normalize_and_threshold("$output_prefix.plsa.zgdp", "$output_prefix.zgwp.observations", 0, $threshold)
			&run_cmdline("./$chow_binary $output_prefix.zgwp.observations $output_prefix.zgwp.chowtree $output_prefix.zgwp.mi $output_prefix.zgwp.nb");
			ChowTree::construct_dot_input("$output_prefix.zgwp.chowtree", "$output_prefix.zgwp.mi", \@topic_summaries, "$output_prefix.zgwp.dot");
			&run_cmdline("dot -Tpng -o $output_prefix.zgwp.png $output_prefix.zgwp.dot");				
		}
	}
	
	else {
		print "command requires an input file specified with the -input flag.  For formatting details see readme\n";
	}
}

elsif ($lda_option) {
	if ($input_file) {

		# Get number of input documents
		my $num_documents = `wc -l $input_file`;
		$num_documents = (split(' ', $num_documents))[0];
		
		# create output prefix if it doesn't exist
		$output_prefix = $output_prefix ? $output_prefix : "lda";
		
		if (! ($text_topics_out || $bin_topics_out  || $predictions_out)) {
			$predictions_out = 1;
		}
	
		#if we have a vocab file calculate bit width correctly
		if ($vocab_file) {
			my $num_features = `wc -l $vocab_file`;
			my $bit_width = ceil(log($num_features)/log(2));
		}
		
		# Construct cache file
		my $cache = "/tmp/ldachow-$$.cache";
		if (-e "$cache") {
			unlink("$cache") or die "Can't unlink cache file $cache: $!\n";
		}

		#run vw lda
		my $lda_cmd = "$vw_path/vw -d $input_file --no_stdin --lda $num_topics --lda_alpha $alpha --lda_rho $rho --minibatch $minibatch --power_t $power_t --initial_t $initial_t --passes $max_iterations --cache_file $cache --lda_D $num_documents -b $bit_width ".
			($predictions_out? "-p $output_prefix.predictions " : "").
			($text_topics_out? "--readable_model $output_prefix.topics.text" : "").
			($bin_topics_out? "-f $output_prefix.topics.dat" : "");	
		&run_cmdline($lda_cmd);
	}
	
	else {
		print "command requires an input file specified with the -input flag.  For formatting details see readme\n";
	}
}

elsif ($lda_chow_option) {
	if ($input_file && $vocab_file) {

		# Get number of input documents
		my $num_documents = `wc -l $input_file`;
		$num_documents = (split(' ', $num_documents))[0];

		# Get the number of words/features - the vocab file is required 
		my $num_features = `wc -l $vocab_file`;
		#get the bitwidth 2^b = num_features
		my $bit_width = ceil(log($num_features)/log(2));
				
		# Construct cache file
		my $cache = "/tmp/ldachow-$$.cache";
		if (-e "$cache") {
			unlink("$cache") or die "Can't unlink cache file $cache: $!\n";
		}
		
		# create output prefix if it doesn't exist
		$output_prefix = $output_prefix ? $output_prefix : "lda-chow";
		
		#name predictions file and text topics file
		my $predictions = "$output_prefix.predictions";
		my $text_topics = "$output_prefix.topics.text";
		
		#run vw lda
		my $lda_cmd = "$vw_path/vw -d $input_file --no_stdin --lda $num_topics --lda_alpha $alpha --lda_rho $rho --minibatch $minibatch --power_t $power_t --initial_t $initial_t --passes $max_iterations --cache_file $cache --lda_D $num_documents -b $bit_width "
			."-p $predictions "
			."--readable_model $text_topics"
			.($bin_topics_out? "-f $output_prefix.topics.dat" : "");	
		&run_cmdline($lda_cmd);
		
		#create chow observations file from predictions
		
		#files created
		my $observations = $output_prefix.".observations";
		my $chowtree = $output_prefix.".chowtree";
		my $mi = $output_prefix.".mi";
		my $nb = $output_prefix.".nb";
		my $dot = $output_prefix.".dot";
		my $png = $output_prefix.".png";
		
		#threshold values create observations file - a document was observed in a topic if its value was sufficiently high
		ChowTree::normalize_and_threshold($predictions, $observations, 1, $threshold);
		
		#create the chowtree
		&run_cmdline("$chow_binary $observations $chowtree $mi $nb");
		
		#get the vocab into an array and identify significant words for each topic
		my @vocabulary = &read_vocab($vocab_file);
		my @topic_labels = &construct_topic_labels(\@vocabulary, $text_topics);
		
		#create the simple tree graphic
		ChowTree::construct_dot_input($chowtree, $mi, \@topic_labels, $dot);
		&run_cmdline("dot -Tpng -o $png $dot");	
	}
	else {
		print "command requires an input file specified with the -input flag and a vocabular file\n specified with the -vocab flag.".
		"For formatting details see readme\n";
	}	
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
