package com.knezevic.edaf.factory.algorithm;

import com.knezevic.edaf.algorithm.gp.GeneticProgrammingAlgorithm;
import com.knezevic.edaf.algorithm.gp.operators.TreeCrossover;
import com.knezevic.edaf.algorithm.gp.operators.TreeMutation;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

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
        return new TreeMutation(config, random);
    }
}
