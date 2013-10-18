#!/opt/anaconda/bin/python

###!/usr/bin/env python

#from __future__ import division
#import os
import sys

prev_word = ""
first_line = 1
running_sum = 0

for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    items = line.split("\t")
    
	# ensure that the entry is in <> \t <> form
    if len(items) == 2:
		word = items[0]
		count = int(items[1])

		#write value for previous word and reset
		if (word != prev_word):
			if (first_line):
				first_line = 0
			else:
				print '%s\t%d,DC' % (prev_word, running_sum)
			running_sum = 0
			prev_word = word

		#increment current word
		running_sum = running_sum + count

# write last line
print '%s\t%d,DC' % (prev_word, running_sum)