Example run:
-------------------------------------------------------------------
This directory includes the MIT LL topic clustering code.  Included
with the distribution is an example based on a small set of the Kiva
data.  To run an example, from the main directory run:

./run_all.py

The output of the code will be in:

tmp/journalEntries.csv.50.summary.txt

This contains the topics indices and summarizing keywords.  E.g.,
---- ------ ----- ------ -----  ----------------
            Topic    Doc  % of 
   #  Index Score Purity  Docs  Summary
---- ------ ----- ------ -----  ----------------
   1 (  30)  8.48  0.763 11.11  items fellowship groups community week partners result inventory weekly beverages
   2 (  37)  6.60  0.721  9.15  fellowship week groups community educational partners expenditures weekly gladness thankfulness
   3 (  24)  3.19  0.709  4.50  member bank continues communal belongs ahead city puts person effort
   4 (  36)  2.91  0.735  3.95  resources del located country limited small people decent worked economically
   5 (  15)  2.23  0.707  3.16  profit increased received supported purchased monthly click rose stock fundraised
...

The topics are sorted by a measure of "goodness" of the particular
topic.  The leftmost column (1, 2, 3, 4, ...) is the index used for
the topic label.

Topic probabilities, p(topic|document), are given in the file:
tmp/journalEntries.csv.50.d2z.txt
The format of the file is:
p(topic1|doc) p(topic2|doc) ....
The "rows" of the file correspond to the Kiva ids listed in the 
tmp/journalEntries.csv.50.counts.txt 
in column1.

The JSON version of topic summaries and topic probabilities per kiva id are given in the file:
serialized/journalEntries.csv.50.json

Source:
-------------------------------------------------------------------
- The text normalization and ingestion is in Python 2.7.
- The topic clustering is written in C.
- The system has been tested under Linux.
- The topic code can be compiled by changing to the directory, 
topic/src/plsa and typing make.
