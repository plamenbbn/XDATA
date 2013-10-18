#!/bin/sh

if [ $# -ne 5 ]
  then
    echo "Usage: $0 hdfs-path-to-input-docs hdfs-path-to-corpus hdfs-output-dir num-words-in-cloud output-prefix"
    exit 1
fi

TF_DOC_STORE=$1       # /user/bigdata/ner_kiva10
DF_DOC_STORE=$2       # /user/bigdata/ner_kiva
HDFS_OUTPUT_BASE=$3   # /user/bigdata/mdecerbo/tfidf1
WORDS_IN_CLOUD=$4     # 30
OUTPUT_PREFIX=$5

hadoop fs -test -d $HDFS_OUTPUT_BASE
TestedDir=$?
if [ $TestedDir -eq 0 ]; then
 echo "$HDFS_OUTPUT_BASE already exists, cowardly refusing to delete it"
 exit 1
fi

echo "Creating $HDFS_OUTPUT_BASE"
hadoop fs -mkdir $HDFS_OUTPUT_BASE

# 1. Count terms
hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-streaming-2.0.0-mr1-cdh4.2.0.jar \
   -file tf_mapper.py tf_reducer.py langid.mod stop_list.txt \
   -inputformat 'SequenceFileAsTextInputFormat' \
   -mapper  "python tf_mapper.py" \
   -reducer "python tf_reducer.py" \
   -input $TF_DOC_STORE \
   -output $HDFS_OUTPUT_BASE/tc \

#2. Get word count
TOTAL_WORDS=`hadoop fs -text $HDFS_OUTPUT_BASE/tc/part-* | fgrep _TF_TOTAL_WORD_COUNT | awk '{print $2}' | awk -F, '{print $1}'`

echo
echo Counted words, got $TOTAL_WORDS words. 

#3. Get document counts.
hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-streaming-2.0.0-mr1-cdh4.2.0.jar \
   -file df_mapper.py df_reducer.py langid.mod stop_list.txt \
   -inputformat 'SequenceFileAsTextInputFormat' \
   -mapper  "python df_mapper.py" \
   -reducer "python df_reducer.py" \
   -input  $DF_DOC_STORE \
   -output $HDFS_OUTPUT_BASE/dc

# 4. Get total document count.
TOTAL_DOCS=`hadoop fs -text $HDFS_OUTPUT_BASE/dc/part-* | fgrep _DF_TOTAL_DOC_COUNT | awk '{print $2}' | awk -F, '{print $1}'`

echo 
echo Counted documents, got $TOTAL_DOCS documents.


#    -D num.key.fields.for.partition=1 \

# 5. Use output from previous stages to get TF-IDF by word, sorted
hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-streaming-2.0.0-mr1-cdh4.2.0.jar \
   -D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator  \
   -D mapred.text.key.comparator.options=-k1nr \
   -D num.key.fields.for.partition=1 \
   -D stream.num.map.output.key.fields=1 \
   -D mapred.child.java.opts=-Xmx2G \
   -file tfidf_reducer.py \
   -mapper  "cat" \
   -reducer "python tfidf_reducer.py" \
   -partitioner org.apache.hadoop.mapred.lib.KeyFieldBasedPartitioner \
   -cmdenv DF_TOTAL_DOC_COUNT=$TOTAL_DOCS \
   -cmdenv TF_TOTAL_WORD_COUNT=$TOTAL_WORDS \
   -input   $HDFS_OUTPUT_BASE/dc \
   -input   $HDFS_OUTPUT_BASE/tc \
   -output  $HDFS_OUTPUT_BASE/tfidf

echo Created TF-IDF scores in $HDFS_OUTPUT_BASE/tfidf .

hadoop fs -text $HDFS_OUTPUT_BASE/tfidf/part-* | sort -k1nr | head -$WORDS_IN_CLOUD | \
 perl WordStatsToCloud.pl -cloudsize $WORDS_IN_CLOUD -in - -out ${OUTPUT_PREFIX}_cloud${WORDS_IN_CLOUD}.html

echo Created HTML word cloud in ${OUTPUT_PREFIX}_cloud${WORDS_IN_CLOUD}.html .



   



