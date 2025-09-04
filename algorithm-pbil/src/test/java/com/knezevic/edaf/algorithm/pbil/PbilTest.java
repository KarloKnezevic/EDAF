package com.knezevic.edaf.algorithm.pbil;

import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import com.knezevic.edaf.statistics.distribution.NormalDistribution;
import com.knezevic.edaf.testing.problems.Sphere;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PbilTest {

    @Test
    void testSphere() {
        // 1. Create a Sphere problem
        Map<String, Object> params = new HashMap<>();
        params.put("optimizationType", OptimizationType.MINIMIZE);
        Problem<FpIndividual> problem = new Sphere(params);

        // 2. Create parameters
        int genotypeLength = 10;
        int populationSize = 100;
        double learningRate = 0.1;
        Random random = new Random(42);

        // 3. Create a TerminationCondition
        TerminationCondition<FpIndividual> terminationCondition = new MaxGenerations<>(1000);

        // 4. Create a Statistics object
        Statistics<FpIndividual> statistics = new NormalDistribution(genotypeLength, random);

        // 5. Create a Pbil algorithm instance
        Algorithm<FpIndividual> pbil = new Pbil<>(problem, statistics, terminationCondition, populationSize, learningRate);

        // 6. Run the algorithm
        pbil.run();

        // 7. Assert that the fitness of the best individual is close to 0
        assertTrue(pbil.getBest().getFitness() < 1e-3);
    }
}
