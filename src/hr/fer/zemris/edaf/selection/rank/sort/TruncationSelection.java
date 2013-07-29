package hr.fer.zemris.edaf.selection.rank.sort;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;

import java.util.Random;

/**
 * Truncation selection is a selection method used in genetic algorithms to
 * select potential candidate solutions for recombination.
 * 
 * In truncation selection the candidate solutions are ordered by fitness, and
 * some proportion, p, (e.g. p=1/2, 1/3, etc.), of the fittest individuals are
 * selected and reproduced 1/p times. Truncation selection is less sophisticated
 * than many other selection methods, and is not often used in practice.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TruncationSelection extends Selection {

	public TruncationSelection(Random rand, double ratio) {

		this.rand = rand;
		this.ratio = ratio;
	}

	@Override
	public Individual selectBetterIndividual(Individual[] individuals) {
		return individuals[0];
	}

	@Override
	public Individual[] selectBetterIndividuals(Individual[] individuals) {
		final Individual[] selected = new Individual[(int) Math.ceil(ratio
				* individuals.length)];

		for (int i = 0; i < selected.length; i++) {
			selected[i] = individuals[i];
		}

		return selected;
	}

	@Override
	public Individual selectWorseIndividual(Individual[] individuals) {
		return individuals[individuals.length - 1];
	}

	@Override
	public Individual[] selectWorseIndividuals(Individual[] individuals) {
		final Individual[] selected = new Individual[(int) Math.ceil(ratio
				* individuals.length)];

		for (int i = 0; i < selected.length; i++) {
			selected[i] = individuals[individuals.length - 1 - i];
		}

		return selected;
	}

}
