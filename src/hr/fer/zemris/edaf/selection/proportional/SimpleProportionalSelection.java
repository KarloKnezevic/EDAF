package hr.fer.zemris.edaf.selection.proportional;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;

import java.util.Random;

/**
 * Fitness proportionate selection, also known as roulette wheel selection, is a
 * genetic operator used in genetic algorithms for selecting potentially useful
 * solutions for recombination.
 * 
 * In fitness proportionate selection, as in all selection methods, the fitness
 * function assigns a fitness to possible solutions or chromosomes. This fitness
 * level is used to associate a probability of selection with each individual
 * chromosome.
 * 
 * While candidate solutions with a higher fitness will be less likely to be
 * eliminated, there is still a chance that they may be. Contrast this with a
 * less sophisticated selection algorithm, such as truncation selection, which
 * will eliminate a fixed percentage of the weakest candidates. With fitness
 * proportionate selection there is a chance some weaker solutions may survive
 * the selection process; this is an advantage, as though a solution may be
 * weak, it may include some component which could prove useful following the
 * recombination process.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SimpleProportionalSelection extends Selection {

	public SimpleProportionalSelection(Random rand, double ratio) {
		this.rand = rand;
		this.ratio = ratio;
	}

	@Override
	public Individual selectBetterIndividual(Individual[] individuals) {

		double sum = 0;
		double max = 0;

		for (int i = 0; i < individuals.length; i++) {
			sum += individuals[i].getFitness();
			if ((i == 0) || (max < individuals[i].getFitness())) {
				max = individuals[i].getFitness();
			}
		}

		sum = (individuals.length * max) - sum;
		final double random = rand.nextDouble() * sum;
		double acc = 0;

		for (int i = 0; i < individuals.length; i++) {
			acc += max - individuals[i].getFitness();
			if (random < acc) {
				return individuals[i];
			}
		}

		return individuals[individuals.length - 1];
	}

	@Override
	public Individual[] selectBetterIndividuals(Individual[] individuals) {

		final Individual[] selected = new Individual[(int) Math.ceil(ratio
				* individuals.length)];

		for (int i = 0; i < selected.length; i++) {
			selected[i] = selectBetterIndividual(individuals);
		}

		return selected;
	}

	@Override
	public Individual selectWorseIndividual(Individual[] individuals) {

		double sum = 0;
		double max = 0;

		for (int i = 0; i < individuals.length; i++) {
			sum += individuals[i].getFitness();
			if ((i == 0) || (max < individuals[i].getFitness())) {
				max = individuals[i].getFitness();
			}
		}

		sum = (individuals.length * max) - sum;
		final double random = rand.nextDouble() * sum;
		double acc = 0;

		for (int i = 0; i < individuals.length; i++) {
			acc += individuals[i].getFitness();
			if (random < acc) {
				return individuals[i];
			}
		}

		return individuals[individuals.length - 1];

	}

	@Override
	public Individual[] selectWorseIndividuals(Individual[] individuals) {

		final Individual[] selected = new Individual[(int) Math.ceil(ratio
				* individuals.length)];

		for (int i = 0; i < selected.length; i++) {
			selected[i] = selectWorseIndividual(individuals);
		}

		return selected;
	}

}