package hr.fer.zemris.edaf.algorithm.eda.independent;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * In the compact Genetic Algorithm (cGA) proposed by Harik et. al. the
 * population is replaced by a single probability vector like in PBIL. However,
 * unlike the PBIL, it modifies the probability vector so that there is direct
 * correspondence between the population that is represented by the probability
 * vector and the probability vector itself.
 * 
 * Two (or more) individuals are generated from this vector of probabilities,
 * the competition between them is carried out and the winner is used for vector
 * update. But instead of shifting the vector proportionally to the distance
 * from either 0 or 1, each component of the vector is updated by shifting its
 * value by the contribution of a single individual to the total frequency
 * assuming a particular population size.
 * 
 * By using this update rule, theory of simple genetic algorithms can be
 * directly used in order to estimate the parameters and behavior of the cGA.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class CGA extends Algorithm {

	private final double epsilon = 0.0005;

	private final double learningRatio = 1.0 / 500;

	public CGA(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		name = "cga";

	}

	@Override
	public void run() {

		int iteration = 0;

		while ((iteration < maxGenerations) && !muProbConvergence()
				&& !stagnate()) {
			++iteration;

			population = statistics.createPopulation();

			runStep(population);

			final Individual tmp = population[0];
			population[0] = getBest();
			pushData(population, iteration);
			population[0] = tmp;
		}
	}

	@Override
	public Individual[] runStep(Individual[] population) {

		evaluation.evaluate(population);

		Arrays.sort(population);

		for (int i = 1; i < population.length; i++) {
			CGAcore(population[0], population[i]);
		}

		final Individual sample = statistics.createIndividual();
		evaluation.evaluate(sample);

		setBest(sample);

		population[population.length - 1] = sample;
		return population;
	}

	private boolean muProbConvergence() {
		final int length = statistics.getMu().length;
		final double[] array = statistics.getMu();

		for (int i = 0; i < length; i++) {
			if ((array[i] > epsilon) && (array[i] < (1 - epsilon))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Supported only discreete (binary) representation. Other representation
	 * return null.
	 * 
	 * @param winner
	 *            Best individual in population.
	 * @param loser
	 *            All other individual in population except the winner.
	 */
	private void CGAcore(Individual winner, Individual loser) {
		for (int i = 0; i < winner.getGenotypeLength(); i++) {
			final int compare = winner.compareGenotype(loser, i);
			if (compare != 0) {
				statistics.getMu()[i] = statistics.getMu()[i]
						+ (compare * learningRatio);
			}
		}
	}

}