package hr.fer.zemris.edaf.genotype;

import java.util.Random;

/**
 * Crossing.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Crossing {

	protected String name;

	protected double crossingProbability;

	protected Random rand;

	public Crossing(String name, double crossingProbability, Random rand) {

		this.name = name;
		this.crossingProbability = crossingProbability;
		this.rand = rand;

	}

	public abstract Individual[] cross(Individual parent1, Individual parent2);

	public String getName() {
		return name;
	}

	public double getMutationProbability() {
		return crossingProbability;
	}

	public Random getRand() {
		return rand;
	}

	@Override
	public String toString() {
		return name + " p = " + crossingProbability;
	}

}
