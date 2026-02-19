package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.boa.Boa;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * A factory for creating {@link Boa} algorithm objects.
 */
public class BoaFactory implements AlgorithmFactory {
    @Override
    public Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                        Selection<?> selection, Statistics<?> statistics,
                                        TerminationCondition<?> terminationCondition, Random random) throws Exception {
        int nInit = (int) config.getAlgorithm().getParameters().get("n_init");
        int nIter = (int) config.getAlgorithm().getParameters().get("n_iter");
        int genotypeLength = config.getProblem().getGenotype().getLength();
        double min = config.getProblem().getGenotype().getLowerBound();
        double max = config.getProblem().getGenotype().getUpperBound();
        @SuppressWarnings("unchecked")
        Problem<FpIndividual> fpProblem = (Problem<FpIndividual>) problem;
        @SuppressWarnings("unchecked")
        TerminationCondition<FpIndividual> fpTermination = (TerminationCondition<FpIndividual>) terminationCondition;
        return new Boa(fpProblem, nInit, nIter, genotypeLength, min, max, fpTermination);
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
