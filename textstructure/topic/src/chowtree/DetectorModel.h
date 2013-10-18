
#ifndef DETECTORMODEL_H_
#define DETECTORMODEL_H_

#include <vector>
#include "InterfaceDetectorModel.h"

class DetectorModel : public InterfaceDetectorModel
{
public:
	// the false positive probability p(z_i=1|e_i=0) and the false negative probability p(z_i=0|e_i=1) must be provided
	DetectorModel(double falsePositiveProbability, double falseNegativeProbability);
	~DetectorModel();

	// returns the probability for p(z_i=z|e_i=e)
	double getDetectorProbability(int z, int e);

private:
	// stores the probabilities p(z_i=a|e_i=b) as mDetectorProbabilities[a][b]
	std::vector<std::vector<double> > mDetectorProbabilities;
};

#endif /* DETECTORMODEL_H_ */
