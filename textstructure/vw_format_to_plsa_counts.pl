#!/usr/bin/perl

my $doc_words = shift;
my $vocabulary= shift;
my $plsa_file = shift;

#get word id mapping from vocab file

my @words = ();

open VOCAB, "<$vocabulary" or die $!;
while ($word = <VOCAB>) {
	chomp($word);
	push @words, $word;
}
close VOCAB;

open INPUT, "<$doc_words" or die $!;
open OUTPUT, ">$plsa_file" or die $!;

my $doc_count = 0;
while ($doc = <INPUT>) {
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