package hr.fer.zemris.edaf.core;

/**
 * Defines the optimization problem.
 *
 * @param <T> The type of individual to be evaluated.
 */
public interface Problem<T extends Individual> {

    /**
     * Evaluates the fitness of an individual and sets it.
     *
     * @param individual The individual to be evaluated.
     */
    void evaluate(T individual);

}
