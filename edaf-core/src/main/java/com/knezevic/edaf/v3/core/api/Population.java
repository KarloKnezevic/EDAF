/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Mutable population aggregate with helper methods used by algorithms and metrics.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class Population<G> implements Iterable<Individual<G>> {

    private final ObjectiveSense objectiveSense;
    private final List<Individual<G>> individuals;

    /**
     * Creates an empty population bound to one objective sense.
     *
     * @param objectiveSense objective optimization sense
     */
    public Population(ObjectiveSense objectiveSense) {
        this.objectiveSense = Objects.requireNonNull(objectiveSense, "objectiveSense must not be null");
        this.individuals = new ArrayList<>();
    }

    /**
     * Creates a population from an initial individual list.
     *
     * @param objectiveSense objective optimization sense
     * @param individuals initial population members
     */
    public Population(ObjectiveSense objectiveSense, List<Individual<G>> individuals) {
        this.objectiveSense = Objects.requireNonNull(objectiveSense, "objectiveSense must not be null");
        this.individuals = new ArrayList<>(Objects.requireNonNull(individuals, "individuals must not be null"));
    }

    /**
     * Returns objective sense used for sorting and best/worst queries.
     *
     * @return objective sense
     */
    public ObjectiveSense objectiveSense() {
        return objectiveSense;
    }

    /**
     * Returns number of individuals currently stored in the population.
     *
     * @return population size
     */
    public int size() {
        return individuals.size();
    }

    /**
     * Adds one individual to the population.
     *
     * @param individual individual to append
     */
    public void add(Individual<G> individual) {
        individuals.add(Objects.requireNonNull(individual, "individual must not be null"));
    }

    /**
     * Adds all individuals from the provided list.
     *
     * @param newIndividuals individuals to append
     */
    public void addAll(List<Individual<G>> newIndividuals) {
        individuals.addAll(Objects.requireNonNull(newIndividuals, "newIndividuals must not be null"));
    }

    /**
     * Returns the individual at a specific index.
     *
     * @param index zero-based position
     * @return individual at index
     */
    public Individual<G> get(int index) {
        return individuals.get(index);
    }

    /**
     * Returns an unmodifiable list view of current individuals.
     *
     * @return immutable population view
     */
    public List<Individual<G>> asList() {
        return Collections.unmodifiableList(individuals);
    }

    /**
     * Removes all individuals from the population.
     */
    public void clear() {
        individuals.clear();
    }

    /**
     * Sorts individuals by scalar fitness according to objective sense.
     */
    public void sortByFitness() {
        Comparator<Individual<G>> comparator = Comparator.comparingDouble(i -> i.fitness().scalar());
        if (objectiveSense == ObjectiveSense.MAXIMIZE) {
            comparator = comparator.reversed();
        }
        individuals.sort(comparator);
    }

    /**
     * Returns the best individual according to objective sense.
     *
     * @return best individual
     */
    public Individual<G> best() {
        if (individuals.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }
        sortByFitness();
        return individuals.get(0);
    }

    /**
     * Returns the worst individual according to objective sense.
     *
     * @return worst individual
     */
    public Individual<G> worst() {
        if (individuals.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }
        sortByFitness();
        return individuals.get(individuals.size() - 1);
    }

    /**
     * Returns iterator over internal individual list.
     *
     * @return population iterator
     */
    @Override
    public Iterator<Individual<G>> iterator() {
        return individuals.iterator();
    }
}
