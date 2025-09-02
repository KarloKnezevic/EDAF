package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.boa.Boa;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * A factory for creating {@link Boa} algorithm objects.
 */
public class BoaFactory implements AlgorithmFactory<FpIndividual> {
    @Override
    public Algorithm<FpIndividual> createAlgorithm(Configuration config, Problem<FpIndividual> problem, Population<FpIndividual> population,
                                                   Selection<FpIndividual> selection, Statistics<FpIndividual> statistics,
                                                   TerminationCondition<FpIndividual> terminationCondition, Random random) throws Exception {
        int nInit = (int) config.getAlgorithm().getParameters().get("n_init");
        int nIter = (int) config.getAlgorithm().getParameters().get("n_iter");
        int genotypeLength = config.getProblem().getGenotype().getLength();
        double min = config.getProblem().getGenotype().getLowerBound();
        double max = config.getProblem().getGenotype().getUpperBound();
        return new Boa(problem, nInit, nIter, genotypeLength, min, max);
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        return null;
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        return null;
    }
}
