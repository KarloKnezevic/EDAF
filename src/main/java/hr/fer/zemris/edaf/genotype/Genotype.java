package hr.fer.zemris.edaf.genotype;

/**
 * Genotype.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Genotype {

	private final Mutation mutation;

	private final Crossing crossing;

	private final Individual individual;

	public Genotype(Mutation mutation, Crossing crossing, Individual individual) {

		this.mutation = mutation;
		this.crossing = crossing;
		this.individual = individual;

	}

	public Mutation getMutation() {
		return mutation;
	}

	public Crossing getCrossing() {
		return crossing;
	}

	public Individual getIndividual() {
		return individual;
	}

}