package hr.fer.zemris.edaf.algorithm.umda;

import hr.fer.zemris.edaf.core.Algorithm;
import hr.fer.zemris.edaf.core.Individual;
import hr.fer.zemris.edaf.core.Population;
import hr.fer.zemris.edaf.core.Problem;
import hr.fer.zemris.edaf.core.Selection;
import hr.fer.zemris.edaf.core.Statistics;
import hr.fer.zemris.edaf.core.TerminationCondition;

/**
 * The Univariate Marginal Distribution Algorithm (UMDA).
 *
 * @param <T> The type of individual in the population.
 */
public class Umda<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int selectionSize;

    private T best;
    private int generation;

    public Umda(Problem<T> problem, Population<T> population, Selection<T> selection,
                Statistics<T> statistics, TerminationCondition<T> terminationCondition,
                int selectionSize) {
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
        for (T individual : population) {
            problem.evaluate(individual);
        }
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Select best individuals
            Population<T> selected = selection.select(population, selectionSize);

            // 2.2. Build probabilistic model
            statistics.estimate(selected);

            // 2.3. Sample new individuals
            Population<T> newPopulation = statistics.sample(population.size());

            // 2.4. Evaluate new individuals
            for (T individual : newPopulation) {
                problem.evaluate(individual);
            }

            // 2.5. Replace old population
            population.clear();
            population.addAll(newPopulation);
            population.sort();

            // 2.6. Update best individual
            T currentBest = population.getBest();
            if (currentBest.getFitness() < best.getFitness()) {
                best = (T) currentBest.copy();
            }

            generation++;
        }
    }

    @Override
    public T getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<T> getPopulation() {
        return population;
    }
}
