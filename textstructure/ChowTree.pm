package ChowTree;

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

sub read_vocab {
    my $vocab_file = shift;
    my (@v);


    unless (open(VOCAB, "<$vocab_file")) {
        warn  "Can't read from $vocab_file: $!\n";
        return(@v);
    }


    push(@v, "errorzerothwordisbogus");

    while (<VOCAB>) {
        chomp;
        push(@v, $_);
        $vocab_size++;
    }

    close VOCAB;
    return (@v);
}

sub construct_topic_labels {
    my $w_aryref = shift;
    die "Not an ARRAY reference" unless (ref($w_aryref) eq "ARRAY");

    my $topicfile = shift;

    my $TOP_N = 10;
    my $PRUNE_AFTER_N = 100;
    my @labels;

    if (not(open(TOPICS, "<$topicfile"))) {
        warn "Can't read topics from $topicfile: $!\n";
        return @labels;
    }
    my @top_values;

    my $header_done = 0;
    my $options_done = 0;
    while (<TOPICS>) {
        chomp;
        if ($options_done and m/^0\s+/) {
            $header_done = 1;
            next;
        } elsif (not $options_done) {
            if (m/^options/) {
                $options_done = 1;
                next;
            }
        }
        next if not $header_done;

        my (@entries) = split(' ', $_);
        last if ($entries[0] > $vocab_size);

        for (my $i=1; $i < scalar(@entries); $i++) {
            push(@{ $top_values[$i]}, {"index" => $entries[0], "score" => $entries[$i]});
            if (($. % $PRUNE_AFTER_N) == 0) {
                my @sorted = sort {$b->{"score"} <=> $a->{"score"} } @{ $top_values[$i] };
                @{ $top_values[$i] } = splice( @sorted, 0, $TOP_N);
            }
        }
    }
    close TOPICS;
    for (my $i = 1; $i < scalar(@top_values); $i++) {
        my @sorted = sort {$b->{"score"} <=> $a->{"score"} } @{ $top_values[$i] };
        @{ $top_values[$i] } = splice( @sorted, 0, $TOP_N);

        $labels[$i-1] = join(',', map { $w_aryref->[$_->{"index"}] } @{ $top_values[$i] }); 
        printf STDOUT "Label %d: %s\n", $i, $labels[$i-1];
    }
    return @labels;
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

1;