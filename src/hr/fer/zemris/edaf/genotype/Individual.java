package hr.fer.zemris.edaf.genotype;

import java.util.Arrays;
import java.util.Random;

/**
 * Individual.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Individual implements Comparable<Individual> {

	protected int populationLen;

	protected Random rand;

	protected double[] variable;

	protected double fitness;

	public abstract double[] getVariable();

	public void setVariable(int index, double value) {
		variable[index] = value;
	}

	public int getVariableNumber() {
		return variable.length;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getPopulationLen() {
		return populationLen;
	}

	public Random getRand() {
		return rand;
	}

	public abstract int getGenotypeLength();

	public abstract int compareGenotype(Individual ind, int index);

	public abstract Individual[] createPopulation(boolean init);

	public abstract Individual copy();

	public abstract void copy(Individual copy);

	/**
	 * Ascending comparation.
	 */
	@Override
	public int compareTo(Individual o) {

		if (fitness < o.fitness) {
			return -1;
		}

		if (fitness > o.fitness) {
			return 1;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Individual)) {
			return false;
		}
		final Individual ind = (Individual) obj;

		return ((populationLen == ind.populationLen)
				&& (Math.abs(fitness - ind.fitness) < 1E-10) && Arrays.equals(
				variable, ind.variable));
	}

}