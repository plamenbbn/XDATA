
#include "NaiveBayes.h"
#include "ChowLiuTree.h"

#include <iostream>
#include <strstream>
#include <stdlib.h>

int main(int argc, char *argv[]) 
{

  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] << " training-data chow-liu-output mutual-information-output naive-bayes-output\n";
    exit(1);
  }

  std::string trainingDataFile = argv[1];
  std::string modelFileChowLiu = argv[2];
  //  std::string modelFileChowLiu = "chowLiu.out";
  //  std::string modelFileNaiveBayes = "naiveBayes.out";
  std::string modelFileNaiveBayes = argv[4];

  // load training data
  std::vector<std::vector<int> > trainingData;

  std::ifstream in(trainingDataFile.c_str());
	
  std::cerr << "Read from input file" << trainingDataFile.c_str() << std::endl;

	if(!in.is_open())
	{
		std::cout << "Error: could not open " << trainingDataFile.c_str() << "\n";
		return(-1);
	}
	int samples, attributes;
	in >> samples;
	in >> attributes;

    if (samples < 2) {
      std::cerr << "Error: only " << samples << " samples declared in " << trainingDataFile << "; check format."<< std::endl;
      return(-1);
    }
    if (attributes < 2) {
      std::cerr << "Error: only " << attributes << " attributes declared in " << trainingDataFile << "; check format."<< std::endl;
      return(-1);
    }

	trainingData.resize(samples, std::vector<int>(attributes, 0));
	for (unsigned int i = 0; i < trainingData.size(); ++i)
	{
		for (unsigned int j = 0; j < (trainingData)[i].size(); ++j)
		{
			in >> (trainingData)[i][j];
		}
	}
	in.close();

    ChowLiuTree * likelihoodChowLiu    = new ChowLiuTree(trainingData, modelFileChowLiu);

    likelihoodChowLiu->setMututalInformationFile(argv[3]);
    likelihoodChowLiu->Train(trainingData, modelFileChowLiu);
    std::cerr << "Chow-Liu tree output to file " << modelFileChowLiu << std::endl;

    ChowLiuTree * copyChowLiu = new ChowLiuTree(modelFileChowLiu);

    InterfaceObservationLikelihood* likelihoodNaiveBayes = new NaiveBayes(trainingData, modelFileNaiveBayes, true);

    return(0);
}

