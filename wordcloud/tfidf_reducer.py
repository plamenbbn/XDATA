#!/opt/anaconda/bin/python

#from __future__ import division
from math import log, sqrt
from operator import itemgetter
import os
import sys

def sbv(d,reverse=False):
    ''' sort by value from PEP 265 '''
    return sorted(d.iteritems(), key=itemgetter(1), reverse=reverse)

#calculate td*idf
def estimate_tfidf(tf,df, dfOption):
    tfidf = -1.0
	
    #basic range error checking
    if tf <= 0.0 or df >= 1.0:
        tfidf = 0.0
   
    else:	
	#value where term is not found in document count corpus
	#should be programmable?
	
	if df <= 0:
		df = .1
			
		#apply scaling if requested
		if dfOption == 'scale':
			df = sqrt(df) 
		
        tfidf = -1.0 * tf * log(df)

    return tfidf

#get command line options regarding scaling
dfOption = None
if len(sys.argv) > 1:
	dfOption = sys.argv[1]
	
#Get totals from environ 
totalDocs  = float(os.environ.get("DF_TOTAL_DOC_COUNT",  1.0))
totalTerms = float(os.environ.get("TF_TOTAL_WORD_COUNT", 1.0))

tc = {}
dc = {}
words = {}

for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    items = line.split("\t")
    
    #ensure that the input is in the form <> \t <>
    if len(items) == 2:
		word = items[0]
    
		#ignore entries that hold count statistics
		if (word == "_TF_TOTAL_WORD_COUNT"):
			continue
		if (word  == "_DF_TOTAL_DOC_COUNT"):
			continue
   
		#get the count and type of the entry
		words[word] = 1
		countandtype = items[1].split(',')
		count = float(countandtype[0])
		tcORdc = countandtype[1]

		#Get document count and term count
		#There should be one entry per word - this sends a warning through sterr but then increments the count anyway
		if (tcORdc == "DC"):
			thisWordDC = dc.get(word, 0)
        
			if (thisWordDC != 0):
				sys.stderr.write('reporter:counter:TFIDF,Duplicate Document Count Records,1')
			
			dc[word] = thisWordDC + count
    
		#equivalent for term counts
		elif (tcORdc == "TC"):
			thisWordTC = tc.get(word, 0)
       
			if (thisWordTC != 0):
				sys.stderr.write('reporter:counter:TFIDF,Duplicate Term Count Records,1')
				
			tc[word] = thisWordTC + count
    
		else:
			sys.stderr.write('reporter:counter:TFIDF,Unknown Records,1')  

#iterate through the words to get tfidf			
tfidf = {}
for word in words:
    thisWordTC = tc.get(word, 0)
    thisWordDC = dc.get(word, 0)
    
    #convert counts to frequencies
    tf = thisWordTC / totalTerms
	
    #converts count to frequency applying sqrt scaling if requested 
    df = thisWordDC / totalDocs
	 
    #get tfidf
    tfidf[word] = estimate_tfidf(tf, df, dfOption)

#sort tfidf dictionary keys
s = sbv(tfidf, reverse=True)

#write in the format word tfidf tf df
for record in s:
    word  = record[0]
    tfidf = record[1]
    tf = tc.get(word, 0) / totalTerms
    df = dc.get(word, 0) / totalDocs
    if df <= 0:
	df = .01
    if dfOption == 'scale':
    	df = sqrt(df)
		
    print '%f\t%s\t%f\t%f' % (tfidf, word, tf, df)



