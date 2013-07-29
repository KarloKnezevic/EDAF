package hr.fer.zemris.edaf.selection.rank.tournament;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;

import java.util.Arrays;
import java.util.Random;

/**
 * Tournament selection is a method of selecting an individual from a population
 * of individuals in a genetic algorithm. Tournament selection involves running
 * several "tournaments" among a few individuals chosen at random from the
 * population. The winner of each tournament (the one with the best fitness) is
 * selected for crossover. Selection pressure is easily adjusted by changing the
 * tournament size. If the tournament size is larger, weak individuals have a
 * smaller chance to be selected.
 * 
 * Deterministic tournament selection selects the best individual (when p=1) in
 * any tournament. A 1-way tournament (k=1) selection is equivalent to random
 * selection. The chosen individual can be removed from the population that the
 * selection is made from if desired, otherwise individuals can be selected more
 * than once for the next generation.
 * 
 * Tournament selection has several benefits: it is efficient to code, works on
 * parallel architectures and allows the selection pressure to be easily
 * adjusted.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class KTournamentSelection extends Selection {

	private final int K;

	public KTournamentSelection(Random rand, double ratio, int K) {

		this.rand = rand;
		this.ratio = ratio;
		this.K = K;
	}

	@Override
	public Individual selectBetterIndividual(Individual[] individuals) {

		final Individual[] tournamentIndividuals = new Individual[K];

		for (int i = 0; i < K; i++) {
			final int index = rand.nextInt(individuals.length);
			tournamentIndividuals[i] = individuals[index];
		}

		Arrays.sort(tournamentIndividuals);

		return tournamentIndividuals[0];
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

		final Individual[] tournamentIndividuals = new Individual[K];

		for (int i = 0; i < K; i++) {
			final int index = rand.nextInt(individuals.length);
			tournamentIndividuals[i] = individuals[index];
		}

		Arrays.sort(tournamentIndividuals);

		return tournamentIndividuals[tournamentIndividuals.length - 1];
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