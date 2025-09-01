package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Individual;

public class TestIndividual implements Individual<Void> {
    private double fitness;

    public TestIndividual(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public Void getGenotype() {
        return null;
    }

    @Override
    public Individual<Void> copy() {
        return new TestIndividual(fitness);
    }
}
