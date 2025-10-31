package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.gp.GeneticProgrammingAlgorithm;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.factory.genotype.PrimitiveSetFactory;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.operators.crossover.TreeCrossover;
import com.knezevic.edaf.genotype.tree.operators.mutation.TreeMutation;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;

import java.util.Random;

/**
 * A factory for creating {@link GeneticProgrammingAlgorithm} objects.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GpFactory implements AlgorithmFactory {

    @Override
    public Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                        Selection<?> selection, Statistics<?> statistics,
                                        TerminationCondition<?> terminationCondition, Random random) throws Exception {

        Crossover crossover = createCrossover(config, random);
        Mutation mutation = createMutation(config, random);

        double crossoverRate = config.getProblem().getGenotype().getCrossing().getProbability();
        double mutationRate = config.getProblem().getGenotype().getMutation().getProbability();
        int elitismSize = config.getAlgorithm().getElitism();

        return new GeneticProgrammingAlgorithm(
                (Problem<TreeIndividual>) problem,
                (Population<TreeIndividual>) population,
                (Selection<TreeIndividual>) selection,
                (Crossover<TreeIndividual>) crossover,
                (Mutation<TreeIndividual>) mutation,
                (TerminationCondition<TreeIndividual>) terminationCondition,
                crossoverRate, mutationRate, elitismSize);
    }

    @Override
    public Crossover createCrossover(Configuration config, Random random) {
        return new TreeCrossover(random);
    }

    @Override
    public Mutation createMutation(Configuration config, Random random) {
        // The factory is the correct place to parse the config and create the components.
        PrimitiveSet ps = PrimitiveSetFactory.create(config);
        int maxDepth = config.getProblem().getGenotype().getMaxDepth();
        return new TreeMutation(ps, maxDepth, random);
    }
}
