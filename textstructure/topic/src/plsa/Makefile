BIN = ../../bin

SRC_DIR = ..

UTIL_DIR = $(SRC_DIR)/util

CLASSIFIER_DIR = $(SRC_DIR)/classifiers

STEMMER_DIR = $(SRC_DIR)/porter_stemmer

UTILS = $(UTIL_DIR)/basic_util.c \
	$(UTIL_DIR)/args_util.c \
	$(UTIL_DIR)/hash_util.c \
	$(CLASSIFIER_DIR)/classifier_util.c \
	$(STEMMER_DIR)/porter_stemmer.c

PROG = $(BIN)/plsa_estimation

CFLAGS = -O3 -Wall -static 

all : $(PROG)
clean :
	rm -f $(PROG)

	
$(BIN)/plsa_estimation : plsa_estimation.c clustering_util.c plsa.c 
	gcc $(CFLAGS) -o $@ $< clustering_util.c plsa.c $(UTILS) -lm -I$(SRC_DIR)




