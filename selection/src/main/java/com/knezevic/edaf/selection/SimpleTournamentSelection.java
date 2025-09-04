package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.SimplePopulation;

import java.util.Random;

/**
 * Simple tournament selection (k=2).
 *
 * @param <T> The type of individual.
 */
public class SimpleTournamentSelection<T extends Individual> implements Selection<T> {

    private final Random random;

    public SimpleTournamentSelection(Random random) {
        this.random = random;
    }

    @Override
    public Population<T> select(Population<T> population, int n) {
        Population<T> selected = new SimplePopulation<>(population.getOptimizationType());
        for (int i = 0; i < n; i++) {
            T individual1 = population.getIndividual(random.nextInt(population.getSize()));
            T individual2 = population.getIndividual(random.nextInt(population.getSize()));

            boolean individual1IsBetter;
            if (population.getOptimizationType() == com.knezevic.edaf.core.api.OptimizationType.MAXIMIZE) {
                individual1IsBetter = individual1.getFitness() > individual2.getFitness();
            } else {
                individual1IsBetter = individual1.getFitness() < individual2.getFitness();
            }

            if (individual1IsBetter) {
                selected.add(individual1);
            } else {
                selected.add(individual2);
            }
        }
        return selected;
    }
}
