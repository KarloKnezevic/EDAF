package com.knezevic.edaf.factory;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;
import com.knezevic.edaf.core.spi.SelectionProvider;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactory;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactoryProvider;
import com.knezevic.edaf.factory.algorithm.GeneticAlgorithmFactory;
import com.knezevic.edaf.core.runtime.*;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import java.util.Random;

/**
 * ComponentFactory implementation that composes the runtime from ServiceLoader-provided plugins.
 * Falls back to default factories for problems, populations, statistics and termination conditions.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SpiBackedComponentFactory implements ComponentFactory {

    private final DefaultComponentFactory fallback = new DefaultComponentFactory();

    @Override
    public Problem<?> createProblem(Configuration config) throws Exception {
        return fallback.createProblem(config);
    }

    @Override
    public Genotype<?> createGenotype(Configuration config, Random random) throws Exception {
        // For now, delegate to existing factory; genotype SPI can be enabled later.
        return fallback.createGenotype(config, random);
    }

    @Override
    public Population<?> createPopulation(Configuration config, Genotype<?> genotype) throws Exception {
        return fallback.createPopulation(config, genotype);
    }

    @Override
    public Statistics<?> createStatistics(Configuration config, Genotype<?> genotype, Random random) throws Exception {
        return fallback.createStatistics(config, genotype, random);
    }

    @Override
    public Selection<?> createSelection(Configuration config, Random random) throws Exception {
        final String selectionId = Objects.requireNonNull(config.getAlgorithm().getSelection().getName(),
                "Selection name must be provided");

        ServiceLoader<SelectionProvider> loader = ServiceLoader.load(SelectionProvider.class);
        Selection<?> fromProvider = StreamSupport.stream(loader.spliterator(), false)
                .filter(p -> p.id().equalsIgnoreCase(selectionId))
                .findFirst()
                .map(SelectionProvider::create)
                .orElse(null);
        if (fromProvider != null) {
            // Note: ExecutionContext with metrics is created in Framework.run(), not here
            // to avoid creating PrometheusEventPublisher multiple times (causes BindException)
            // Selection providers should not create ExecutionContext - that's done at Framework level
            return fromProvider;
        }
        try {
            return fallback.createSelection(config, random);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TerminationCondition<?> createTerminationCondition(Configuration config) throws Exception {
        return fallback.createTerminationCondition(config);
    }

    @Override
    public Algorithm<?> createAlgorithm(Configuration config, Problem<?> problem, Population<?> population,
                                     Selection<?> selection, Statistics<?> statistics,
                                     TerminationCondition<?> terminationCondition, Random random) throws Exception {

        final String algorithmId = Objects.requireNonNull(config.getAlgorithm().getName(),
                "Algorithm name must be provided").trim();
        final String genotypeId = Objects.requireNonNull(config.getProblem().getGenotype().getType(),
                "Genotype type must be provided").trim();

        int selectionSize;
        if (config.getAlgorithm() != null && config.getAlgorithm().getSelection() != null) {
            selectionSize = config.getAlgorithm().getSelection().getSize();
        } else if (population != null) {
            selectionSize = Math.max(1, population.getSize() / 2);
        } else {
            // Algorithms that don't use an explicit Population (e.g., BOA) can derive their own batch size
            selectionSize = 0;
        }
        int genotypeLength = config.getProblem().getGenotype().getLength();

        ServiceLoader<AlgorithmProvider> loader = ServiceLoader.load(AlgorithmProvider.class);
        // If legacy factory supports GA/GP operators, derive them
        Crossover<?> crossover = null;
        Mutation<?> mutation = null;
        AlgorithmFactory legacyFactory = AlgorithmFactoryProvider.getFactory(config);
        if (legacyFactory instanceof GeneticAlgorithmFactory gaf) {
            crossover = (Crossover<?>) gaf.createCrossover(config, random);
            mutation = (Mutation<?>) gaf.createMutation(config, random);
        } else if (legacyFactory instanceof com.knezevic.edaf.factory.algorithm.GpFactory gpf) {
            // GP also requires crossover and mutation operators
            crossover = (Crossover<?>) gpf.createCrossover(config, random);
            mutation = (Mutation<?>) gpf.createMutation(config, random);
        }
        // If not provided by legacy factories, derive GA operators from genotype settings when available
        if (mutation == null) {
            try {
                GeneticAlgorithmFactory helper = new GeneticAlgorithmFactory() {
                    @Override
                    public Algorithm<?> createAlgorithm(Configuration cfg, Problem<?> pr, Population<?> po, Selection<?> se, Statistics<?> st, TerminationCondition<?> tc, Random rnd) {
                        return null; // Not used; we only need operator constructors
                    }
                };
                // Only create if genotype config provides the required sections
                if (config.getProblem() != null && config.getProblem().getGenotype() != null && config.getProblem().getGenotype().getMutation() != null) {
                    mutation = (Mutation<?>) helper.createMutation(config, random);
                }
                if (crossover == null && config.getProblem() != null && config.getProblem().getGenotype() != null && config.getProblem().getGenotype().getCrossing() != null) {
                    crossover = (Crossover<?>) helper.createCrossover(config, random);
                }
            } catch (Throwable ignored) { }
        }
        final Crossover<?> cFinal = crossover;
        final Mutation<?> mFinal = mutation;

        final int maxGenerations = config.getAlgorithm() != null && config.getAlgorithm().getTermination() != null
                ? config.getAlgorithm().getTermination().getMaxGenerations()
                : Integer.MAX_VALUE;

        Algorithm<?> fromProvider = StreamSupport.stream(loader.spliterator(), false)
                .filter(p -> p.id().equalsIgnoreCase(algorithmId))
                .filter(p -> p.supports(genotypeOf(genotypeId), problem.getClass()))
                .findFirst()
                .map(p -> p.createWithConfig(problem, population, selection, cFinal, mFinal, statistics,
                        terminationCondition, random, selectionSize, genotypeLength, maxGenerations))
                .orElse(null);
        if (fromProvider != null) {
            return fromProvider;
        }
        try {
            return fallback.createAlgorithm(config, problem, population, selection, statistics, terminationCondition, random);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends Genotype> genotypeOf(String id) {
        // This simple resolver keeps compatibility; providers may ignore it.
        if ("binary".equalsIgnoreCase(id)) return com.knezevic.edaf.core.api.Genotype.class;
        return com.knezevic.edaf.core.api.Genotype.class;
    }
}


