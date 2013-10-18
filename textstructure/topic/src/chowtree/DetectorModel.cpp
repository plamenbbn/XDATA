#include "DetectorModel.h"

DetectorModel::DetectorModel(double falsePositiveProbability, double falseNegativeProbability)
: InterfaceDetectorModel(falsePositiveProbability, falseNegativeProbability)
{
	std::vector<double> temp;
	temp.resize(2, 0.0);
	mDetectorProbabilities.resize(2, temp);

	mDetectorProbabilities[0][1] = falseNegativeProbability;
	mDetectorProbabilities[1][1] = 1-falseNegativeProbability;
	mDetectorProbabilities[1][0] = falsePositiveProbability;
	mDetectorProbabilities[0][0] = 1-falsePositiveProbability;
}

DetectorModel::~DetectorModel()
{

}

double DetectorModel::getDetectorProbability(int z, int e)
{
	return mDetectorProbabilities[z][e];
}
