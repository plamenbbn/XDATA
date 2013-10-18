#!/opt/anaconda/bin/python

###!/usr/bin/env python

#from __future__ import division
import os
import sys

first_line = 1
prev_word = ""
running_sum = 0

for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    items = line.split("\t")
    
	# ensure that the entry is in <> \t <> form
    if len(items) == 2:
		word = items[0]
		count = float(items[1])

		#counts for a particular word are over - write and reset
		if (word != prev_word):
			
			if (first_line):
				first_line = 0
			else:
				print '%s\t%f,TC' % (prev_word, running_sum)
				running_sum = 0
				prev_word = word

		#still incrementing a particular word
		running_sum = running_sum + count

#write the last word
print '%s\t%f,TC' % (prev_word, running_sum)


