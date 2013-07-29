package hr.fer.zemris.edaf.selection.proportional;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;

import java.util.Random;

/**
 * Stochastic universal sampling (SUS) is a technique used in genetic algorithms
 * for selecting potentially useful solutions for recombination. It was
 * introduced by James Baker.
 * 
 * SUS is a development of fitness proportionate selection (FPS) which exhibits
 * no bias and minimal spread. Where FPS chooses several solutions from the
 * population by repeated random sampling, SUS uses a single random value to
 * sample all of the solutions by choosing them at evenly spaced intervals. This
 * gives weaker members of the population (according to their fitness) a chance
 * to be chosen and thus reduces the unfair nature of fitness-proportional
 * selection methods.
 * 
 * Other methods like roulette wheel can have bad performance when a member of
 * the population has a really large fitness in comparison with other members.
 * Using a comb-like ruler, SUS starts from a small random number, and chooses
 * the next candidates from the rest of population remaining, not allowing the
 * fittest members to saturate the candidate space.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class StohasticUniversalSampling extends Selection {

	private final int samples;

	public StohasticUniversalSampling(Random rand, double ratio, int samples) {

		this.rand = rand;
		this.ratio = ratio;
		this.samples = samples;

	}

	@Override
	public Individual selectBetterIndividual(Individual[] individuals) {

		return selectBetterIndividuals(individuals)[rand.nextInt(samples)];
	}

	@Override
	public Individual[] selectBetterIndividuals(Individual[] individuals) {

		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {
			// min fitness are better
			sum += 1.0 / individuals[i].getFitness();
		}

		final double distance = sum / samples;

		return rewardBetterFitnessSelection(distance, individuals);

	}

	private Individual[] rewardBetterFitnessSelection(double distance,
			Individual[] individuals) {

		double start = distance * rand.nextDouble();

		final Individual[] selected = new Individual[samples];

		int index = 0;

		double sumFitness = 1.0 / individuals[index].getFitness();

		for (int i = 0; i < samples; i++) {

			while (Math.abs(sumFitness) < Math.abs(start)) {
				index++;
				sumFitness += 1.0 / individuals[index].getFitness();
			}

			start += distance;
			selected[i] = individuals[index];

		}

		return selected;

	}

	@Override
	public Individual selectWorseIndividual(Individual[] individuals) {

		return selectWorseIndividuals(individuals)[rand.nextInt(samples)];

	}

	@Override
	public Individual[] selectWorseIndividuals(Individual[] individuals) {

		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {

			sum += individuals[i].getFitness();
		}

		final double distance = sum / samples;

		return rewardWorseFitnessSelection(distance, individuals);
	}

	private Individual[] rewardWorseFitnessSelection(double distance,
			Individual[] individuals) {

		double start = distance * rand.nextDouble();

		final Individual[] selected = new Individual[samples];

		int index = 0;

		double sumFitness = individuals[index].getFitness();

		for (int i = 0; i < samples; i++) {

			while (Math.abs(sumFitness) < Math.abs(start)) {

				index++;
				sumFitness += individuals[index].getFitness();
			}

			start += distance;

			selected[i] = individuals[index];

		}

		return selected;
	}

}