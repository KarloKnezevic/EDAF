package hr.fer.zemris.edaf.core;

/**
 * Defines the statistics component for building and sampling probabilistic models.
 *
 * @param <T> The type of individual in the population.
 */
public interface Statistics<T extends Individual> {

    /**
     * Estimates the parameters of the probabilistic model from a population.
     *
     * @param population The population to estimate the model from.
     */
    void estimate(Population<T> population);

    /**
     * Updates the probabilistic model based on a single individual.
     *
     * @param individual The individual to update the model with.
     * @param learningRate The learning rate for the update.
     */
    void update(T individual, double learningRate);

    /**
     * Samples a new population from the probabilistic model.
     *
     * @param size The size of the new population.
     * @return A new population sampled from the model.
     */
    Population<T> sample(int size);
}
