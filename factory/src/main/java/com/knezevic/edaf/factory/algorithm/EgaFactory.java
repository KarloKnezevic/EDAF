package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.ega.eGA;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;

import java.util.Random;

/**
 * A factory for creating {@link eGA} algorithm objects.
 */
public class EgaFactory extends GeneticAlgorithmFactory {
    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population, Selection selection, Statistics statistics, TerminationCondition terminationCondition, Random random) throws Exception {
        if (population == null) throw new IllegalArgumentException("eGA requires a population.");
        if (selection == null) throw new IllegalArgumentException("eGA requires a selection method.");
        Crossover crossover = createCrossover(config, random);
        Mutation mutation = createMutation(config, random);
        return new eGA(problem, population, selection, crossover, mutation, terminationCondition);
    }
}
