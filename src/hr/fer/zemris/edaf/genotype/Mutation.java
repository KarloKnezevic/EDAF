package hr.fer.zemris.edaf.genotype;

import java.util.Random;

/**
 * Mutation.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Mutation {

	protected String name;

	protected double mutationProbability;

	protected Random rand;

	public Mutation(String name, double mutationProbability, Random rand) {

		this.name = name;
		this.mutationProbability = mutationProbability;
		this.rand = rand;

	}

	public abstract void mutate(Individual individual);

	public String getName() {
		return name;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public Random getRand() {
		return rand;
	}

	@Override
	public String toString() {
		return name + " p = " + mutationProbability;
	}

}