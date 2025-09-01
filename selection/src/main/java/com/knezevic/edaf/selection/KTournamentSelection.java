package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.SimplePopulation;

import java.util.Random;

/**
 * K-Tournament selection.
 *
 * @param <T> The type of individual.
 */
public class KTournamentSelection<T extends Individual> implements Selection<T> {

    private final Random random;
    private final int k;

    public KTournamentSelection(Random random, int k) {
        this.random = random;
        this.k = k;
    }

    @Override
    public Population<T> select(Population<T> population, int n) {
        Population<T> selected = new SimplePopulation<>();
        for (int i = 0; i < n; i++) {
            Population<T> tournament = new SimplePopulation<>();
            for (int j = 0; j < k; j++) {
                tournament.add(population.getIndividual(random.nextInt(population.getSize())));
            }
            tournament.sort();
            selected.add(tournament.getBest());
        }
        return selected;
    }
}
