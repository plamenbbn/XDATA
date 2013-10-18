#ifndef NAIVEBAYES_H_
#define NAIVEBAYES_H_

#include <iostream>
#include <vector>
#include <ctime>
#include <fstream>
#include <math.h>
#include "InterfaceObservationLikelihood.h"
#include "InterfaceDetectorModel.h"
//#include "InterfacePlaceModel.h"

//const std::string NaiveBayesFilename = "NaiveBayes.txt";

class NaiveBayes : public InterfaceObservationLikelihood
{
public :
	// constructor which trains the model using the training_data and saves the model into pModelFile
	NaiveBayes(std::vector<std::vector<int> >& training_data, std::string pModelFile, bool calculateModel=false);
	// constructor which loads the model from pModelFile
	NaiveBayes(std::string pModelFile);
	~NaiveBayes();

	// returns p(Z_k | L_i) as defined in equations (5)-(8) for the NaiveBayes, i=location, Z_k=observations
    /*	double evaluate(std::vector<int>& observations, int location, InterfaceDetectorModel* detectorModel, InterfacePlaceModel* placeModel); */

	// returns the marginal probability for observing a single attribute p(z_attr = val)
	double getMarginalPriorProbability(int attr, int val) { return mMarginalPriorProbability[attr][val]; };

	// returns the whole marginal probabilities vector
	std::vector<std::vector<double> >& getMarginalPriorProbabilities() { return mMarginalPriorProbability; };

	// returns p(Z_k | L_u) for a randomly sampled place L_u with randomly sampled obervations Z_k as needed in equation (17)
	// observation = Z_k
	double sampleNewPlaceObservation(InterfaceDetectorModel* detectorModel, std::vector<int>& observation);

	// returns p(Z_k | L_{avg}) for the average place L_{avg} as needed in equation (16)
	double meanFieldNewPlaceObservation(InterfaceDetectorModel* detectorModel, const std::vector<int>& observation);

private:
	// calculates the marginal probabilities of the attributes for generative model from the training data
	void generateMarginalProbabilities();

	// saves the Naive Bayes model (i.e. the marginals)
	void saveModel(std::string pModelFile);
	
	// loads the Naive Bayes model from file (i.e. the marginals)
	void loadModel(std::string filename);

	//the training data in the form [image[histogram of image]] = [sample][attribute]
	std::vector<std::vector<int> > mTrainingData;

	//discrete attribute sizes
	std::vector<int> mAttributeSizes;

	// stores the marginal probability for observing a single attribute p(z_attr = val) as mMarginalPriorProbability[attr][val]
	std::vector<std::vector<double> > mMarginalPriorProbability;

};

#endif /* NAIVEBAYES_H_ */
