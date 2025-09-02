package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.ltga.LTGA;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.genotype.binary.mutation.SimpleMutation;

import java.util.Random;

/**
 * A factory for creating {@link LTGA} algorithm objects.
 *
 * @param <T> The type of individual.
 */
public class LtgaFactory<T extends Individual> implements AlgorithmFactory<T> {
    @Override
    public Algorithm<T> createAlgorithm(Configuration config, Problem<T> problem, Population<T> population,
                                     Selection<T> selection, Statistics<T> statistics,
                                     TerminationCondition<T> terminationCondition, Random random) throws Exception {
        int length = config.getProblem().getGenotype().getLength();
        Mutation mutation = createMutation(config, random);
        return (Algorithm<T>) new LTGA((Problem<BinaryIndividual>) problem, (Population<BinaryIndividual>) population,
                (Selection<BinaryIndividual>) selection, (Mutation<BinaryIndividual>) mutation,
                (TerminationCondition<BinaryIndividual>) terminationCondition, length, random);
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        return null;
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        double mutationProbability = config.getProblem().getGenotype().getMutation().getProbability();
        return new SimpleMutation(random, mutationProbability);
    }
}
