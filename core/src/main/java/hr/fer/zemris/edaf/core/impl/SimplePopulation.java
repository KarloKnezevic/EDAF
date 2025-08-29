package hr.fer.zemris.edaf.core.impl;

import hr.fer.zemris.edaf.core.api.Individual;
import hr.fer.zemris.edaf.core.api.Population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A simple implementation of the Population interface, backed by an ArrayList.
 *
 * @param <T> The type of individual in the population.
 */
public class SimplePopulation<T extends Individual> implements Population<T> {

    private final List<T> individuals;

    public SimplePopulation() {
        this.individuals = new ArrayList<>();
    }

    @Override
    public int getSize() {
        return individuals.size();
    }

    @Override
    public T getIndividual(int index) {
        return individuals.get(index);
    }

    @Override
    public void add(T individual) {
        individuals.add(individual);
    }

    @Override
    public void addAll(Collection<T> individuals) {
        this.individuals.addAll(individuals);
    }

    @Override
    public void setIndividual(int index, T individual) {
        individuals.set(index, individual);
    }

    @Override
    public void remove(T individual) {
        individuals.remove(individual);
    }

    @Override
    public void clear() {
        individuals.clear();
    }

    @Override
    public T getBest() {
        return individuals.stream().min(Comparator.comparingDouble(Individual::getFitness)).orElse(null);
    }

    @Override
    public T getWorst() {
        return individuals.stream().max(Comparator.comparingDouble(Individual::getFitness)).orElse(null);
    }

    @Override
    public void sort() {
        individuals.sort(Comparator.comparingDouble(Individual::getFitness));
    }

    @Override
    public Iterator<T> iterator() {
        return individuals.iterator();
    }
}
