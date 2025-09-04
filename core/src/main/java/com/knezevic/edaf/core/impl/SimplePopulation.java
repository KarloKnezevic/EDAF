package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Population;

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
    private final OptimizationType optimizationType;

    /**
     * Creates a new, empty population with a specified optimization type.
     *
     * @param optimizationType The optimization type for this population.
     */
    public SimplePopulation(OptimizationType optimizationType) {
        this.individuals = new ArrayList<>();
        this.optimizationType = optimizationType;
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
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
        Comparator<Individual> comparator = Comparator.comparingDouble(Individual::getFitness);
        return individuals.stream()
                .min(optimizationType == OptimizationType.MINIMIZE ? comparator : comparator.reversed())
                .orElse(null);
    }

    @Override
    public T getWorst() {
        Comparator<Individual> comparator = Comparator.comparingDouble(Individual::getFitness);
        return individuals.stream()
                .max(optimizationType == OptimizationType.MINIMIZE ? comparator : comparator.reversed())
                .orElse(null);
    }

    @Override
    public void sort() {
        Comparator<Individual> comparator = Comparator.comparingDouble(Individual::getFitness);
        individuals.sort(optimizationType == OptimizationType.MINIMIZE ? comparator : comparator.reversed());
    }

    @Override
    public Iterator<T> iterator() {
        return individuals.iterator();
    }
}
