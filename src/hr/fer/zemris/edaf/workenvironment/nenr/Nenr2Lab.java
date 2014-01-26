package hr.fer.zemris.edaf.workenvironment.nenr;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.neural.DataSet;
import hr.fer.zemris.edaf.fitness.neural.INeuralNetwork;
import hr.fer.zemris.edaf.fitness.neural.NeuralNetworkLite;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

public class Nenr2Lab extends Evaluation {
	
	private INeuralNetwork neuralNetwork;
	
	private DataSet dataSet;

	public Nenr2Lab(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {
		
		neuralNetwork = new NeuralNetworkLite(2,8,3);
		
		dataSet = new DataSet(2, 3);
		dataSet.loadFromFile("res/nenr2lab/projekt2-data.txt");
		
		algorithm.run();
	}

	@Override
	public void evaluate(Individual individual) {
		neuralNetwork.setParameters(individual.getVariable());
		individual.setFitness(neuralNetwork.calcError(dataSet));
	}

}
