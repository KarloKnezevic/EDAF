package hr.fer.zemris.edaf.algorithm.hybrid;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Random;

/**
 * The original objective is to get benefits from both approaches. The main
 * difference from these two evolutionary strategies is how new individuals are
 * generated. These new individuals generated on each generation are called
 * offspring. On one hand, GAs uses crossover and mutation operators as a
 * mechanism to create new individuals from the best individuals of the previous
 * generation. On the other, EDAs builds a probabilistic model with the bests
 * individuals and then sample the model to generate new ones.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class HybridEda extends Algorithm {

	private final Algorithm algorithm1;

	private final Algorithm algorithm2;

	PopulationRatio ratio;

	public HybridEda(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism, Algorithm algorithm1, Algorithm algorithm2,
			String ratioName, double initRatio) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		this.algorithm1 = algorithm1;
		this.algorithm2 = algorithm2;

		name = algorithm1.getName() + "-" + algorithm2.getName();

		ratio = new PopulationRatio(ratioName, maxGenerations, initRatio);
	}

	@Override
	public void run() {

		population = genotype.getIndividual().createPopulation(true);

		evaluation.evaluate(population);

		int generation = 0;
		while ((generation < maxGenerations) && !stagnate()) {
			++generation;

			population = runStep(population);

			getBest(population);

			pushData(population, generation, ratio.getRatio("0.000"));

		}

	}

	@Override
	public Individual[] runStep(Individual[] population) {

		final Individual[] algorithm1Population = algorithm1.runStep(population
				.clone());

		final Individual[] algorithm2Population = algorithm2.runStep(population
				.clone());

		ratio.computeRatio(algorithm1Population, algorithm2Population);

		population = ratio.getUnitedPopulation();

		evaluation.evaluate(population);

		return population;

	}

}