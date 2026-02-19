package com.knezevic.edaf.v3.core.api;

import java.util.Objects;

/**
 * Strongly typed individual entity carrying genotype and fitness.
 *
 * @param <G> genotype value type.
 */
public final class Individual<G> {

    private final G genotype;
    private final Fitness fitness;

    public Individual(G genotype, Fitness fitness) {
        this.genotype = Objects.requireNonNull(genotype, "genotype must not be null");
        this.fitness = Objects.requireNonNull(fitness, "fitness must not be null");
    }

    public G genotype() {
        return genotype;
    }

    public Fitness fitness() {
        return fitness;
    }

    public Individual<G> withFitness(Fitness newFitness) {
        return new Individual<>(genotype, newFitness);
    }
}
