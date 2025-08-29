package hr.fer.zemris.edaf.algorithm.mimic;

import hr.fer.zemris.edaf.core.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The MIMIC algorithm.
 */
public class MIMIC implements Algorithm<BinaryIndividual> {

    private final Problem<BinaryIndividual> problem;
    private final Population<BinaryIndividual> population;
    private final Selection<BinaryIndividual> selection;
    private final Statistics<BinaryIndividual> statistics;
    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int selectionSize;

    private BinaryIndividual best;
    private int generation;
    private ProgressListener listener;

    public MIMIC(Problem<BinaryIndividual> problem, Population<BinaryIndividual> population,
                 Selection<BinaryIndividual> selection, Statistics<BinaryIndividual> statistics,
                 TerminationCondition<BinaryIndividual> terminationCondition, int selectionSize) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.selectionSize = selectionSize;
    }

    @Override
    public void run() {
        // 1. Initialize population
        evaluatePopulation(population);
        population.sort();
        best = (BinaryIndividual) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<BinaryIndividual> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<BinaryIndividual> newPopulation = statistics.sample(population.size());

            // 2.4. Evaluate new individuals
            evaluatePopulation(newPopulation);

            // 2.5. Replace old population
            population.clear();
            population.addAll(newPopulation);
            population.sort();

            // 2.6. Update best individual
            BinaryIndividual currentBest = population.getBest();
            if (currentBest.getFitness() < best.getFitness()) {
                best = (BinaryIndividual) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
    }

    private void evaluatePopulation(Population<BinaryIndividual> population) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (BinaryIndividual individual : population) {
            tasks.add(() -> {
                problem.evaluate(individual);
                return null;
            });
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    @Override
    public BinaryIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<BinaryIndividual> getPopulation() {
        return population;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
