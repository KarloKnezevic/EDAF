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
 */
public final class Population<G> implements Iterable<Individual<G>> {

    private final ObjectiveSense objectiveSense;
    private final List<Individual<G>> individuals;

    public Population(ObjectiveSense objectiveSense) {
        this.objectiveSense = Objects.requireNonNull(objectiveSense, "objectiveSense must not be null");
        this.individuals = new ArrayList<>();
    }

    public Population(ObjectiveSense objectiveSense, List<Individual<G>> individuals) {
        this.objectiveSense = Objects.requireNonNull(objectiveSense, "objectiveSense must not be null");
        this.individuals = new ArrayList<>(Objects.requireNonNull(individuals, "individuals must not be null"));
    }

    public ObjectiveSense objectiveSense() {
        return objectiveSense;
    }

    public int size() {
        return individuals.size();
    }

    public void add(Individual<G> individual) {
        individuals.add(Objects.requireNonNull(individual, "individual must not be null"));
    }

    public void addAll(List<Individual<G>> newIndividuals) {
        individuals.addAll(Objects.requireNonNull(newIndividuals, "newIndividuals must not be null"));
    }

    public Individual<G> get(int index) {
        return individuals.get(index);
    }

    public List<Individual<G>> asList() {
        return Collections.unmodifiableList(individuals);
    }

    public void clear() {
        individuals.clear();
    }

    public void sortByFitness() {
        Comparator<Individual<G>> comparator = Comparator.comparingDouble(i -> i.fitness().scalar());
        if (objectiveSense == ObjectiveSense.MAXIMIZE) {
            comparator = comparator.reversed();
        }
        individuals.sort(comparator);
    }

    public Individual<G> best() {
        if (individuals.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }
        sortByFitness();
        return individuals.get(0);
    }

    public Individual<G> worst() {
        if (individuals.isEmpty()) {
            throw new IllegalStateException("Population is empty");
        }
        sortByFitness();
        return individuals.get(individuals.size() - 1);
    }

    @Override
    public Iterator<Individual<G>> iterator() {
        return individuals.iterator();
    }
}
