BIN = ../../bin

all: $(BIN)/chowtest

clean: 
	rm -f $(BIN)/chowtest

$(BIN)/chowtest: main.cpp ChowLiuTree.cpp NaiveBayes.cpp ChowLiuTree.h NaiveBayes.h InterfaceDetectorModel.h InterfaceObservationLikelihood.h
	g++ -g -o $(BIN)/chowtest main.cpp ChowLiuTree.cpp NaiveBayes.cpp
