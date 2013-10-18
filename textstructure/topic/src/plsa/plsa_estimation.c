/* DARPA XDATA licenses this file to You under the Apache License, Version 2.0
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
*/

/*Edited version of code from MIT-Lincoln lab deployed under the following header - our code generaly begins
at line 224/


/* -*- C -*-
 *
 * Copyright (c) 2010
 * MIT Lincoln Laboratory
 * Massachusetts Institute of Technology
 *
 * All Rights Reserved
 *
 * FILE: plsa_estimation_combined_file.c
 * Adapted from TJ Hazen's code to use a "combined" file -- a file with document counts per line.
 * BC: 5/09/13
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <float.h>
#include <time.h>
#include "util/basic_util.h"
#include "util/args_util.h"
#include "util/hash_util.h"
#include "classifiers/classifier_util.h"
#include "plsa/clustering_util.h"
#include "plsa/plsa.h"
#include "porter_stemmer/porter_stemmer.h"

/* Function prototypes */
char **create_labels_list ( SPARSE_FEATURE_VECTORS *feature_vectors, CLASS_SET *classes );
void write_z_given_d (PLSA_MODEL *plsa_model, char *filename);
void write_w_given_z (PLSA_MODEL *plsa_model, char *filename);
void write_W (PLSA_MODEL *plsa_model, char *filename);
void write_Z (PLSA_MODEL *plsa_model, char *filename);
void print_post_topics_words (PLSA_MODEL *plsa_model, char *filename);
void print_post_topics_docs (SPARSE_FEATURE_VECTORS *feature_vectors, PLSA_MODEL *plsa_model, char *filename);


/* Main Program */
int main(int argc, char **argv)
{
  // Set up argument table
  ARG_TABLE *argtab = NULL;
  argtab = llspeech_new_string_arg(argtab, "vector_list_in", NULL,
				   "Input file containing a list of labeled feature vector files");
  argtab = llspeech_new_string_arg(argtab, "feature_list_in", NULL, 
				  "List of terms to use in feature set");
  argtab = llspeech_new_string_arg(argtab, "feature_list_out", NULL, 
				  "Output file containing list of terms used in feature set");
  argtab = llspeech_new_string_arg(argtab, "stop_list_in", NULL, 
				  "List of terms to exclude from feature set");
  argtab = llspeech_new_float_arg(argtab, "df_cutoff", 0.5, 
				  "Exclude terms that happen in greater than this fraction of vectors");
  argtab = llspeech_new_float_arg(argtab, "tf_cutoff", 5.0, 
				  "Exclude terms that occur this number of times or fewer in the data");
  argtab = llspeech_new_float_arg(argtab, "alpha", 0.001, 
				  "Smoothing parameter for topic model P(z|d)");
  argtab = llspeech_new_float_arg(argtab, "beta", 0.001, 
				  "Smoothing parameter for word model P(w|z)");
  argtab = llspeech_new_int_arg(argtab, "num_topics", 1,
				"Number of latent PLSA topics");
  argtab = llspeech_new_int_arg(argtab, "max_iter", 500,
				"Maximum number of PLSA training iterations");
  argtab = llspeech_new_float_arg(argtab, "convergence", 0.001,
				"Average likelihood convergence threshhold");
  argtab = llspeech_new_flag_arg(argtab, "random", "Do a random seeding initialization of the PLSA topics");
  argtab = llspeech_new_flag_arg(argtab, "list_stemming", "Do Porter stemming to remove redundant signature words");
  
  argtab = llspeech_new_string_arg(argtab, "z_given_d_out", NULL, "Output file containing ascii doc/topic matrix");
  argtab = llspeech_new_string_arg(argtab, "w_given_z_out", NULL, "Output file containing ascii topic/word matrix");
  argtab = llspeech_new_string_arg(argtab, "w_out", NULL, "Output file containing ascii word vector");
  argtab = llspeech_new_string_arg(argtab, "z_out", NULL, "Output file containing ascii topic vector");
  argtab = llspeech_new_string_arg(argtab, "summary_out", NULL, "Output file containing key topic words");
  argtab = llspeech_new_string_arg(argtab, "z_given_w_post_out", NULL, "Output file containing word/topic matrix");
  argtab = llspeech_new_string_arg(argtab, "z_given_d_post_out", NULL, "Output file containing doc/topic matrix");
  
  /* Parse the command line arguments */ 
  argc = llspeech_args(argc, argv, argtab);

  // Extract command line argument settings
  char *vector_list_in = (char *) llspeech_get_string_arg(argtab, "vector_list_in");
  char *feature_list_in = (char *) llspeech_get_string_arg(argtab, "feature_list_in");
  char *feature_list_out = (char *) llspeech_get_string_arg(argtab, "feature_list_out");
  char *stop_list_in = (char *) llspeech_get_string_arg(argtab, "stop_list_in");
  char *z_given_d_out = (char *) llspeech_get_string_arg(argtab, "z_given_d_out");
  char *w_given_z_out = (char *) llspeech_get_string_arg(argtab, "w_given_z_out");
  char *w_out = (char *) llspeech_get_string_arg(argtab, "w_out");
  char *z_out = (char *) llspeech_get_string_arg(argtab, "z_out");
  char *summary_out = (char *) llspeech_get_string_arg(argtab, "summary_out");
  char *z_given_w_post_out = (char *) llspeech_get_string_arg(argtab, "z_given_w_post_out");
  char *z_given_d_post_out = (char *) llspeech_get_string_arg(argtab, "z_given_d_post_out");
  
  float df_cutoff = llspeech_get_float_arg(argtab, "df_cutoff");
  float tf_cutoff = llspeech_get_float_arg(argtab, "tf_cutoff");
  float alpha = llspeech_get_float_arg(argtab, "alpha");
  float beta = llspeech_get_float_arg(argtab, "beta");
  int num_topics = llspeech_get_int_arg(argtab, "num_topics");
  int max_iter = llspeech_get_int_arg(argtab, "max_iter");
  float conv_threshold = llspeech_get_float_arg(argtab, "convergence");
  int random = llspeech_get_flag_arg(argtab, "random");
  int stem_list = llspeech_get_flag_arg(argtab, "list_stemming");
  
  time_t begin_time, start_time, end_time;

  if ( vector_list_in == NULL ) {
    fprintf ( stderr, "\nArgument list:\n");
    llspeech_args_prusage(argtab);
    die ( "Must specify argument -vector_list_in\n");
  }

  if ( alpha < 0 ) die ( "-alpha parameter cannot be negative\n");
  if ( beta < 0 ) die ( "-beta parameter cannot be negative\n");
  if ( max_iter < 0 ) die ( "-max_iter parameter must non-negative\n");
  if ( num_topics < 1 ) die ( "-num_topics parameters must be set to a positive value\n");

  time(&begin_time);

  FEATURE_SET *stop_list = NULL;
  if (stop_list_in != NULL) {
    printf ("(Loading stop list..."); fflush(stdout);
    stop_list = load_feature_set ( stop_list_in );
    printf("done)\n");
  }

  FEATURE_SET *features = NULL;
  if ( feature_list_in != NULL ) {
    printf ("(Loading feature list..."); fflush(stdout);
    features = load_feature_set ( feature_list_in );
    printf("done)\n");
  }

  // Load feature set from features observed in training data 
  if ( features == NULL ) {
    printf("(Creating feature set from training files..."); fflush(stdout);
    time(&start_time);
    features = create_feature_set_from_file (vector_list_in, 0, stop_list);
    time(&end_time);
    printf("done in %d seconds)\n",(int)difftime(end_time,start_time));
  }
  
  // Add some count info into the feature set about multiword units
  add_word_count_info_into_feature_set (features, stop_list);

  // Load list of classes
  CLASS_SET *classes = NULL;

  printf("classes is : %p\n", classes);
  
  // Load training set feature vectors
  printf("(Loading feature vectors..."); fflush(stdout);
  time(&start_time);
  SPARSE_FEATURE_VECTORS *feature_vectors = load_sparse_feature_vectors_combined (vector_list_in, features, classes);
  time(&end_time);
  printf("done in %d seconds)\n",(int)difftime(end_time,start_time));
  
  //Learn feature weights for features
  printf("(Learning feature weights..."); fflush(stdout);
  learn_feature_weights ( feature_vectors, df_cutoff, tf_cutoff, 0, IDF_WEIGHTING, 0 ); 
  printf("done)\n");

  // Prune features whose feature weight is zero 
  // from feature set and feature vectors
  printf("(Prune zero weight features..."); fflush(stdout);
  prune_zero_weight_features_from_feature_vectors(feature_vectors);
  printf("done)\n");
 
  // Save the pruned feature set to file if requested
  if ( feature_list_out != NULL ) {
    printf("(Writing feature set to file '%s'...",feature_list_out); fflush(stdout);
    save_feature_set ( features, feature_list_out );
    printf("done)\n");
  }

  // Compute initial assignments of vectors to clusters 
  int *vector_labels = NULL;
  if ( random ) {
    //vector_labels = random_clustering ( feature_vectors, num_topics );
    vector_labels = kmeans_clustering ( feature_vectors, num_topics, 20 );
  } else {
    printf("(Applying feature weights..."); fflush(stdout);
    apply_feature_weights_to_feature_vectors ( feature_vectors );
    printf ("done)\n");

    vector_labels = deterministic_clustering ( feature_vectors, num_topics );

    printf("(Remove zero weight features from feature set..."); fflush(stdout);
    remove_zero_weight_features ( features );
    printf ("done)\n");
    
    printf("(Reloading feature vectors..."); fflush(stdout);
    free_sparse_feature_vectors ( feature_vectors );
    feature_vectors = load_sparse_feature_vectors ( vector_list_in, features, classes );
    printf("done)\n");
  } 
  
  time(&end_time);
  printf ("(Total preprocessing time: %d seconds)\n",(int)difftime(end_time,begin_time));
  time(&begin_time);

  // Estimating the PLSA model
  PLSA_MODEL *plsa_model = train_plsa_model_from_labels ( feature_vectors, vector_labels, 
							  num_topics, alpha, beta, max_iter,
							  conv_threshold, 0 );

  time(&end_time);
  printf ("(Total training time: %d seconds)\n",(int)difftime(end_time,begin_time));
  
  //print z_given_d
  if (z_given_d_out) 
	write_z_given_d (plsa_model, z_given_d_out);
    
  //print_w_given_z
  if (w_given_z_out)
	write_w_given_z (plsa_model, w_given_z_out); 
  
  //print W
  if (w_out)
	write_W (plsa_model, w_out);
    
  //print_Z
  if (z_out)
	write_Z (plsa_model, z_out); 
  
  //summarize and print labels
  if (summary_out) {
	PLSA_SUMMARY *plsa_summary = summarize_plsa_model ( plsa_model, 1 );
	print_plsa_summary ( plsa_summary, 0, summary_out );
  }
  
  //get posterior probs for topics words
  if (z_given_w_post_out)
	print_post_topics_words (plsa_model, z_given_w_post_out);
  
  //get posterior probs for topics docs
  //run like nb classifier
  if (z_given_d_post_out)
	print_post_topics_docs (feature_vectors, plsa_model, z_given_d_post_out);
	
	return 0;
}

//functions to print generated matrices in ascii format
void write_z_given_d (PLSA_MODEL *plsa_model, char *filename) {
  int d, z;
  FILE *fp = fopen_safe (filename, "w" );
  for (d=0; d<plsa_model->num_documents; d++ ) {
    for (z=0; z<plsa_model->num_topics; z++ ) {
      fprintf( fp, " %.6f", plsa_model->P_z_given_d[z][d] );
    }
    fprintf (fp, "\n");
  }
  fclose(fp);
}

void write_w_given_z (PLSA_MODEL *plsa_model, char *filename) {
  int z, w;
  FILE *fp = fopen_safe (filename, "w" );
  
  for (z=0; z<plsa_model->num_topics; z++) {
    for (w=0; w<plsa_model->num_features; w++ )  {
		fprintf( fp, " %.6f", plsa_model->P_w_given_z[w][z] );
    }
	fprintf (fp, "\n");
  }
  fclose(fp);
}

void write_W (PLSA_MODEL *plsa_model, char *filename) {
  int w;
  FILE *fp = fopen_safe (filename, "w" );
  
  for (w=0; w<plsa_model->num_features; w++ )  {
	fprintf( fp, " %.6f", plsa_model->P_w[w]);
  }
 
  fclose(fp);
}

void write_Z (PLSA_MODEL *plsa_model, char *filename) {
  int z;
  FILE *fp = fopen_safe (filename, "w" );
  
  for (z=0; z<plsa_model->num_topics; z++ )  {
	fprintf( fp, " %.6f", plsa_model->P_z[z] );
  }
  fclose(fp);
}

//get posteriors for words to topics
void print_post_topics_words (PLSA_MODEL *plsa_model, char *filename) {
	int z;
	int w;
	double sum, lSum, lMax;
	double *post, *lPost;
	
	post = (double*) malloc (sizeof(double) * plsa_model->num_topics);
	lPost = (double*) malloc (sizeof(double) * plsa_model->num_topics);
	
	FILE *fp = fopen_safe (filename, "w" );
	
	//(t|w) = (w|t) * (t) / (w)
	for (w = 0; w < plsa_model->num_features; w++) {
		for (z = 0; z < plsa_model->num_topics; z++) {
			lPost[z] = log(plsa_model->P_w_given_z[w][z]) + log(plsa_model->P_z[z]) - log(plsa_model->P_w[w]);
		}	
	
		//recast as ratios to maximum log value
		lMax = -DBL_MAX;
		for (z = 0; z < plsa_model->num_topics; z++)
			if (lPost[z] > lMax)
				lMax = lPost[z];
					
		//exponeniate ratio and sum
		lSum = 0;
		for (z = 0; z < plsa_model->num_topics; z++) {
			lPost[z] = exp(lPost[z] - lMax);
			lSum += lPost[z];
		}
				
		//normalize
		for (z = 0; z < plsa_model->num_topics; z++)
			lPost[z] /= lSum;
		
		//print
		for (z = 0; z < plsa_model->num_topics; z++) {
			fprintf(fp, " %.6f", lPost[z]);
		}
		
		fprintf(fp, "\n");
	
	}
	
	fclose(fp);
}

//run through the docs agian as if a test set in a naive bayes classifier
void print_post_topics_docs (SPARSE_FEATURE_VECTORS *feature_vectors, PLSA_MODEL *plsa_model, char *filename) {
	int d, f, w, z, cnt;
	double lMax, lSum;
	SPARSE_FEATURE_VECTOR *vector;
	double *ll; 
	
	ll = (double*) malloc (sizeof(double) * plsa_model->num_topics);
	
	FILE *fp = fopen_safe (filename, "w" );
	
	//run as if naive bayes test set to get posterior probabilities for each topic for each document
	for (d = 0; d < plsa_model->num_documents; d++) {
		
		//for each topic log(p_z) + sum(log (w_given_z's))
		for (z = 0; z < plsa_model->num_topics; z++) {
		
			ll[z] = log(plsa_model->P_z[z]);
			
			vector = feature_vectors->vectors[d];
			for (f = 0; f < vector->num_features; f++) {
				
				w = vector->feature_indices[f];
				cnt = vector->feature_values[f];
				
				ll[z] += cnt * log(plsa_model->P_w_given_z[w][z]);
			}
		}
				
		//normalize
		lMax = -DBL_MAX;
		for (z = 0; z < plsa_model->num_topics; z++)
			if (ll[z] > lMax)
				lMax = ll[z];
			
		//exponeniate ratio and sum
		lSum = 0;
		for (z = 0; z < plsa_model->num_topics; z++) {
			ll[z] = exp(ll[z] - lMax);
			lSum += ll[z];
		}
				
		//normalize
		for (z = 0; z < plsa_model->num_topics; z++)
			ll[z] /= lSum;
		
		//print
		for (z = 0; z < plsa_model->num_topics; z++)
			fprintf(fp, " %.6f", ll[z]);	

		fprintf(fp, "\n");
	}
	
	fclose(fp);
}
	
	
			
			
		
		
		
	
	
	