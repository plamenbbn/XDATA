// author: Richard

#include "ChowLiuTree.h"
#include <stdlib.h>


ChowLiuTree::ChowLiuTree(std::vector<std::vector<int> >& training_data, std::string pCLTreeFilename): InterfaceObservationLikelihood(training_data, pCLTreeFilename)
{

}

std::string ChowLiuTree::getMututalInformationFile(void)
{
  return mMutualInformationFile;
}

void ChowLiuTree::setMututalInformationFile(std::string filename)
{
  mMutualInformationFile = filename;
}


void ChowLiuTree::Train(std::vector<std::vector<int> >& training_data, std::string pCLTreeFilename) 
{
  mTrainingData = training_data;


		//set number of discrete attribute values per attribute (which are 0,1,2,...,n)
		for (unsigned int i=0; i<mTrainingData.begin()->size(); i++)
		{
			mAttributeSizes.push_back(2);
		}

		// calculate optimal first-order dependences
		std::vector< std::vector<double> > mutualInformation = calculateMutualInformation();

		primMaximumSpanningTree(mutualInformation);

		generateChowLiuProbabilities();

		saveModel(pCLTreeFilename);

	srand((unsigned)time(0));

}

ChowLiuTree::ChowLiuTree(std::string pCLTreeFilename)
: InterfaceObservationLikelihood(pCLTreeFilename)
{
	loadModel(pCLTreeFilename);

	srand((unsigned)time(0));
}

ChowLiuTree::~ChowLiuTree()
{
}

std::vector< std::vector<double> > ChowLiuTree::calculateMutualInformation()
{
	std::cout << "Start ChowLiuTree::calculateMutualInformation()" << std::endl;
	std::vector< std::vector<double> > p;		// marginal probabilities [attribute number][value number]
	p.resize(mAttributeSizes.size());
	for (unsigned int i=0; i<mAttributeSizes.size(); i++)
		for (int j=0; j<mAttributeSizes[i]; j++)
			p[i].push_back(0.0);				//Todo: Change to >0 if no 0-probabilities wanted

	//marginals
	for (unsigned int sample=0; sample<mTrainingData.size(); sample++)
	{
      //      std::cout << "Sample " << sample << ":" << std::endl;
		for (unsigned int i=0; i<mAttributeSizes.size(); i++)
		{
			p[i][mTrainingData[sample][i]] += 1.0;
            //            std::cout << "  p[" << i  << "][" << mTrainingData[sample][i] << "] = " << p[i][mTrainingData[sample][i]] << std::endl;

		}
	}
	int ts = mTrainingData.size();
    //    std::cout << "norm by " << ts <<std::endl;
	for (unsigned int i=0; i<mAttributeSizes.size(); i++)
	{ //normalize
      for (int j=0; j<mAttributeSizes[i]; j++) {
		p[i][j] /= ts;
        //        std::cout << "  p[" << i  << "][" << j << "] = " << p[i][j] << std::endl;
      }
	}

	// calculate entropies
	std::vector<double> entropies;
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		entropies.push_back(0.0);
		for (int val=0; val<mAttributeSizes[attr]; val++)
		{
			if (p[attr][val] != 0.0) entropies[attr] -= p[attr][val] * log(p[attr][val]);
		}
	}

	// mutual information: mutualInf[attribute 1][attribute 2]
	std::vector< std::vector<double> > mutualInf;
	mutualInf.resize(mAttributeSizes.size());
	for (unsigned int attr1=0; attr1<mutualInf.size(); attr1++)
	{
		mutualInf[attr1].resize(attr1+1);
		for (unsigned int attr2=0; attr2<attr1; attr2++)
		{
			// joint probabilities: joint[i][j] is the probability for x[attr1] = i and x[attr2] = j
			std::vector< std::vector<double> > joints;
			joints.resize(mAttributeSizes[attr1]);
			for (int i=0; i<mAttributeSizes[attr1]; i++)
				for (int j=0; j<mAttributeSizes[attr2]; j++)
					joints[i].push_back(0.0);

			// count all occurences of joints and calculate joints
			for (unsigned int sample=0; sample<mTrainingData.size(); sample++)
			{
				joints[mTrainingData[sample][attr1]][mTrainingData[sample][attr2]]++;
			}
			for (int i=0; i<mAttributeSizes[attr1]; i++)
				for (int j=0; j<mAttributeSizes[attr2]; j++)
					joints[i][j] /= ts;

			// calculate mutual information
			mutualInf[attr1][attr2] = entropies[attr1] + entropies[attr2];
			for (int i=0; i<mAttributeSizes[attr1]; i++)
				for (int j=0; j<mAttributeSizes[attr2]; j++)
					if (joints[i][j] != 0) mutualInf[attr1][attr2] += joints[i][j] * log(joints[i][j]);
		}
	}

    // dump to file, if provided
    
    
    std::ofstream mifile;
    if (! mMutualInformationFile.empty()) {
      mifile.open(mMutualInformationFile.c_str(), std::ofstream::out);
    }

	for (unsigned int attr1=0; attr1<mutualInf.size(); attr1++)
	{
		for (unsigned int attr2=0; attr2<attr1; attr2++)
         {
           std::cout << "MI[" << attr1 << "][" << attr2 << "] =" << mutualInf[attr1][attr2] << std::endl;
           if (! mMutualInformationFile.empty()) {
             mifile << "MI[" << attr1 << "][" << attr2 << "] =" << mutualInf[attr1][attr2] << std::endl;
           }
        }
    }

    if (! mMutualInformationFile.empty()) {
      mifile.close();
    }

	return mutualInf;
}

void ChowLiuTree::primMaximumSpanningTree(std::vector< std::vector<double> > edgeWeights)
{
	std::cout << "Start ChowLiuTree::primMaximumSpanningTree()" << std::endl;

	// the node queue hold the nodes ordered with their distance to the spanning tree <value, node number>
	std::multimap<double, int> NodeQueue;
	std::multimap<double, int>::iterator ItNodeQueue, ItNodeQueue2;

	// this vector saves which nodes are still contained in the NodeQueue (0=not in queue, 1=in queue)
	//std::vector<int> NodeInQueue;
	
	// this vector saves the current distance to the spanning tree for each node
	std::vector<double> NodeValue;
	
	// the vector mParentIndex stores the parent attribute to each node (index=attribute, value=parent attribute)

	// initialization
	mParentIndex.clear();
	for (unsigned int i=0; i<mAttributeSizes.size(); i++)
	{
		//NodeInQueue.push_back(1);
		mParentIndex.push_back(0);
	}

	NodeQueue.insert(std::pair<double, int>(-DBL_MAX, 0));
	NodeValue.push_back(-DBL_MAX);
	for (unsigned int i=1; i<mAttributeSizes.size(); i++)
	{
		NodeQueue.insert(std::pair<double, int>(0.0, i));
		NodeValue.push_back(0.0);
	}

	// build MST
	int currentNode = 0;
	while (NodeQueue.size() > 0)
	{

      std::cout << "\nNodeQueue now has " << NodeQueue.size() << " entries." << std::endl;
      
      int qi = 0;
      for (std::multimap<double, int>::iterator qiter = NodeQueue.begin();
           qiter != NodeQueue.end();
           qiter++) {

        std::cout << "NodeQueue[" << qi << "]=(" << qiter->first << "," << qiter->second << "), NodeValue[" << qi << "]=" << NodeValue[qi] <<  std::endl;
        
        qi++;
      }

      // draw node with minimal value from queue
      ItNodeQueue = NodeQueue.begin();
      currentNode = ItNodeQueue->second;

      std::cout << "Erasing node (" << ItNodeQueue->first << "," << ItNodeQueue->second << ")" << std::endl;

      NodeQueue.erase(ItNodeQueue);
      //NodeInQueue[currentNode] = 0;
      
      qi = 0;
      for (std::multimap<double, int>::iterator qiter = NodeQueue.begin();
           qiter != NodeQueue.end();
           qiter++) {

        std::cout << "NodeQueue[" << qi << "]=(" << qiter->first << "," << qiter->second << "), NodeValue[" << qi << "]=" << NodeValue[qi] <<  std::endl;
        
        qi++;
      }

      
		
		// update remaining nodes in queue
		for (ItNodeQueue = NodeQueue.begin(); ItNodeQueue != NodeQueue.end();)
		{
			int node = ItNodeQueue->second;
			int node1 = std::max(currentNode, node);	//node1 has to be bigger than node2 because of memory scheme of edgeWeights
			int node2 = std::min(currentNode, node);

            std::cout << "Comparing currentNode " << currentNode << " and " << node << ", -mi[" << node1 << "][" << node2 << "]=" << -edgeWeights[node1][node2] << ", NodeValue[" << node << "]=" << NodeValue[node] << std::endl;

			if (-edgeWeights[node1][node2] <= NodeValue[node])		// -edgeWeights since mutualInformation is positive!
			{
				mParentIndex[node] = currentNode;
                std::cout << " Set parent index for " << node << " to " << currentNode << std::endl;
				NodeValue[node] = -edgeWeights[node1][node2];
                std::cout << " Set NodeValue[" << node << "] to " << NodeValue[node] << std::endl;

				ItNodeQueue2 = ItNodeQueue;
				ItNodeQueue++;
                std::cout << " Erasing (" << ItNodeQueue2->first << "," << ItNodeQueue2->second << ")" << std::endl;
				NodeQueue.erase(ItNodeQueue2);
				NodeQueue.insert(ItNodeQueue, std::pair<double,int>(-edgeWeights[node1][node2], node));
                std::cout << " Inserting (" << -edgeWeights[node1][node2] << "," << node << ")" << std::endl;


			}
			else
			{
				ItNodeQueue++;
			}
		}
	}
}
//G: Graph
//VG: Knotenmenge von G
//w: Gewichtsfunktion für Kantenlänge
//r: Startknoten (r ? VG)
//Q: Prioritätswarteschlange
//?[u]: Elternknoten von Knoten u im Spannbaum
//Adj[u]: Adjazenzliste von u (alle Nachbarknoten)
//wert[u]: Abstand von u zum entstehenden Spannbaum
//
//algorithmus_von_prim(G,w,r)
//01  Q  VG   //Initialisierung
//02  für alle u ? Q
//03      wert[u]  ?
//04      ?[u]  0
//05  wert[r]  0
//06  solange Q ? 
//07      u  extract_min(Q)
//08      für alle v ? Adj[u]
//09          wenn v ? Q und w(u,v) < wert[v]
//10              dann ?[v]  u
//11                  wert[v]  w(u,v)


void ChowLiuTree::generateChowLiuProbabilities()
{
	std::cout << "Start ChowLiuTree::generateChowLiuProbabilities()" << std::endl;

    std::cout << "Attribute sizes: " << mAttributeSizes.size() << std::endl;

	// initialize all probabilities with count 0.1 in order to avoid zero probabilities
	// mProbabilityModel[attr][a][b] stands for p( attr=a | parent(attr)=b )
	// mMarginalPriorProbability[attr][a] stands for p(z_attr = a)
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
      std::cout << "attr " << attr << std::endl;

		// first-order dependent probabilities
		std::vector<std::vector<double> > temp2;
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
			std::vector<double> temp;
			temp.resize(mAttributeSizes[mParentIndex[attr]], 0.001f);
			//for (int b=0; b<mAttributeSizes[mParentIndex[attr]]; b++)
			//{
			//	temp.push_back(0.1);
			//}
			temp2.push_back(temp);
		}
		mProbabilityModel.push_back(temp2);

		// marginals
		std::vector<double> temp3;
		temp3.resize(mAttributeSizes[attr], 0.1);
		mMarginalPriorProbability.push_back(temp3);
	}
	mProbabilityModel[0][0][1] = 0.0;
	mProbabilityModel[0][1][0] = 0.0;

	// count occurences in training data
	for (unsigned int sample=0; sample<mTrainingData.size(); sample++)
	{
		for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
		{
			mProbabilityModel[attr][mTrainingData[sample][attr]][mTrainingData[sample][mParentIndex[attr]]] += 1.0;
			mMarginalPriorProbability[attr][mTrainingData[sample][attr]] += 1.0;
		}
	}

	// normalize probabilities
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		// first-order dependent probabilities
		for (int b=0; b<mAttributeSizes[mParentIndex[attr]]; b++)
		{
			double sum = 0.0;
			for (int a=0; a<mAttributeSizes[attr]; a++) sum += mProbabilityModel[attr][a][b];
			for (int a=0; a<mAttributeSizes[attr]; a++) mProbabilityModel[attr][a][b] /= sum;
		}

		// marginals
		double sum = 0.0;
		for (int a=0; a<mAttributeSizes[attr]; a++)	sum += mMarginalPriorProbability[attr][a];
		for (int a=0; a<mAttributeSizes[attr]; a++)	mMarginalPriorProbability[attr][a] /= sum;
	}
}

void ChowLiuTree::saveModel(std::string pCLTreeFilename)
{
	std::ofstream out(pCLTreeFilename.c_str());
	if(!out.is_open())
	{
		std::cout << "Error: could not open " << pCLTreeFilename.c_str() << "\n";
		return;
	}

	// save parent index vector
	out << mParentIndex.size() << "\n";
	for (unsigned int attr=0; attr<mParentIndex.size(); attr++)
	{
		out << mParentIndex[attr] << "\t";
	}
	out << std::endl;

	// save attribute sizes
	out << mAttributeSizes.size() << "\n";
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		out << mAttributeSizes[attr] << "\t";
	}
	out << std::endl;

	// save first-order dependency probability model
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
			for (int b=0; b<mAttributeSizes[mParentIndex[attr]]; b++)
			{
				out << mProbabilityModel[attr][a][b] << "\t";
			}
			out << "\n";
		}
	}

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

void ChowLiuTree::loadModel(std::string filename)
{
	std::ifstream in(filename.c_str());
	if(!in.is_open())
	{
		std::cout << "Error: could not open " << filename.c_str() << "\n";
		return;
	}

	mParentIndex.clear();
	mAttributeSizes.clear();
	mProbabilityModel.clear();
	mMarginalPriorProbability.clear();

	// load parent index vector
	int parentIndexSize = 0;
	in >> parentIndexSize;
	int parentIndex = 0;
    std::cout << "Reading model from " << filename << std::endl;
	for (int attr=0; attr<parentIndexSize; attr++)
	{
		in >> parentIndex;
		mParentIndex.push_back(parentIndex);
        std::cout << "Parent index " << attr << " of " << parentIndexSize << ": " << parentIndex << std::endl;
	}

	// load attribute sizes
	int attributeSizesSize = 0;
	in >> attributeSizesSize;
	int attributeSize = 0;
	for (int attr=0; attr<attributeSizesSize; attr++)
	{
		in >> attributeSize;
		mAttributeSizes.push_back(attributeSize);
        std::cout << "Attribute size " << attr << " of " << attributeSizesSize << ": " << attributeSize << std::endl;
	}

	// load first-order dependency probability model
    std::cout << "Loading first order dependencies..." << std::endl;
	double prob = 0.0;
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
      std::cout << "attr " << attr << " of " << mAttributeSizes.size() << ": parentIndex = " << mParentIndex[attr] << std::endl;
		std::vector<std::vector<double> > temp2;
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
          std::cout << " a=" << a << " of " << mAttributeSizes[attr] << ": last prob for " << mParentIndex[attr] << " is " << mAttributeSizes[mParentIndex[attr]] << std::endl;
			std::vector<double> temp;
			for (int b=0; b<mAttributeSizes[mParentIndex[attr]]; b++)
			{
				in >> prob;
                std::cout << "  prob[" << b << "] == " << prob << std::endl;
				temp.push_back(prob);
			}
			temp2.push_back(temp);
		}
		mProbabilityModel.push_back(temp2);
	}

	// load marginal probability model
    std::cout << "Loading marginals..." << std::endl;
	for (unsigned int attr=0; attr<mAttributeSizes.size(); attr++)
	{
      std::cout << "attr " << attr << " of " << mAttributeSizes.size() << ":" << std::endl;
		std::vector<double> temp;
		for (int a=0; a<mAttributeSizes[attr]; a++)
		{
			in >> prob;
            std::cout << " marginal prior=" << prob << std::endl;
			temp.push_back(prob);
		}
		mMarginalPriorProbability.push_back(temp);
	}

	in.close();

	// build sampling order
	std::vector<int> done;
	done.resize((int)mAttributeSizes.size(), 0);
	mSamplingOrder.clear();
	mSamplingOrder.push_back(0);
	done[0] = 1;
	int attributesSampled = 1;
	while (attributesSampled < (int)mAttributeSizes.size())
	{
		for (int attr=1; attr<(int)mAttributeSizes.size(); attr++)
		{
			// sample if parent attribute is already set
			if ((done[mParentIndex[attr]] != 0) && (done[attr]==0))
			{
				mSamplingOrder.push_back(attr);
				done[attr] = 1;
				attributesSampled++;
			}
		}
	}
}

/*
double ChowLiuTree::evaluate(std::vector<int>& observations, int location, InterfaceDetectorModel* detectorModel, InterfacePlaceModel* placeModel)
{
	//double p_Zk_Li = 1.0;	// = p(Z_k | L_i), i.e. the return value of this function
	double p_Zk_Li_log = 0.0;
	
	double p_zr_Li = 0.0;	// = p(z_r | L_i), i.e. the marginal probability of the root observation attribute (i.e. z_0) given location L_i
	for (int s=0; s<mAttributeSizes[0]; s++)
	{
		p_zr_Li += detectorModel->getDetectorProbability(observations[0], s) * placeModel->getWordProbability(0, s, location);	// equation (8)
	}
	if (p_zr_Li>1.0) std::cout << "ChowLiuTree::evaluate: p_zr_Li > 1\n";
	//p_Zk_Li *= p_zr_Li;
	p_Zk_Li_log += log(p_zr_Li);

	for (unsigned int attr=1; attr<mAttributeSizes.size(); attr++)
	{
		double p_zq_zpq_Li = 0.0;	// = p(z_q | z_p_q, L_i), i.e. the value calculated in equation (11)

		for (int s=0; s<mAttributeSizes[attr]; s++)
		{
			double alpha = getMarginalPriorProbability(attr, observations[attr]) * detectorModel->getDetectorProbability((observations[attr]+1)%2, s)
				* mProbabilityModel[attr][(observations[attr]+1)%2][observations[mParentIndex[attr]]];	// equation (13)
			double beta = getMarginalPriorProbability(attr, (observations[attr]+1)%2) * detectorModel->getDetectorProbability(observations[attr], s)
				* mProbabilityModel[attr][observations[attr]][observations[mParentIndex[attr]]];		// equation (14)

			double p_zq_eq_zpq = 1/(1+alpha/beta);	// = p(z_q | e_q, z_p_q), i.e. the probability calculated in equation (12)

			p_zq_zpq_Li += p_zq_eq_zpq * placeModel->getWordProbability(attr, s, location);	// equation (11)
		}
		if (p_zq_zpq_Li>1.0) std::cout << "ChowLiuTree::evaluate: p_zq_zpq_Li > 1\n";

		//p_Zk_Li *= p_zq_zpq_Li;
		p_Zk_Li_log += log(p_zq_zpq_Li);
	}

	//return p_Zk_Li;
	return p_Zk_Li_log;
}
*/

double ChowLiuTree::getMarginalPriorProbability(int attr, int val)
{
	return mMarginalPriorProbability[attr][val];
}

double ChowLiuTree::sampleNewPlaceObservation(InterfaceDetectorModel* detectorModel, std::vector<int>& observation)
{
	// sample from Chow Liu distribution
	std::vector<int> sample;
	observation.resize((int)mAttributeSizes.size(), -1);
	
	// sample the root
	int value = -1;
	do
	{
		value = int((double)mAttributeSizes[0]*rand()/(RAND_MAX + 1.0));
		if (((double)rand()/(RAND_MAX + 1.0)) > mMarginalPriorProbability[0][value]) value = -1;
	} while (value == -1);
	observation[0] = value;
	
	// sample the other attributes
	int attributesSampled = 1;
	for (int k=1; k<(int)mAttributeSizes.size(); k++)
	{
		// sample if parent attribute is already set
		int attr = mSamplingOrder[k];
		value = -1;
		do
		{
			value = int((double)mAttributeSizes[attr]*rand()/(RAND_MAX + 1.0));
			if (((double)rand()/(RAND_MAX + 1.0)) > mProbabilityModel[attr][value][observation[mParentIndex[attr]]]) value = -1;
		} while (value == -1);
		observation[attr] = value;
	}

	// calculate observation likelihood
	// p_Zk_Lu = p(Z_k | L_u) is the observation likelihood of a randomly sampled place L_u
	double p_Zk_Lu = 1.0;

	double p_zr_Lu = 0.0;	// = p(z_r | L_u), i.e. the marginal probability of the root observation attribute (i.e. z_0) given the unknown location L_u
	for (int s=0; s<mAttributeSizes[0]; s++)
	{
		p_zr_Lu += detectorModel->getDetectorProbability(observation[0], s) * mMarginalPriorProbability[0][s];	// equation (8) with marginals for the place
	}
	p_Zk_Lu *= p_zr_Lu;

	for (unsigned int attr=1; attr<mAttributeSizes.size(); attr++)
	{
		double p_zq_zpq_Lu = 0.0;	// = p(z_q | z_p_q, L_u), i.e. the value calculated in equation (11)

		for (int s=0; s<mAttributeSizes[attr]; s++)
		{
			double alpha = mMarginalPriorProbability[attr][observation[attr]] * detectorModel->getDetectorProbability((observation[attr]+1)%2, s)
				* mProbabilityModel[attr][(observation[attr]+1)%2][observation[mParentIndex[attr]]];	// equation (13)
			double beta = mMarginalPriorProbability[attr][(observation[attr]+1)%2] * detectorModel->getDetectorProbability(observation[attr], s)
				* mProbabilityModel[attr][observation[attr]][observation[mParentIndex[attr]]];		// equation (14)

			double p_zq_eq_zpq = 1/(1+alpha/beta);	// = p(z_q | e_q, z_p_q), i.e. the probability calculated in equation (12)

			p_zq_zpq_Lu += p_zq_eq_zpq * mMarginalPriorProbability[attr][s];	// equation (11) with marginals for the place
		}

		p_Zk_Lu *= p_zq_zpq_Lu;
	}

	return p_Zk_Lu;
}


double ChowLiuTree::meanFieldNewPlaceObservation(InterfaceDetectorModel* detectorModel, const std::vector<int>& observation)
{
	// calculate observation likelihood
	// p_Zk_Lu = p(Z_k | L_u) is the observation likelihood of a randomly sampled place L_u
	//double p_Zk_Lu = 1.0;
	double p_Zk_Lu_log = 0.0;

	double p_zr_Lu = 0.0;	// = p(z_r | L_u), i.e. the marginal probability of the root observation attribute (i.e. z_0) given the unknown location L_u
	for (int s=0; s<mAttributeSizes[0]; s++)
	{
		p_zr_Lu += detectorModel->getDetectorProbability(observation[0], s) * mMarginalPriorProbability[0][s];	// equation (8) with marginals for the place
	}
	//p_Zk_Lu *= p_zr_Lu;
	p_Zk_Lu_log += log(p_zr_Lu);

	for (unsigned int attr=1; attr<mAttributeSizes.size(); attr++)
	{
		double p_zq_zpq_Lu = 0.0;	// = p(z_q | z_p_q, L_u), i.e. the value calculated in equation (11)

		for (int s=0; s<mAttributeSizes[attr]; s++)
		{
			double alpha = mMarginalPriorProbability[attr][observation[attr]] * detectorModel->getDetectorProbability((observation[attr]+1)%2, s)
				* mProbabilityModel[attr][(observation[attr]+1)%2][observation[mParentIndex[attr]]];	// equation (13)
			double beta = mMarginalPriorProbability[attr][(observation[attr]+1)%2] * detectorModel->getDetectorProbability(observation[attr], s)
				* mProbabilityModel[attr][observation[attr]][observation[mParentIndex[attr]]];		// equation (14)

			double p_zq_eq_zpq = 1/(1+alpha/beta);	// = p(z_q | e_q, z_p_q), i.e. the probability calculated in equation (12)

			p_zq_zpq_Lu += p_zq_eq_zpq * mMarginalPriorProbability[attr][s];	// equation (11) with marginals for the place
		}

		//p_Zk_Lu *= p_zq_zpq_Lu;
		p_Zk_Lu_log += log(p_zq_zpq_Lu);
	}

	//return p_Zk_Lu;
	return p_Zk_Lu_log;
}
