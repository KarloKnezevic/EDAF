package hr.fer.zemris.edaf.core.api;

/**
 * A listener for monitoring the progress of an algorithm.
 */
public interface ProgressListener {

    /**
     * Called by the algorithm at the end of each generation.
     *
     * @param generation The current generation number (starting from 1).
     * @param bestInGeneration The best individual in the current generation.
     * @param population The population at the end of the generation (can be null).
     */
    void onGenerationDone(int generation, Individual bestInGeneration, Population population);

}
