#!/usr/bin/env python

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

import sys
import langid
from optparse import OptionParser
import re

#get csv file and the column to extract text from
if len(sys.argv) >= 3:
	inputFile = sys.argv[1]
	column = int(sys.argv[2])
	outputFile = sys.argv[3]

#get options
usage = "usage: %CSVtoVW input column output [options]"
parser = OptionParser(usage=usage)
parser.add_option("--vw", action="store_true", dest="vwFormat", default=False,
    help="print the counts file in a vw compatible format")
parser.add_option("--plsa", action="store_true", dest="plsaFormat", default=False,
    help="print the counts file in a plsa compatible format")
parser.add_option("--delim", action="store", type="string", dest="delim", default="\t",
    help="delimeter used in the input file")
parser.add_option("--vocab", action="store_true", dest="vocab", default=False,
    help="output a vocabulary file")
parser.add_option("--language", action="store", type="string", dest="language", default="",
    help="language to limit the output to")
parser.add_option("--preProc", action="store_true", dest="preProc", default=False,
    help="perform pre-processing on the text")
(options, args) = parser.parse_args()

if inputFile and column and outputFile:
	#open the input file
	csv = open(inputFile, 'r')

	#open the output file
	counts = open(outputFile + '.counts', 'w')

	if options.vocab:
		vocabFile = open(outputFile + '.vocab', 'w')

	#dictionary to store full vocabulary
	vocab = {}

	#doc/word counters
	docNum = 0
	wordNum = 0
	counter = 0

	#loop through the csv
	for line in csv:

		if counter % 5000 == 0:
			print "{}".format(counter)
		counter+=1
		
		#get the specified text field
		fields = line.split(options.delim)
		text = fields[column]
		
		#create/clear dictionary for the current document
		localCounts = {}
		
		#detect language if selected
		if options.language:
			langDetected = langid.classify(text)
		
		#if it's the right language or if we are taking everything
		if not options.language or langDetected[0] == options.language:

			#remove leading and trailing whitespace
			text = text.strip()
			
			# split the line into words
			words = text.split()
			
			for word in words:
				valid = True
			
				#pre-processing
				if options.preProc:
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
				if valid: #and not word in stoplist:
					
					#increment doc-word counter
					localCounts[word] = 1 + localCounts.get(word, 0)
					
					#check if it's in the vocabulary - if not add with the next number
					if options.vwFormat or options.vocab:
					
						if not vocab.get(word):
					
							vocab[word] = wordNum
							wordNum+=1
						
							#print to vocab list is specified
							if options.vocab:
								vocabFile.write("%s\n" % word)
							
					
		#format is | wordIdx:count wordIdx:count
		if options.vwFormat:
			counts.write('|')
		
			for word in localCounts:
				counts.write (" %d:%d" % (vocab[word], localCounts[word]))
				
			counts.write('\n')
				
			
		#format is <docNum word|count word|count....
		if options.plsaFormat:
			#print document number
			counts.write("%d" % docNum)
			
			#print words
			for word in localCounts:
				counts.write("%s|%d" % (word, localCounts[word]))
				
			counts.write('\n')
			
		#increment doc count
		docNum += 1

	#close files
	csv.close()		
	counts.close()

	if options.vocab:
		vocabFile.close()
		
else:
	print "usage: ./CSVtoVW.py input column output [options]";