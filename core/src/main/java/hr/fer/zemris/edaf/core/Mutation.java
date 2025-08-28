package hr.fer.zemris.edaf.core;

/**
 * Defines the mutation operator.
 *
 * @param <T> The type of individual.
 */
public interface Mutation<T extends Individual> {

    /**
     * Mutates an individual.
     *
     * @param individual The individual to mutate.
     */
    void mutate(T individual);
}
