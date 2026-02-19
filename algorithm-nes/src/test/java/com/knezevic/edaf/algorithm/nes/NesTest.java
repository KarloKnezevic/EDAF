package com.knezevic.edaf.algorithm.nes;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import com.knezevic.edaf.testing.problems.Sphere;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests NES convergence on standard optimization benchmarks.
 */
class NesTest {

    @Test
    void nesConvergesOnSphere() {
        int dimension = 10;
        int populationSize = 50;
        int maxGenerations = 500;
        Random random = new Random(12345);

        Sphere problem = new Sphere(Map.of("optimizationType", OptimizationType.min));

        TerminationCondition<FpIndividual> termination = algorithm ->
            algorithm.getGeneration() >= maxGenerations;

        double etaMu = 1.0;
        double etaSigma = (3.0 + Math.log(dimension)) / (5.0 * Math.sqrt(dimension));
        NesStatistics nesStats = new NesStatistics(dimension, random, etaMu, etaSigma);

        NES nes = new NES(problem, termination, nesStats, populationSize);
        nes.run();

        FpIndividual best = nes.getBest();
        // Sphere minimum is 0.0; NES should get close with 500 generations
        assertTrue(best.getFitness() < 1.0,
            "NES should converge near optimum on Sphere(10), got fitness: " + best.getFitness());
    }

    @Test
    void nesImprovesOverGenerations() {
        int dimension = 5;
        int populationSize = 30;
        int maxGenerations = 100;
        Random random = new Random(42);

        Sphere problem = new Sphere(Map.of("optimizationType", OptimizationType.min));

        TerminationCondition<FpIndividual> termination = algorithm ->
            algorithm.getGeneration() >= maxGenerations;

        double etaMu = 1.0;
        double etaSigma = (3.0 + Math.log(dimension)) / (5.0 * Math.sqrt(dimension));
        NesStatistics nesStats = new NesStatistics(dimension, random, etaMu, etaSigma);

        NES nes = new NES(problem, termination, nesStats, populationSize);
        nes.run();

        // After 100 generations on Sphere(5), fitness should be significantly improved
        FpIndividual best = nes.getBest();
        assertTrue(best.getFitness() < 5.0,
            "NES should improve fitness on Sphere(5), got: " + best.getFitness());
        assertTrue(nes.getGeneration() == maxGenerations,
            "NES should run for exactly " + maxGenerations + " generations");
    }
}
