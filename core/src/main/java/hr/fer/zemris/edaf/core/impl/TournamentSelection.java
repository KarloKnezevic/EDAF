package hr.fer.zemris.edaf.core.impl;

import hr.fer.zemris.edaf.core.api.Individual;
import hr.fer.zemris.edaf.core.api.Population;
import hr.fer.zemris.edaf.core.api.Selection;

import java.util.Random;

/**
 * Tournament selection.
 *
 * @param <T> The type of individual.
 */
public class TournamentSelection<T extends Individual> implements Selection<T> {

    private final Random random;
    private final int size;

    public TournamentSelection(Random random, int size) {
        this.random = random;
        this.size = size;
    }

    @Override
    public Population<T> select(Population<T> population, int n) {
        Population<T> selected = new SimplePopulation<>();
        for (int i = 0; i < n; i++) {
            Population<T> tournament = new SimplePopulation<>();
            for (int j = 0; j < size; j++) {
                tournament.add(population.getIndividual(random.nextInt(population.getSize())));
            }
            tournament.sort();
            selected.add(tournament.getBest());
        }
        return selected;
    }
}
