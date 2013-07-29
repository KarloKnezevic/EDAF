package hr.fer.zemris.edaf.selection;

import hr.fer.zemris.edaf.genotype.Individual;

import java.util.Random;

/**
 * Selection.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Selection {

	protected Random rand;

	protected double ratio;

	public abstract Individual selectBetterIndividual(Individual[] individuals);

	public abstract Individual[] selectBetterIndividuals(
			Individual[] individuals);

	public abstract Individual selectWorseIndividual(Individual[] individuals);

	public abstract Individual[] selectWorseIndividuals(Individual[] individuals);

	public Individual[] selectBetterIndividuals(Individual[] individuals,
			double ratio) {

		final double tmp = this.ratio;

		this.ratio = ratio;

		final Individual[] retIndividuals = selectBetterIndividuals(individuals);

		this.ratio = tmp;

		return retIndividuals;

	}

	public Individual[] selectWorseIndividuals(Individual[] individuals,
			double ratio) {

		final double tmp = this.ratio;

		this.ratio = ratio;

		final Individual[] retIndividuals = selectWorseIndividuals(individuals);

		this.ratio = tmp;

		return retIndividuals;

	}

}