package com.knezevic.edaf.factory;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactory;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactoryProvider;
import com.knezevic.edaf.factory.genotype.GenotypeFactory;
import com.knezevic.edaf.factory.genotype.GenotypeFactoryProvider;
import com.knezevic.edaf.factory.population.DefaultPopulationFactory;
import com.knezevic.edaf.factory.population.PopulationFactory;
import com.knezevic.edaf.factory.problem.DefaultProblemFactory;
import com.knezevic.edaf.factory.problem.ProblemFactory;
import com.knezevic.edaf.factory.selection.SelectionFactory;
import com.knezevic.edaf.factory.selection.SelectionFactoryProvider;
import com.knezevic.edaf.factory.statistics.DefaultStatisticsFactory;
import com.knezevic.edaf.factory.statistics.StatisticsFactory;
import com.knezevic.edaf.factory.termination.DefaultTerminationConditionFactory;
import com.knezevic.edaf.factory.termination.TerminationConditionFactory;

import java.util.Random;

public class DefaultComponentFactory implements ComponentFactory {

    private final ProblemFactory problemFactory;
    private final PopulationFactory populationFactory;
    private final StatisticsFactory statisticsFactory;
    private final TerminationConditionFactory terminationConditionFactory;

    public DefaultComponentFactory() {
        this.problemFactory = new DefaultProblemFactory();
        this.populationFactory = new DefaultPopulationFactory();
        this.statisticsFactory = new DefaultStatisticsFactory();
        this.terminationConditionFactory = new DefaultTerminationConditionFactory();
    }

    @Override
    public Problem createProblem(Configuration config) throws Exception {
        return problemFactory.create(config);
    }

    @Override
    public Genotype createGenotype(Configuration config, Random random) throws Exception {
        GenotypeFactory genotypeFactory = GenotypeFactoryProvider.getFactory(config);
        if (genotypeFactory != null) {
            return genotypeFactory.create(config, random);
        }
        return null;
    }

    @Override
    public Population createPopulation(Configuration config, Genotype genotype) throws Exception {
        return populationFactory.create(config, genotype);
    }

    @Override
    public Statistics createStatistics(Configuration config, Genotype genotype, Random random) throws Exception {
        return statisticsFactory.create(config, genotype, random);
    }

    @Override
    public Selection createSelection(Configuration config, Random random) throws Exception {
        SelectionFactory selectionFactory = SelectionFactoryProvider.getFactory(config);
        if (selectionFactory != null) {
            return selectionFactory.create(config, random);
        }
        return null;
    }

    @Override
    public TerminationCondition createTerminationCondition(Configuration config) throws Exception {
        return terminationConditionFactory.create(config);
    }

    @Override
    public Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                                     Selection selection, Statistics statistics,
                                     TerminationCondition terminationCondition, Random random) throws Exception {
        AlgorithmFactory algorithmFactory = AlgorithmFactoryProvider.getFactory(config);
        if (algorithmFactory != null) {
            return algorithmFactory.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);
        }
        return null;
    }
}
