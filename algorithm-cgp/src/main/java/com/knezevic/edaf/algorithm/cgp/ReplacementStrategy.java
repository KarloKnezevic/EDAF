package com.knezevic.edaf.algorithm.cgp;

/**
 * Defines the replacement strategy for the CGP algorithm.
 */
public enum ReplacementStrategy {
    /**
     * Generational replacement: the entire population is replaced by the offspring.
     */
    GENERATIONAL,

    /**
     * Steady-state replacement: only the worst individuals are replaced by the offspring.
     */
    STEADY_STATE
}
