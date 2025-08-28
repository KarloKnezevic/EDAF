package hr.fer.zemris.edaf.algorithm.eda.dependent;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * BMDA is an extension of the Univariate Marginal Distribution Algorithm
 * (UMDA). It uses the pair gene dependencies in order to improve algorithms
 * that use simple univariate marginal distributions. BMDA is a special case of
 * the Factorization Distribution Algorithm, but without any problem specic
 * knowledge in the initial stage. The dependencies are being discovered during
 * the optimization process itself.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class BMDA extends Algorithm {

	private double indEstimationRatio;
	private final double reducingFactor;

	public BMDA(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism, double estimationProbab) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		name = "bmda";

		indEstimationRatio = estimationProbab;
		reducingFactor = computeReducingFactor(estimationProbab,
				0.1 * estimationProbab);
	}

	@Override
	public void run() {
		population = genotype.getIndividual().createPopulation(true);

		evaluation.evaluate(population);

		int generation = 0;
		while ((generation < maxGenerations) && !stagnate()) {
			++generation;

			runStep(population);

			indEstimationRatio *= reducingFactor;

			getBest(population);

			pushData(population, generation);

		}

	}

	@Override
	public Individual[] runStep(Individual[] population) {
		Arrays.sort(population);

		final Individual[] sampled = statistics
				.createPopulation(statistics
						.bivariateMarginalDistribution(selection
								.selectBetterIndividuals(population,
										indEstimationRatio)));

		evaluation.evaluate(sampled);

		insertIntoPopulation(population, sampled, indEstimationRatio);

		return population;
	}

}
