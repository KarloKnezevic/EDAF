package hr.fer.zemris.edaf.algorithm.eda.independent;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * The Univariate Marginal Distribution Algorithm (UMDA) was introduced by
 * Műehlenbein. In contrast to PBIL, the population of solutions is kept and
 * processed.
 * 
 * In each iteration the frequency functions on each position for the selected
 * set of promising solutions are computed and these are then used to generate
 * new solutions. The new solutions replace the old ones and the process is
 * repeated until the termination criteria are met.
 * 
 * From the implementation point of view the UMDA matches the pseudo-code of
 * typical EDA algorithm, except that the dependence graph of constructed
 * probabilistic model contains no edges.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class UMDA extends Algorithm {

	private final double indEstimationRatio;

	public UMDA(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism, double estimationProbab) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		name = "umda";

		indEstimationRatio = estimationProbab;

	}

	@Override
	public void run() {

		population = genotype.getIndividual().createPopulation(true);

		evaluation.evaluate(population);

		int generation = 0;
		while ((generation < maxGenerations) && !stagnate()) {
			++generation;

			runStep(population);

			getBest(population);

			pushData(population, generation);

		}

	}

	@Override
	public Individual[] runStep(Individual[] population) {

		Arrays.sort(population);

		statistics.independentlyEstimateParams(selection
				.selectBetterIndividuals(population, indEstimationRatio));

		final Individual[] sampled = statistics.createPopulation();

		evaluation.evaluate(sampled);

		insertIntoPopulation(population, sampled, indEstimationRatio);

		return population;
	}

}