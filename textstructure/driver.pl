#!/usr/bin/env perl

use Getopt::Long;
use Pod::Usage;

#locations or binaries
$plsa_estimation_binary = "topic/bin/plsa_estimation";
$chow_binary = "topic/bin/chowtest";

my $plsa_option;
my $plsa_chow_option;
my $output_path;

#plsa
my $plsa_counts_file;
my $num_topics;
my $tf_cutoff;
my $df_cutoff;
my $stop_list;
my $max_iterations;
my $out_all;
my $features_out;
my $summary_out;
my $out_all;
my $z_given_d_out;
my $w_given_z_out;
my $w_out;
my $z_out;
my $plsa_model; #need to should add to c code

#lda

#chow
my $threshold;
my $all_models;
my $z_given_d_model;
my $z_given_d_post;
my $z_given_w_post;


GetOptions("plsa" => \$plsa_option,
	"plsa_chow" => \$plsa_chow_option,
	"output=s" => \$output_path,
	
	
	"word_count_file=s" => \$plsa_counts_file,
	
	#plsa options
	"num_topics=i" => \$num_topics,
    "tf_cutoff=i" => \$tf_cutoff,
	"df_cutoff=f" => \$df_cutoff,
	"stop_list=s" => \$stop_list,
	"max_iterations=i" => \$max_iterations,
	"features_out" => \$features_out,
	"summary_out" => \$summary_out,
	"out_all" => \$out_all,
	"z_given_d_out" => \$z_given_d_out,
	"w_given_z_out" => \$w_given_z_out,
	"w_out" => \$w_out,
	"z_out" => \$z_out,
	
	# #lda options
	
	
	#chow options
	"threshold=f" => \$threshold, 
	"all_topic_models" => \$all_models, 
	"z_given_d" => \$z_given_d_model,
	"z_given_d_post" => \$z_given_d_post,
	"z_given_w_post" => \$z_given_w_post
	);

if ($plsa_option) {
	#test for whether there are ample arguments to run - need to input something and output something
	if ($plsa_counts_file && ($out_all || $summary_out || $z_given_d_out || $w_given_z_out || $w_out || $z_out)) {
		
		#get arguments into string form
		$counts_file = "-vector_list_in $plsa_counts_file ";
		
		$num_topics = ($num_topics ? "-num_topics $num_topics " : "");
		$tf_cutoff = ($tf_cutoff ? "-tf_cutoff $tf_cutoff " : "");
		$df_cutoff = ($df_cutoff ? "-df_cutoff $df_cutoff " : "");
		$stop_list = ($stop_list ? "-stop_list_in $stop_list " : "");
		$max_iterations = ($max_iterations ? "-max_iter $max_iterations " : "");
		$features_out = ($features_out || $out_all? "-feature_list_out $output_path.plsa.features " : "");
		$summary_out = ($summary_out || $out_all? "-summary_out $output_path.plsa.summary " : "");
		$z_given_d_out = ($z_given_d_out || $out_all? "-z_given_d_out $output_path.plsa.zgd " : "");
		$w_given_z_out = ($w_given_z_out || $out_all? "-w_given_z_out $output_path.plsa.wgz " : "");
		$w_out = ($w_out || $out_all? "-w_out $output_path.plsa.w " : ""); 
		$z_out= ($z_out || $out_all? "-z_out $output_path.plsa.z " : ""); 
		
		#run the command
		
		$cmd = "$plsa_estimation_binary $counts_file$num_topics$tf_cutoff$df_cutoff$stop_list$max_iterations";
			$cmd.= "$features_out$summary_out$z_given_d_out$w_given_z_out$w_out$z_out -random";
		
		&run_cmdline($cmd);
	}
	else {#?
	}
}

elsif ($plsa_chow_option) {
	if ($plsa_counts_file) {
		if ($all_models || $z_given_d_model || $z_given_d_post || $z_given_w_pos) {
		
			#create plsa job
			#get arguments into string form
			$plsa_counts_file = "-vector_list_in $plsa_counts_file ";
			$summary_out = "-summary_out $output_path.plsa.summary ";
			
			$num_topics = ($num_topics ? "-num_topics $num_topics " : "");
			$tf_cutoff = ($tf_cutoff ? "-tf_cutoff $tf_cutoff " : "");
			$df_cutoff = ($df_cutoff ? "-df_cutoff $df_cutoff " : "");
			$stop_list = ($stop_list ? "-stop_list_in $stop_list " : "");
			$max_iterations = ($max_iterations ? "-max_iter $max_iterations " : "");
			$features_out = ($features_out? "-feature_list_out $features_out " : "");
			$z_given_d_model = ($z_given_d_model || $all_models? "-z_given_d_out $output_path.plsa.zgdm " : "");
			$z_given_d_post = ($z_given_d_post || $all_models? "-z_given_d_post $output_path.plsa.zgdp " : "");
			$z_given_w_post = ($z_given_w_post || $all_models? "-z_given_w_post $output_path.plsa.zgwp" : "");
			  
			  
			#run the command
	
			$cmd = "$plsa_estimation_binary $plsa_counts_file$num_topics$tf_cutoff$df_cutoff$stop_list$max_iterations";
				$cmd.= "$features_out$summary_out$z_given_d_model$z_given_d_post$z_given_w_post -random";
	
			&run_cmdline($cmd);
			
			#get the topic summaries:
			my @topic_summaries = ();
			open FILE, "<$output_path.plsa.summary" or die $!;
			$counter = 0;
			while ($topic = <FILE>) {
				chomp($topic);
				$topic_summaries[$counter] = $topic;
				$counter++;
			}
			close FILE;
		
	
			#make chow trees for three potential models
			if ($z_given_d_model || $all_models) {
				&normalize_and_threshold("$output_path.plsa.zgdm", "$output_path.zgdm.observations", 0, $threshold);
				&run_cmdline("./$chow_binary $output_path.zgdm.observations $output_path.zgdm.chowtree $output_path.zgdm.mi $output_path.zgdm.nb");
				&construct_dot_input("$output_path.zgdm.chowtree", "$output_path.zgdm.mi", \@topic_summaries, "$output_path.zgdm.dot");
				&run_cmdline("dot -Tpng -o $output_path.zgdm.png $output_path.zgdm.dot");				
			}
			
			if ($z_given_d_post || $all_models) {
				&normalize_and_threshold("$output_path.plsa.zgdp", "$output_path.zgdp.observations", 0, $threshold);
				&run_cmdline("./$chow_binary $output_path.zgdp.observations $output_path.zgdp.chowtree $output_path.zgdp.mi $output_path.zgdp.nb");
				&construct_dot_input("$output_path.zgdp.chowtree", "$output_path.zgdp.mi", \@topic_summaries, "$output_path.zgdp.dot");
				&run_cmdline("dot -Tpng -o $output_path.zgdp.png $output_path.zgdp.dot");				
			}
			
			if ($z_given_w_post || $all_models) {
				&normalize_and_threshold("$output_path.plsa.zgdp", "$output_path.zgwp.observations", 0, $threshold);
				&run_cmdline("./$chow_binary $output_path.zgwp.observations $output_path.zgwp.chowtree $output_path.zgwp.mi $output_path.zgwp.nb");
				&construct_dot_input("$output_path.zgwp.chowtree", "$output_path.zgwp.mi", \@topic_summaries, "$output_path.zgwp.dot");
				&run_cmdline("dot -Tpng -o $output_path.zgwp.png $output_path.zgwp.dot");				
			}
		}
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

sub normalize_and_threshold {
    my $obs = shift;
    my $chow_input = shift;
    my $do_norm = shift;
    my $threshold = shift;

    die "Can't read from observations file $obs: $!\n"
        unless open (OBS, "<$obs");

    die "Can't write to $chow_input: $!\n"
        unless open(OUT, ">$chow_input");

    my $num_documents = 0;
    my $num_features = -1;
    my $first_line = 1;
    my @output;
    while (<OBS>) {
        chomp;

        my @vals = split(' ', $_);
        if ($first_line) {
            $first_line = 0;
            $num_features = scalar(@vals);
        } else {
            my $this_line_num_features = scalar(@vals);
            if (scalar(@vals) != $num_features) {
                die "$obs must have the same number of features per line! Line $. got $this_line_num_features expected $num_features";
            }
        }

        $num_documents++;

        my $sum = 0;
        if ($do_norm) {
            foreach my $val (@vals) { $sum += $val; }
			
			if ($sum) {
				map { $_ /= $sum } @vals;
			}
			else {
				map { $_ * 0 } @vals;
			}
        }
        if (defined($threshold)) {
            my $numerical_threshold = $threshold;
            if ($do_norm and ($threshold > 1)) {
                my @sorted = sort {$b <=> $a} @vals;
                $numerical_threshold = $sorted[ceil($threshold)];
            }
            map { $_ = (($_ >= $numerical_threshold)?1:0) } @vals;
        }
        push(@output, join(' ', @vals));
    }
    close OBS;

    print OUT "$num_documents $num_features\n";
    foreach my $s (@output) {
        print OUT "$s", "\n";
    }
    
    close OUT;
}

sub construct_dot_input {
    my $treefile = shift;
    my $mifile   = shift;
    my $w_aryref = shift;
    my $dot_input = shift;

    my $t = ref $w_aryref;
    die "Not an ARRAY reference: $t" unless ($t eq "ARRAY");

    die "Can't read from $treefile: $!\n"
        unless open(TREE, "<$treefile");

    die "Can't read from $mifile: $!\n"
        unless open(MI, "<$mifile");

    die "Can't write to $dot_input: $!\n"
        unless open(DOT, ">$dot_input");

    my @mi;
    my @parent;
    my @p_head_given_head;
    my @p_head_given_tail;

    while (<MI>) {
        chomp;
        
        if (m/^MI\[([0-9]+)\]\[([0-9]+)\]\s*\=\s*\-*([0-9\.e\+\-]+)/) {
            $mi[$1]->[$2] = $3;
            $mi[$2]->[$1] = $3;
        }
    }
    close MI;


    my $junk;
    $junk = <TREE>;
    my $parent_list = <TREE>;
    chomp $parent_list;
    @parent = split(' ', $parent_list);
    $junk = <TREE>;
    $junk = <TREE>;
    $junk = <TREE>;

    my $this_entry = 0;
    while (<TREE>) {
        chomp;
        my @stuff = split(' ', $_);
        $p_head_given_tail[$this_entry] = $stuff[0];
        $p_head_given_head[$this_entry] = $stuff[1];
        if ($this_entry == 0) {
            if ($p_head_given_head[0] != 1) {
                die "Chow tree parser failed";
            }
        }
        $this_entry++;
    }
    close TREE;

    for (my $i = 0; $i < @mi; $i++) {
        $mi[$i][$i] = 1.0;
    }


    print DOT << "EOT";
    digraph d {
        rankdir = BT;
EOT

    for (my $i = 0; $i < @mi; $i++) {

        my $topic_label = $w_aryref->[$i];

        printf DOT '     x%d [label="x%d\n%s\nP(1|x%d=0) = %.5f\nP(1|x%d=1) = %.5f\n"];', $i, $i, $topic_label,  $parent[$i], $p_head_given_tail[$i], $parent[$i], $p_head_given_head[$i];
        print DOT "\n";

        if ($i > 0) { printf DOT "     x%d -> x%d [label=\"I(\\\T,\\\H)=%.5f\"];\n", $i, $parent[$i], $mi[$i]->[$parent[$i]]; }
    }

print DOT << "EOT";
}
EOT
    
    close DOT;
}