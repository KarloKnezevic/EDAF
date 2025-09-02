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

@SuppressWarnings({"rawtypes", "unchecked"})
public class GpFactory implements AlgorithmFactory<TreeIndividual> {

    @Override
    public Algorithm<TreeIndividual> createAlgorithm(Configuration config, Problem<TreeIndividual> problem, Population<TreeIndividual> population,
                                                     Selection<TreeIndividual> selection, Statistics<TreeIndividual> statistics,
                                                     TerminationCondition<TreeIndividual> terminationCondition, Random random) {

        Crossover crossover = createCrossover(config, random);
        Mutation mutation = createMutation(config, random);

        double crossoverRate = config.getProblem().getGenotype().getCrossing().getProbability();
        double mutationRate = config.getProblem().getGenotype().getMutation().getProbability();
        int elitismSize = config.getAlgorithm().getElitism();

        return new GeneticProgrammingAlgorithm(problem, population, selection, crossover, mutation,
                terminationCondition, crossoverRate, mutationRate, elitismSize);
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
