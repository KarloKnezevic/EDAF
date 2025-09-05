package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.operator.CgpCrossoverOperator;
import com.knezevic.edaf.algorithm.cgp.operator.CgpMutationOperator;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.ProgressListener;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.SimplePopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CgpAlgorithm implements Algorithm<CgpIndividual> {

    private static final Logger logger = LoggerFactory.getLogger(CgpAlgorithm.class);

    private final CgpConfig config;
    private final Problem<CgpIndividual> problem;
    private final CgpDecoder decoder;
    private final CgpGenotypeFactory genotypeFactory;
    private final Selection<CgpIndividual> selection;
    private final CgpMutationOperator mutation;
    private final CgpCrossoverOperator crossover;
    private final Random random;
    private final TerminationCondition<CgpIndividual> terminationCondition;

    private Population<CgpIndividual> population;
    private CgpIndividual bestIndividual;
    private int generation;
    private ProgressListener listener;

    public CgpAlgorithm(CgpConfig config, Problem<CgpIndividual> problem, CgpDecoder decoder,
                        CgpGenotypeFactory genotypeFactory, Selection<CgpIndividual> selection,
                        CgpMutationOperator mutation, CgpCrossoverOperator crossover, Random random,
                        TerminationCondition<CgpIndividual> terminationCondition) {
        this.config = config;
        this.problem = problem;
        this.decoder = decoder;
        this.genotypeFactory = genotypeFactory;
        this.selection = selection;
        this.mutation = mutation;
        this.crossover = crossover;
        this.random = random;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public void run() {
        initialize();

        while (!terminationCondition.shouldTerminate(this)) {
            generation++;

            if (config.getReplacementStrategy() == ReplacementStrategy.GENERATIONAL) {
                runGenerationalEpoch();
            } else {
                runSteadyStateEpoch();
            }

            population.sort();
            bestIndividual = population.getBest();

            logger.info("Generation: {}, Best Fitness: {}", generation, bestIndividual.getFitness());
            if (listener != null) {
                listener.onGenerationDone(generation, bestIndividual, population);
            }
        }
    }

    private void runGenerationalEpoch() {
        List<CgpIndividual> offspring = new ArrayList<>();
        for (int i = 0; i < config.getPopulationSize(); i++) {
            offspring.add(createOffspring());
        }
        for(int i=0; i<offspring.size(); i++){
            population.setIndividual(i, offspring.get(i));
        }
    }

    private void runSteadyStateEpoch() {
        CgpIndividual offspring = createOffspring();
        population.sort();
        // After sorting, the worst individual is always at the last position.
        population.setIndividual(population.getSize() - 1, offspring);
    }

    private CgpIndividual createOffspring() {
        Population<CgpIndividual> parents = selection.select(population, 2);
        CgpIndividual parent1 = parents.getIndividual(0);
        CgpIndividual parent2 = parents.getIndividual(1);

        CgpIndividual child;
        if (config.isUseCrossover() && random.nextDouble() < config.getCrossoverRate()) {
            child = crossover.crossover(parent1, parent2);
        } else {
            child = (CgpIndividual) parent1.copy();
        }

        mutation.mutate(child);
        decoder.decode(child);
        problem.evaluate(child);
        return child;
    }

    private void initialize() {
        this.population = new SimplePopulation<>(problem.getOptimizationType());
        for (int i = 0; i < config.getPopulationSize(); i++) {
            int[] genotype = genotypeFactory.create();
            CgpIndividual individual = new CgpIndividual(genotype);
            decoder.decode(individual);
            problem.evaluate(individual);
            population.add(individual);
        }
        population.sort();
        this.bestIndividual = population.getBest();
        this.generation = 0;
        if (listener != null) {
            listener.onGenerationDone(generation, bestIndividual, population);
        }
    }

    @Override
    public CgpIndividual getBest() {
        return bestIndividual;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<CgpIndividual> getPopulation() {
        return population;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
