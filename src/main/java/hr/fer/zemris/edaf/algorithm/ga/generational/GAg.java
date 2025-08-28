package hr.fer.zemris.edaf.algorithm.ga.generational;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * A genetic algorithm is an iterative procedure maintaining a population of
 * structures that are candidate solutions to specific domain challenges. During
 * each temporal increment (called a generation), the structures in the current
 * population are rated for their effectiveness as domain solutions, and on the
 * basis of these evaluations, a new population of candidate solutions is formed
 * using specific genetic operators such as reproduction, crossover, and
 * mutation.
 * 
 * They combine survival of the fittest among string structures with a
 * structured yet randomized information exchange to form a search algorithm
 * with some of the innovative flair of human search. In every generation, a new
 * set of artificial creatures (strings) is created using bits and pieces of the
 * fittest of the old; an occasional new part is tried for good measure. While
 * randomized, genetic algorithms are no simple random walk. They efficiently
 * exploit historical information to speculate on new search points with
 * expected improved performance.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GAg extends Algorithm {

	private Individual[] newGeneration;

	public GAg(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		name = "gag";

		newGeneration = genotype.getIndividual().createPopulation(false);

	}

	@Override
	public void run() {

		population = genotype.getIndividual().createPopulation(true);

		evaluation.evaluate(population);

		int generation = 0;
		while ((generation < maxGenerations) && !stagnate()) {
			++generation;

			runStep(population);

			getBest(newGeneration);

			pushData(population, generation);

		}

	}

	@Override
	public Individual[] runStep(Individual[] population) {

		Arrays.sort(population);

		eliteSurvival(population, newGeneration);

		offspringCreate(population, newGeneration);

		final Individual[] tmp = population;
		population = newGeneration;
		newGeneration = tmp;

		evaluation.evaluate(population);

		return population;

	}

	private void offspringCreate(Individual[] population,
			Individual[] newGeneration) {

		for (int i = elitism; i < newGeneration.length; i++) {

			final Individual parent1 = selection
					.selectBetterIndividual(population);
			final Individual parent2 = selection
					.selectBetterIndividual(population);

			final Individual[] children = genotype.getCrossing().cross(parent1,
					parent2);

			genotype.getMutation().mutate(children[0]);
			genotype.getMutation().mutate(children[1]);

			newGeneration[i] = children[0];

			if ((i + 1) < newGeneration.length) {
				newGeneration[++i] = children[1];
			}

		}

	}

	private void eliteSurvival(Individual[] population,
			Individual[] newGeneration) {

		for (int elite = 0; elite < elitism; elite++) {
			newGeneration[elite] = population[elite].copy();
		}

	}

}