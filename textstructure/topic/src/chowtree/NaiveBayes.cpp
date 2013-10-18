// author: David

#include "NaiveBayes.h"
#include <stdlib.h>

NaiveBayes::NaiveBayes(std::vector<std::vector<int> >& training_data, std::string pModelFile, bool calculateModel)
: InterfaceObservationLikelihood(training_data, pModelFile, false)
{
	mTrainingData = training_data;

	if (calculateModel==false)
	{
		loadModel(pModelFile);
	}
	else
	{
		//set number of discrete attribute values per attribute (which are 0,1,2,...,n)
		for (unsigned int i=0; i<mTrainingData.begin()->size(); i++)
		{
			mAttributeSizes.push_back(2);
		}

		generateMarginalProbabilities();

		saveModel(pModelFile);

	}
	srand((unsigned)time(0));
}

NaiveBayes::NaiveBayes(std::string pModelFile)
: InterfaceObservationLikelihood(pModelFile)
{
	loadModel(pModelFile);

	srand((unsigned)time(0));
}

//NaiveBayes::NaiveBayes()
//: InterfaceObservationLikelihood("")
//{
//	//set number of discrete attribute values per attribute (which are 0,1,2,...,n)
//	mAttributeSize = 2;
//}

NaiveBayes::~NaiveBayes()
{
}


void NaiveBayes::generateMarginalProbabilities()
{
	// initialize all probabilities with count 0.1 in order to avoid zero probabilities
	// mMarginalPriorProbability[attr][a] stands for p(z_attr = a)
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		std::vector<double> temp;
		temp.resize(mAttributeSizes[attr], 0.001);
		mMarginalPriorProbability.push_back(temp);
	}

	// count occurences in training data
	for (unsigned int sample=0; sample<mTrainingData.size(); sample++)
	{
		for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
		{
			mMarginalPriorProbability[attr][mTrainingData[sample][attr]] += 1.0;
		}
	}

	// normalize probabilities
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		double sum = 0.0;
		for (int a=0; a<mAttributeSizes[attr]; a++)	sum += mMarginalPriorProbability[attr][a];
		for (int a=0; a<mAttributeSizes[attr]; a++)	mMarginalPriorProbability[attr][a] /= sum;
	}
}


void NaiveBayes::saveModel(std::string pModelFile)
{
	std::ofstream out(pModelFile.c_str());
	if(!out.is_open())
	{
		std::cout << "Error: could not open " << pModelFile.c_str() << "\n";
		return;
	}

	// save attribute sizes
	out << mAttributeSizes.size() << "\n";
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		out << mAttributeSizes[attr] << "\t";
	}
	out << std::endl;

	// save marginal probability model
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
			out << mMarginalPriorProbability[attr][a] << "\t";
		}
		out << "\n";
	}

	out.close();
}


void NaiveBayes::loadModel(std::string filename)
{
	std::ifstream in(filename.c_str());
	if(!in.is_open())
	{
		std::cout << "Error: could not open " << filename.c_str() << "\n";
		return;
	}

	mAttributeSizes.clear();
	mMarginalPriorProbability.clear();

	// load attribute sizes
	int attributeSizesSize = 0;
	in >> attributeSizesSize;
	int attributeSize = 0;
	for (int attr=0; attr<attributeSizesSize; attr++)
	{
		in >> attributeSize;
		mAttributeSizes.push_back(attributeSize);
	}

	// load marginal probability model
	double prob = 0.0;
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		std::vector<double> temp;
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
			in >> prob;
			temp.push_back(prob);
		}
		mMarginalPriorProbability.push_back(temp);
	}

	in.close();
}

/*
double NaiveBayes::evaluate(std::vector<int>& observations, int location, InterfaceDetectorModel* detectorModel, InterfacePlaceModel* placeModel)
{
	// with log likelihoods
	//double p_Zk_Li = 1.0;	// = p(Z_k | L_i), i.e. the return value of this function
	double p_Zk_Li_log = 0.0;
	
	double p_zj_Li = 0.0;	// = p(z_j | L_i), i.e. the probability of the observation attribute z_j given location L_i
	for (unsigned int j = 0;j < observations.size(); ++j)
	{
		p_zj_Li = 0.0;
		for (int s=0; s<mAttributeSizes[j]; s++)
		{
			p_zj_Li += detectorModel->getDetectorProbability(observations[j], s) * placeModel->getWordProbability(j, s, location);	// equation (8)
		}
		//p_Zk_Li *= p_zj_Li;
		p_Zk_Li_log += log(p_zj_Li);
	}

	//return p_Zk_Li;
	return p_Zk_Li_log;

	//double p_Zk_Li = 1.0;	// = p(Z_k | L_i), i.e. the return value of this function
	//
	//double p_zj_Li = 0.0;	// = p(z_j | L_i), i.e. the probability of the observation attribute z_j given location L_i
	//for (unsigned int j = 0;j < observations.size(); ++j)
	//{
	//	p_zj_Li = 0.0;
	//	for (int s=0; s<mAttributeSizes[j]; s++)
	//	{
	//		p_zj_Li += detectorModel->getDetectorProbability(observations[j], s) * placeModel->getWordProbability(j, s, location);	// equation (8)
	//	}
	//	p_Zk_Li *= p_zj_Li;
	//}

	//return p_Zk_Li;
}
*/

double NaiveBayes::sampleNewPlaceObservation(InterfaceDetectorModel* detectorModel, std::vector<int>& observation)
{
	// sample from Naive Bayes distribution
	std::vector<int> sample;
	observation.resize((int)mAttributeSizes.size(), -1);
	
	// sample all attributes
	/*for (int attr=0; attr<(int)mAttributeSizes.size(); attr++)
	{
		int value = -1;
		do
		{
			value = int((double)mAttributeSizes[attr]*rand()/(RAND_MAX + 1.0));
			if (((double)rand()/(RAND_MAX + 1.0)) > mMarginalPriorProbability[attr][value]) value = -1;
		} while (value == -1);
		observation[attr] = value;
	}*/
	sample = mTrainingData[int((double)mTrainingData.size()*rand()/(RAND_MAX + 1.0))];


	// calculate observation likelihood
	// p_Zk_Lu = p(Z_k | L_u) is the observation likelihood of a randomly sampled place L_u
	double p_Zk_Lu = 1.0;
	
	double p_zj_Lu = 0.0;	// = p(z_j | L_u), i.e. the probability of the observation attribute z_j given the unknown location L_u
	for (unsigned int j = 0; j < observation.size(); ++j)
	{
		p_zj_Lu = 0.0;
		for (int s=0; s<mAttributeSizes[j]; s++)
		{
			//p_zj_Lu += detectorModel->getDetectorProbability(observation[j], s) * mMarginalPriorProbability[j][s];	// equation (8)
			//double p_ejs_Li = (detectorModel->getDetectorProbability(sample[j], s) * mMarginalPriorProbability[j][s]) / mMarginalPriorProbability[j][sample[j]];
			double p_ejs_Li = (sample[j]==s) ? 1 : 0;
			p_zj_Lu += detectorModel->getDetectorProbability(observation[j], s) * p_ejs_Li;	// equation (8)
		}
		p_Zk_Lu *= p_zj_Lu;
	}

	return p_Zk_Lu;
}


double NaiveBayes::meanFieldNewPlaceObservation(InterfaceDetectorModel* detectorModel, const std::vector<int>& observation)
{
	// calculate observation likelihood
	// p_Zk_Lu = p(Z_k | L_u) is the observation likelihood of a randomly sampled place L_u
	//double p_Zk_Lu = 1.0;
	double p_Zk_Lu_log = 0.0;
	
	double p_zj_Lu = 0.0;	// = p(z_j | L_u), i.e. the probability of the observation attribute z_j given the unknown location L_u
	for (unsigned int attr = 0; attr < observation.size(); ++attr)
	{
		p_zj_Lu = 0.0;
		for (int s=0; s<mAttributeSizes[attr]; s++)
		{
			p_zj_Lu += detectorModel->getDetectorProbability(observation[attr], s) * mMarginalPriorProbability[attr][s];	// equation (8)
		}
		//p_Zk_Lu *= p_zj_Lu;
		p_Zk_Lu_log += log(p_zj_Lu);
	}

	//return p_Zk_Lu;
	return p_Zk_Lu_log;
}
