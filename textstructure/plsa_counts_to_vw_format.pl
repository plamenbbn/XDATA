#!/usr/bin/perl

my $plsa_file = shift;
my $doc_words = shift;
my $vocabulary= shift;

open INPUT, "<$plsa_file" or die $!;
open OUTPUT, ">doc_words" or die $!;

my %words = ();
my $word_count = 0;

while ($doc = <INPUT>) {
	chomp($doc);
	@word_pairs = split /\s/, $doc;
	
	print OUTPUT "|";
	
	for($i=1; $i<scalar @word_pairs; $i++) {
	
		@pair = split /\|/, $word_pairs[$i];
	
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