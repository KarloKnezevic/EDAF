package hr.fer.zemris.edaf.core.api;

/**
 * Defines the termination condition for an algorithm.
 *
 * @param <T> The type of individual in the population.
 */
public interface TerminationCondition<T extends Individual> {

    /**
     * Checks if the algorithm should terminate.
     *
     * @param algorithm The algorithm to check.
     * @return {@code true} if the algorithm should terminate, {@code false} otherwise.
     */
    boolean shouldTerminate(Algorithm<T> algorithm);
}
