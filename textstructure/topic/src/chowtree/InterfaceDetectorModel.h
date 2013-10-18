#ifndef INTERFACEDETECTORMODEL_H_
#define INTERFACEDETECTORMODEL_H_

class InterfaceDetectorModel
{
public:
	// the false positive probability p(z_i=1|e_i=0) and the false negative probability p(z_i=0|e_i=1) must be provided
	InterfaceDetectorModel(double falsePositiveProbability, double falseNegativeProbability) {};
	virtual ~InterfaceDetectorModel() {};

	// returns the probability for p(z_i=z|e_i=e)
	virtual double getDetectorProbability(int z, int e) = 0;
};

#endif /* INTERFACEDETECTORMODEL_H_ */
