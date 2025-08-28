package hr.fer.zemris.edaf.core;

/**
 * Represents the genotype of an individual.
 *
 * @param <G> The type of the genotype instance.
 */
public interface Genotype<G> {

    /**
     * Creates a new genotype instance.
     *
     * @return A new genotype.
     */
    G create();

    /**
     * Gets the length of the genotype.
     *
     * @return The length.
     */
    int getLength();
}
