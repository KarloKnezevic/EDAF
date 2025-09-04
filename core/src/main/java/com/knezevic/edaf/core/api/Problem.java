package com.knezevic.edaf.core.api;

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

    /**
     * Gets the optimization type (e.g., MINIMIZE or MAXIMIZE).
     *
     * @return The optimization type.
     */
    OptimizationType getOptimizationType();

}
