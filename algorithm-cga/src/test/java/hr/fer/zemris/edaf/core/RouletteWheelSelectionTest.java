package hr.fer.zemris.edaf.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouletteWheelSelectionTest {

    private Population<TestIndividual> population;
    private Random random;

    @BeforeEach
    void setUp() {
        population = new SimplePopulation<>();
        population.add(new TestIndividual(10)); // fitness 10
        population.add(new TestIndividual(30)); // fitness 30
        population.add(new TestIndividual(60)); // fitness 60
        random = new Random(42);
    }

    @Test
    void testSelect() {
        RouletteWheelSelection<TestIndividual> selection = new RouletteWheelSelection<>(random);
        int numSelections = 10000;
        int[] selections = new int[3];

        for (int i = 0; i < numSelections; i++) {
            Population<TestIndividual> selectedPopulation = selection.select(population, 1);
            TestIndividual selected = selectedPopulation.get(0);
            if (selected.getFitness() == 10) {
                selections[0]++;
            } else if (selected.getFitness() == 30) {
                selections[1]++;
            } else {
                selections[2]++;
            }
        }

        double totalFitness = 100.0;
        double expected1 = 10.0 / totalFitness;
        double expected2 = 30.0 / totalFitness;
        double expected3 = 60.0 / totalFitness;

        double actual1 = (double) selections[0] / numSelections;
        double actual2 = (double) selections[1] / numSelections;
        double actual3 = (double) selections[2] / numSelections;

        // Check if the selection distribution is roughly proportional to fitness
        assertEquals(expected1, actual1, 0.05);
        assertEquals(expected2, actual2, 0.05);
        assertEquals(expected3, actual3, 0.05);
    }

    // A simple Individual implementation for testing purposes
    private static class TestIndividual implements Individual<Void> {
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
}
