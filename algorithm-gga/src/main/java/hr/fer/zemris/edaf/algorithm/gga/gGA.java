package hr.fer.zemris.edaf.algorithm.gga;

import hr.fer.zemris.edaf.core.Algorithm;
import hr.fer.zemris.edaf.core.Crossover;
import hr.fer.zemris.edaf.core.Individual;
import hr.fer.zemris.edaf.core.Mutation;
import hr.fer.zemris.edaf.core.Population;
import hr.fer.zemris.edaf.core.Problem;
import hr.fer.zemris.edaf.core.Selection;
import hr.fer.zemris.edaf.core.SimplePopulation;
import hr.fer.zemris.edaf.core.TerminationCondition;

/**
 * A generational Genetic Algorithm (gGA).
 *
 * @param <T> The type of individual in the population.
 */
public class gGA<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Population<T> population;
    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final TerminationCondition<T> terminationCondition;
    private final int elitism;

    private T best;
    private int generation;

    public gGA(Problem<T> problem, Population<T> population, Selection<T> selection,
               Crossover<T> crossover, Mutation<T> mutation,
               TerminationCondition<T> terminationCondition, int elitism) {
        this.problem = problem;
        this.population = population;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.terminationCondition = terminationCondition;
        this.elitism = elitism;
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
            // 2.1. Create new population
            Population<T> newPopulation = new SimplePopulation<>();

            // 2.2. Elitism
            for (int i = 0; i < elitism; i++) {
                newPopulation.add((T) population.get(i).copy());
            }

            // 2.3. Crossover and mutation
            while (newPopulation.size() < population.size()) {
                // Select parents
                Population<T> parents = selection.select(population, 2);
                // Crossover
                T offspring = crossover.crossover(parents.get(0), parents.get(1));
                // Mutation
                mutation.mutate(offspring);
                // Evaluate
                problem.evaluate(offspring);
                // Add to new population
                newPopulation.add(offspring);
            }

            // 2.4. Replace old population
            population.clear();
            population.addAll(newPopulation);
            population.sort();

            // 2.5. Update best individual
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
