/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.Objects;

/**
 * Strongly typed individual entity carrying genotype and fitness.
 *
 * @param <G> genotype value type.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class Individual<G> {

    private final G genotype;
    private final Fitness fitness;

    /**
     * Creates an immutable individual with genotype and evaluated fitness.
     *
     * @param genotype genotype value
     * @param fitness evaluated fitness
     */
    public Individual(G genotype, Fitness fitness) {
        this.genotype = Objects.requireNonNull(genotype, "genotype must not be null");
        this.fitness = Objects.requireNonNull(fitness, "fitness must not be null");
    }

    /**
     * Returns genotype value carried by this individual.
     *
     * @return genotype value
     */
    public G genotype() {
        return genotype;
    }

    /**
     * Returns fitness assigned to this individual.
     *
     * @return fitness value
     */
    public Fitness fitness() {
        return fitness;
    }

    /**
     * Returns a new individual with the same genotype and replaced fitness.
     *
     * @param newFitness fitness value for returned individual
     * @return copied individual with updated fitness
     */
    public Individual<G> withFitness(Fitness newFitness) {
        return new Individual<>(genotype, newFitness);
    }
}
