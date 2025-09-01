package com.knezevic.edaf.algorithm.cga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class cGATest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Problem<BinaryIndividual> problem = new MaxOnes();

        // 2. Create parameters
        int genotypeLength = 20;
        int n = 100;
        Random random = new Random(42);

        // 3. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(1000);

        // 4. Create a cGA algorithm instance
        Algorithm<BinaryIndividual> cga = new cGA(problem, terminationCondition, n, genotypeLength, random);

        // 5. Run the algorithm
        cga.run();

        // 6. Assert that the best individual has a fitness of 0
        assertEquals(0, cga.getBest().getFitness());

        // 7. Assert that the genotype of the best individual is all ones
        byte[] genotype = cga.getBest().getGenotype();
        for (byte b : genotype) {
            assertEquals(1, b);
        }
    }
}
