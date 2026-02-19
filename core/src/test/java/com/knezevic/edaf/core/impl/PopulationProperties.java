package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.OptimizationType;
import net.jqwik.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PopulationProperties {

    /** Minimal Individual for testing. */
    private static class StubIndividual implements Individual<double[]> {
        private double fitness;
        private final double[] genotype;

        StubIndividual(double fitness) {
            this.fitness = fitness;
            this.genotype = new double[]{fitness};
        }

        @Override public double getFitness() { return fitness; }
        @Override public void setFitness(double fitness) { this.fitness = fitness; }
        @Override public double[] getGenotype() { return genotype; }
        @Override public Individual<double[]> copy() {
            return new StubIndividual(fitness);
        }
    }

    @Provide
    Arbitrary<List<Double>> fitnessList() {
        return Arbitraries.doubles().between(-1000, 1000)
            .list().ofMinSize(1).ofMaxSize(50);
    }

    @Property(tries = 100)
    void sortPreservesSize(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.min);
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
        }

        int sizeBefore = pop.getSize();
        pop.sort();
        assertEquals(sizeBefore, pop.getSize());
    }

    @Property(tries = 100)
    void sortMinOrdersAscending(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.min);
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
        }

        pop.sort();

        for (int i = 1; i < pop.getSize(); i++) {
            assertTrue(pop.getIndividual(i - 1).getFitness() <= pop.getIndividual(i).getFitness(),
                "Population should be sorted ascending for min optimization");
        }
    }

    @Property(tries = 100)
    void sortMaxOrdersDescending(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.max);
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
        }

        pop.sort();

        for (int i = 1; i < pop.getSize(); i++) {
            assertTrue(pop.getIndividual(i - 1).getFitness() >= pop.getIndividual(i).getFitness(),
                "Population should be sorted descending for max optimization");
        }
    }

    @Property(tries = 100)
    void addIncrementsSize(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.min);
        for (int i = 0; i < fitnesses.size(); i++) {
            pop.add(new StubIndividual(fitnesses.get(i)));
            assertEquals(i + 1, pop.getSize());
        }
    }

    @Property(tries = 100)
    void getBestReturnsMinFitness(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.min);
        double expectedMin = Double.MAX_VALUE;
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
            expectedMin = Math.min(expectedMin, f);
        }

        assertEquals(expectedMin, pop.getBest().getFitness(), 1e-10);
    }

    @Property(tries = 100)
    void getBestReturnsMaxFitness(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.max);
        double expectedMax = -Double.MAX_VALUE;
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
            expectedMax = Math.max(expectedMax, f);
        }

        assertEquals(expectedMax, pop.getBest().getFitness(), 1e-10);
    }

    @Property(tries = 100)
    void clearEmptiesPopulation(@ForAll("fitnessList") List<Double> fitnesses) {
        SimplePopulation<StubIndividual> pop = new SimplePopulation<>(OptimizationType.min);
        for (double f : fitnesses) {
            pop.add(new StubIndividual(f));
        }

        pop.clear();
        assertEquals(0, pop.getSize());
    }
}
