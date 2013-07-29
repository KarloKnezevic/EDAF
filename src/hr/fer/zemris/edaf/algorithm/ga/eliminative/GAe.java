package hr.fer.zemris.edaf.algorithm.ga.eliminative;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * The standard generational genetic system uses two populations at the
 * selection stage of the genetic algorithm. One population of the system
 * contains the parents to be selected and a second population is generated to
 * hold their progeny. The steady state genetic system uses the same population
 * for both parents and their progeny.
 * 
 * Steady state genetic algorithm writes the next generation of the genetic
 * algorithm to the same population from which the parents were selected. When
 * the genetic operation on the parents is completed, the new offspring take the
 * place of members of the previous generation within that population.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GAe extends Algorithm {

	private final int eliminativeIndividuals;

	public GAe(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			double mortality) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, mortality);

		name = "gae";

		eliminativeIndividuals = (int) Math.ceil(genotype.getIndividual()
				.getPopulationLen() * mortality);

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

		steady_state(population);

		evaluation.evaluate(population);

		return population;
	}

	private void steady_state(Individual[] population) {

		for (int i = 0; i < eliminativeIndividuals; i++) {

			final Individual[] selected = selection
					.selectBetterIndividuals(population);

			Arrays.sort(selected);

			final Individual[] children = genotype.getCrossing().cross(
					selected[0], selected[1]);

			genotype.getMutation().mutate(children[0]);
			genotype.getMutation().mutate(children[1]);

			final Individual betterChild = children[0].getFitness() < children[1]
					.getFitness() ? children[0] : children[1];

			final Individual worse = selection
					.selectWorseIndividual(population);

			if (worse.getFitness() > selected[selected.length - 1].getFitness()) {
				worse.copy(betterChild);
			} else {
				selected[selected.length - 1].copy(betterChild);
			}

		}

	}
}