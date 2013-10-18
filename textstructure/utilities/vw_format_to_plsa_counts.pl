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

my $doc_words = shift;
my $vocabulary= shift;
my $plsa_file = shift;

#get word id mapping from vocab file
my @words = ();
open VOCAB, "<$vocabulary" or die $!;
print "creating vocabulary hash...\n";
while ($word = <VOCAB>) {
	chomp($word);
	push @words, $word;
}
close VOCAB;

open INPUT, "<$doc_words" or die $!;
open OUTPUT, ">$plsa_file" or die $!;

#do the conversion (id to word string)
print "converting lines...\n";
my $doc_count = 0;
while ($doc = <INPUT>) {

	if ($doc_count % 1000 == 0) {
		print "$doc_count...\n";
	}

	chomp($doc);
	@word_pairs = split /\s/, $doc;
	
	print OUTPUT "$doc_count";
	$doc_count++;
	
	for($i=1; $i<scalar @word_pairs; $i++) {
		@pair = split /:/, $word_pairs[$i];
		
		if ($pair[0] < scalar @words) {
			print OUTPUT " $words[$pair[0]]|$pair[1]";
		}
	}
	
	print OUTPUT "\n";
}

close INPUT;
close OUTPUT;