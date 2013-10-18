
#ifndef INTERFACEOBSERVATIONLIKELIHOOD_H_
#define INTERFACEOBSERVATIONLIKELIHOOD_H_

#include <vector>
#include <string>
#include "InterfaceDetectorModel.h"
//#include "InterfacePlaceModel.h"


class InterfaceObservationLikelihood {
public:
	//takes the training data in the form [image[histogram of image]] and generates the naive Bayes model or the Chow Liu tree
	InterfaceObservationLikelihood(std::vector<std::vector<int> >& training_data, std::string pCLTreeFilename, bool calculateModel=false) {};
	
	// loads the stored naive Bayes model or Chow Liu Tree from file
	InterfaceObservationLikelihood(std::string pCLTreeFilename) {};
	
	virtual ~InterfaceObservationLikelihood() {};
	
	// returns P(Z_k | L_i) as defined in equations (5)-(8) for the naive Bayes model or (9)-(14) for the Chow Liu trees
    /*	virtual double evaluate(std::vector<int>& observations, int location, InterfaceDetectorModel* detectorModel, InterfacePlaceModel* placeModel) = 0; */

	// returns the marginal probability for observing a single attribute p(z_attr = val)
	virtual double getMarginalPriorProbability(int attr, int val) = 0;

	// returns the whole marginal probabilities vector
	virtual std::vector<std::vector<double> >& getMarginalPriorProbabilities() = 0;

	// returns p(Z_k | L_u) for a randomly sampled place L_u with randomly sampled obervations Z_k as needed in equation (17)
	virtual double sampleNewPlaceObservation(InterfaceDetectorModel* detectorModel, std::vector<int>& observation) = 0;

	// returns p(Z_k | L_{avg}) for the average place L_{avg} as needed in equation (16)
	virtual double meanFieldNewPlaceObservation(InterfaceDetectorModel* detectorModel, const std::vector<int>& observation) = 0;
};

#endif /* INTERFACEOBSERVATIONLIKELIHOOD_H_ */
