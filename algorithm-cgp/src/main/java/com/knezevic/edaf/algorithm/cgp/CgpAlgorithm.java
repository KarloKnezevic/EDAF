package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.operator.CgpCrossoverOperator;
import com.knezevic.edaf.algorithm.cgp.operator.CgpMutationOperator;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.core.impl.SimplePopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CgpAlgorithm extends AbstractAlgorithm<CgpIndividual> {

    private static final Logger logger = LoggerFactory.getLogger(CgpAlgorithm.class);

    private final CgpConfig config;
    private final CgpDecoder decoder;
    private final CgpGenotypeFactory genotypeFactory;
    private final Selection<CgpIndividual> selection;
    private final CgpMutationOperator mutation;
    private final CgpCrossoverOperator crossover;
    private final Random random;
    private final TerminationCondition<CgpIndividual> terminationCondition;

    private Population<CgpIndividual> population;

    public CgpAlgorithm(CgpConfig config, Problem<CgpIndividual> problem, CgpDecoder decoder,
                        CgpGenotypeFactory genotypeFactory, Selection<CgpIndividual> selection,
                        CgpMutationOperator mutation, CgpCrossoverOperator crossover, Random random,
                        TerminationCondition<CgpIndividual> terminationCondition) {
        super(problem, "cgp");
        this.config = config;
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
        publishAlgorithmStarted();

        initialize();

        while (!terminationCondition.shouldTerminate(this)) {
            incrementGeneration();

            if (config.getReplacementStrategy() == ReplacementStrategy.GENERATIONAL) {
                runGenerationalEpoch();
            } else {
                runSteadyStateEpoch();
            }

            population.sort();
            setBest(population.getBest());

            logger.info("Generation: {}, Best Fitness: {}", getGeneration(), getBest().getFitness());
            notifyListener();
            publishGenerationCompleted();
        }

        publishAlgorithmTerminated();
    }

    private void runGenerationalEpoch() {
        // Elitism: preserve the best individual from current population
        population.sort();
        CgpIndividual bestFromCurrent = (CgpIndividual) population.getBest().copy();

        List<CgpIndividual> offspring = new ArrayList<>();
        for (int i = 0; i < config.getPopulationSize(); i++) {
            offspring.add(createOffspring());
        }

        // Evaluate all offspring in parallel if context provides executor
        long e0 = System.nanoTime();
        evaluateIndividuals(offspring);
        long e1 = System.nanoTime();
        publishEvaluationCompleted(getGeneration(), offspring.size(), e1 - e0);

        // Replace population with offspring
        for (int i = 0; i < offspring.size(); i++) {
            population.setIndividual(i, offspring.get(i));
        }
        population.sort();

        // Ensure best individual is preserved (elitism)
        CgpIndividual currentBest = population.getBest();
        if (isFirstBetter(bestFromCurrent, currentBest)) {
            // Best from previous generation is better, replace worst with it
            population.remove(population.getWorst());
            population.add(bestFromCurrent);
            population.sort();
        }
    }

    private void runSteadyStateEpoch() {
        CgpIndividual offspring = createOffspring();
        // Evaluate the single offspring
        long e0 = System.nanoTime();
        problem.evaluate(offspring);
        long e1 = System.nanoTime();
        publishEvaluationCompleted(getGeneration(), 1, e1 - e0);

        population.sort();
        // Elitism: never replace the best individual
        CgpIndividual currentBest = population.getBest();
        CgpIndividual worst = population.getWorst();

        // Only replace worst if offspring is better AND worst is not the current best
        if (isFirstBetter(offspring, worst) && worst != currentBest) {
            population.setIndividual(population.getSize() - 1, offspring);
        } else if (isFirstBetter(offspring, currentBest)) {
            // Offspring is better than current best, replace worst with offspring
            population.setIndividual(population.getSize() - 1, offspring);
        }
        population.sort();
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

        return child;
    }

    private void initialize() {
        this.population = new SimplePopulation<>(problem.getOptimizationType());
        List<CgpIndividual> initialIndividuals = new ArrayList<>();

        for (int i = 0; i < config.getPopulationSize(); i++) {
            int[] genotype = genotypeFactory.create();
            CgpIndividual individual = new CgpIndividual(genotype);
            decoder.decode(individual);
            initialIndividuals.add(individual);
        }

        // Evaluate initial population in parallel if context provides executor
        long t0 = System.nanoTime();
        evaluateIndividuals(initialIndividuals);
        long t1 = System.nanoTime();
        publishEvaluationCompleted(0, initialIndividuals.size(), t1 - t0);

        for (CgpIndividual individual : initialIndividuals) {
            population.add(individual);
        }

        population.sort();
        setBest(population.getBest());
        setGeneration(0);
        notifyListener();
        publishGenerationCompleted();
    }

    /**
     * Evaluates a list of CgpIndividuals in parallel using the context executor
     * or a default thread pool. This is distinct from the base class's
     * evaluatePopulation(Population) because it operates on a List.
     */
    private void evaluateIndividuals(List<CgpIndividual> individuals) {
        ExecutorService executor = getContext() != null && getContext().getExecutor() != null
                ? getContext().getExecutor()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = new ArrayList<>();
        for (CgpIndividual individual : individuals) {
            tasks.add(() -> {
                problem.evaluate(individual);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Evaluation interrupted", e);
        }

        if (getContext() == null) {
            executor.shutdown();
        }
    }

    @Override
    public Population<CgpIndividual> getPopulation() {
        return population;
    }
}
