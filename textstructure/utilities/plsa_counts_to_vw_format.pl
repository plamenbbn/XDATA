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

my $plsa_file = shift;
my $doc_words = shift;
my $vocabulary= shift;

open INPUT, "<$plsa_file" or die $!;
open OUTPUT, ">doc_words" or die $!;

my %words = ();
my $word_count = 0;

#do conversion - word to word idx
while ($doc = <INPUT>) {
	chomp($doc);
	@word_pairs = split /\s/, $doc;
	
	print OUTPUT "|";
	
	#skip 
	for($i=1; $i<scalar @word_pairs; $i++) {
	
		@pair = split /\|/, $word_pairs[$i];
	
		#add to words array to create index
		if (! exists $words{$pair[0]}) {
			$words{$pair[0]} = $word_count;
			$word_count++;
		}
		
		print OUTPUT " $words{$pair[0]}:$pair[1]";
	}
	
	print OUTPUT "\n";
}

close INPUT;
close OUTPUT;

#print vocabulary key
open VOCAB, ">$vocabulary" or die $!;
foreach $word (sort {$words{$a} <=> $words{$b}} keys %words) {
	print VOCAB "$word\n";
}
close VOCAB;