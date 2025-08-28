package hr.fer.zemris.edaf.core;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A simple implementation of the Population interface.
 *
 * @param <T> The type of individual in the population.
 */
public class SimplePopulation<T extends Individual> extends ArrayList<T> implements Population<T> {

    @Override
    public T getBest() {
        return stream().min(Comparator.comparingDouble(Individual::getFitness)).orElse(null);
    }

    @Override
    public T getWorst() {
        return stream().max(Comparator.comparingDouble(Individual::getFitness)).orElse(null);
    }

    @Override
    public void sort() {
        sort(Comparator.comparingDouble(Individual::getFitness));
    }
}
