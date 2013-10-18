#!/opt/anaconda/bin/python
###!/usr/bin/env python

import sys
import re

#if there's a stoplist create stoplist 
stoplist = {}
if len(sys.argv) == 4:
	stopfile = open(str(sys.argv[1]), 'r')
	
	for line in stopfile:
		#put into a hash
		line = line.strip()
		line = line.lower()
	
		stoplist[line] = 1
	
#get what language we are checking and pre-processing options from the command line
language = sys.argv[2] if len(sys.argv) == 4 else sys.argv[1]
language = False if language == '0' else language
preProc =  sys.argv[3] if len(sys.argv) == 4 else sys.argv[2]
preProc = True if preProc == '1' else False

#load language detection module		
if language:
	import zipimport
	importer = zipimport.zipimporter('langid.mod')
	langid = importer.load_module('langid')

globalwordpresent = {}
doccount = 0

# input comes from STDIN (standard input)
for line in sys.stdin:
	#empty local hash
	localwordpresent = {}
	
	#check language
	if language:
		langDetected = langid.classify(line)
	
	#if it's the right language or if we are taking everything
	if not language or langDetected[0] == language:

		valid = True;
		
		#increment counter
		doccount += 1
		
		# remove leading and trailing whitespace
		line = line.strip()
		
		# split the line into words
		words = line.split()
		
		for word in words:
		
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
			
		        localwordpresent[word] = 1
			
		#add word to global
		for word in localwordpresent:
			globalwordpresent[word] = 1 + globalwordpresent.get(word,0);
		
for word in globalwordpresent:
    print '%s\t%d' % (word, globalwordpresent[word])

print '_DF_TOTAL_DOC_COUNT\t%d' % doccount
