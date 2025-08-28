package hr.fer.zemris.edaf.core;

/**
 * A listener for monitoring the progress of an algorithm.
 */
public interface ProgressListener {

    /**
     * Called by the algorithm at the end of each generation.
     *
     * @param generation The current generation number (starting from 1).
     * @param population The population at the end of the generation.
     */
    void onGenerationDone(int generation, Population population);

}
