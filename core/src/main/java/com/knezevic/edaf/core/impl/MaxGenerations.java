package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.TerminationCondition;

/**
 * A termination condition that stops the algorithm after a fixed number of generations.
 *
 * @param <T> The type of individual.
 */
public class MaxGenerations<T extends Individual> implements TerminationCondition<T> {

    private final int maxGenerations;

    public MaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    @Override
    public boolean shouldTerminate(Algorithm<T> algorithm) {
        return algorithm.getGeneration() >= maxGenerations;
    }
}
