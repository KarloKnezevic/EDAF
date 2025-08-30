package hr.fer.zemris.edaf.core.api;

/**
 * Represents an individual in the population.
 *
 * @param <G> The type of genotype.
 */
public interface Individual<G> {

    /**
     * Gets the fitness of the individual.
     *
     * @return The fitness value.
     */
    double getFitness();

    /**
     * Sets the fitness of the individual.
     *
     * @param fitness The fitness value.
     */
    void setFitness(double fitness);

    /**
     * Gets the genotype of the individual.
     *
     * @return The genotype.
     */
    G getGenotype();

    /**
     * Creates a copy of the individual.
     *
     * @return A new instance of the individual with the same properties.
     */
    Individual<G> copy();
}
