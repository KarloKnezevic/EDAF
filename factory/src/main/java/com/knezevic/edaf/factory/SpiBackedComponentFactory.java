package com.knezevic.edaf.factory;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;
import com.knezevic.edaf.core.spi.SelectionProvider;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactory;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactoryProvider;
import com.knezevic.edaf.factory.algorithm.GeneticAlgorithmFactory;
import com.knezevic.edaf.factory.algorithm.GpFactory;
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
            // Provide a default execution context if algorithm supports it
            long seed =  System.currentTimeMillis();
            try {
                Object seedParam = config.getAlgorithm().getParameters() != null ? config.getAlgorithm().getParameters().get("seed") : null;
                if (seedParam instanceof Number n) {
                    seed = n.longValue();
                }
            } catch (Exception ignored) {}
            RandomSource rs = new SplittableRandomSource(seed);
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
            EventPublisher events = new ConsoleEventPublisher();
            if ("true".equalsIgnoreCase(System.getProperty("edaf.metrics.enabled"))) {
                try {
                    if (System.getProperty("edaf.metrics.prometheus.port") != null) {
                        Class<?> cls = Class.forName("com.knezevic.edaf.metrics.PrometheusEventPublisher");
                        events = (EventPublisher) cls.getConstructor().newInstance();
                    } else {
                        Class<?> cls = Class.forName("com.knezevic.edaf.metrics.MicrometerEventPublisher");
                        events = (EventPublisher) cls.getConstructor().newInstance();
                    }
                } catch (Throwable ignored) { }
            }
            ExecutionContext ctx = new ExecutionContext(rs, executor, events);
            if (fromProvider instanceof SupportsExecutionContext s) {
                s.setExecutionContext(ctx);
            }
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

        int selectionSize = config.getAlgorithm() != null && config.getAlgorithm().getSelection() != null
                ? config.getAlgorithm().getSelection().getSize()
                : Math.max(1, population.getSize() / 2);
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
        final Crossover<?> cFinal = crossover;
        final Mutation<?> mFinal = mutation;

        Algorithm<?> fromProvider = StreamSupport.stream(loader.spliterator(), false)
                .filter(p -> p.id().equalsIgnoreCase(algorithmId))
                .filter(p -> p.supports(genotypeOf(genotypeId), problem.getClass()))
                .findFirst()
                .map(p -> p.create(problem, population, selection, cFinal, mFinal, statistics, terminationCondition, random, selectionSize, genotypeLength))
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


