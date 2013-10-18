#!/opt/anaconda/bin/python
###!/usr/bin/env python

from math import log
import sys
import re

#get command options
tfOption = sys.argv[1]

#if there's a stoplist create stoplist 
stoplist = {}
if len(sys.argv) == 5:
	stopfile = open(str(sys.argv[2]), 'r')
	
	for line in stopfile:
		#put into a hash
		line = line.strip()
		line = line.lower()
	
		stoplist[line] = 1
		
#get what language we are checking and pre-processing options from the command line
language = sys.argv[3] if len(sys.argv) == 5 else sys.argv[2]
language = False if language == '0' else language
preProc =  sys.argv[4] if len(sys.argv) == 5 else sys.argv[3]
preProc = True if preProc == '1' else False

#load language detection module		
if language:
	import zipimport
	importer = zipimport.zipimporter('langid.mod')
	langid = importer.load_module('langid')
	
wordcounts = {}
totalcount = 0

for line in sys.stdin:
	#empty local hash
	localWords = {}
	
    #check language
	if language:
		langDetected = langid.classify(line)
	
	#if it's the right language or if we are taking everything
	if not language or langDetected[0] == language:
			
		# remove leading and trailing whitespace
		line = line.strip()
		
		# split the line into words
		words = line.split()
	
		for word in words:
		
			valid = True;
		
			#pre-processing
			if preProc:
				word = word.lower()
				
				#remove leading punctiation
				word = re.sub('^[\'"({-]', '', word)
						
				#remove trailing punction
				word = re.sub('[\'"-)};:,.?!]$', '', word)
				
				#remove 's
				word = re.sub('\'s$', '', word)
							
				#limit words to letters and one internal apostraphe or hyphen
				valid = re.match('^[a-zA-Z]+[\'-]?[a-zA-Z]+$', word)
					
			#add word to local hash
			if valid and not word in stoplist:
				localWords[word] = 1 + localWords.get(word, 0)
				
		#get the count to add to the global hash depending on the aproach
		#also add whatever contribution to the total count
		for word in localWords:
			if tfOption == 'count':
				wordcounts[word] = wordcounts.get(word,0) + localWords.get(word,0)
				totalcount += localWords.get(word,0)
			if tfOption == 'log':
				wordcounts[word] = wordcounts.get(word,0) + 1 + log(localWords.get(word,0)) 
				totalcount += 1 + log(localWords.get(word,0)) 
			if tfOption == 'boolean':
				wordcounts[word] = wordcounts.get(word,0) + 1
				totalcount += 1
			
for word in wordcounts:
    print '%s\t%f' % (word, wordcounts[word])

print '_TF_TOTAL_WORD_COUNT\t%f' % (totalcount)
