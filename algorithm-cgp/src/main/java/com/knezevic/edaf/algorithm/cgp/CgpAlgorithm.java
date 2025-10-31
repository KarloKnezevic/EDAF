package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.algorithm.cgp.operator.CgpCrossoverOperator;
import com.knezevic.edaf.algorithm.cgp.operator.CgpMutationOperator;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.ProgressListener;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CgpAlgorithm implements Algorithm<CgpIndividual>, SupportsExecutionContext {

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
    private ExecutionContext context;

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
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmStarted("cgp"));
        }
        
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
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new GenerationCompleted("cgp", generation, bestIndividual));
            }
        }
        
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new AlgorithmTerminated("cgp", generation));
        }
    }

    private void runGenerationalEpoch() {
        List<CgpIndividual> offspring = new ArrayList<>();
        for (int i = 0; i < config.getPopulationSize(); i++) {
            offspring.add(createOffspring());
        }
        
        // Evaluate all offspring in parallel if context provides executor
        long e0 = System.nanoTime();
        evaluatePopulation(offspring);
        long e1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new EvaluationCompleted("cgp", generation, offspring.size(), e1 - e0));
        }
        
        for(int i=0; i<offspring.size(); i++){
            population.setIndividual(i, offspring.get(i));
        }
    }

    private void runSteadyStateEpoch() {
        CgpIndividual offspring = createOffspring();
        // Evaluation is done in createOffspring()
        
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
        
        // Evaluate single individual (steady-state) or batch (generational)
        long e0 = System.nanoTime();
        problem.evaluate(child);
        long e1 = System.nanoTime();
        if (context != null && context.getEvents() != null && config.getReplacementStrategy() == ReplacementStrategy.STEADY_STATE) {
            // For steady-state, emit individual evaluation
            context.getEvents().publish(new EvaluationCompleted("cgp", generation, 1, e1 - e0));
        }
        
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
        evaluatePopulation(initialIndividuals);
        long t1 = System.nanoTime();
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new EvaluationCompleted("cgp", 0, initialIndividuals.size(), t1 - t0));
        }
        
        for (CgpIndividual individual : initialIndividuals) {
            population.add(individual);
        }
        
        population.sort();
        this.bestIndividual = population.getBest();
        this.generation = 0;
        if (listener != null) {
            listener.onGenerationDone(generation, bestIndividual, population);
        }
        if (context != null && context.getEvents() != null) {
            context.getEvents().publish(new GenerationCompleted("cgp", 0, bestIndividual));
        }
    }
    
    private void evaluatePopulation(List<CgpIndividual> individuals) {
        ExecutorService executor = context != null && context.getExecutor() != null
                ? context.getExecutor()
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
        
        if (context == null) {
            executor.shutdown();
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
    
    @Override
    public void setExecutionContext(ExecutionContext context) {
        this.context = context;
        // Note: Components (CgpMutationOperator, CgpCrossoverOperator, CgpGenotypeFactory)
        // already use RandomSource, which was provided during construction.
        // If a new RandomSource is needed from context, it would require recreating components,
        // which is typically not necessary as the initial RandomSource is sufficient.
    }
}
