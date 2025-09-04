package com.knezevic.edaf.algorithm.ltga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.genotype.binary.mutation.SimpleMutation;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LTGATest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Map<String, Object> params = new HashMap<>();
        params.put("optimizationType", OptimizationType.MAXIMIZE);
        Problem<BinaryIndividual> problem = new MaxOnes(params);

        // 2. Create a BinaryGenotype factory
        int genotypeLength = 20;
        Random random = new Random(42);
        BinaryGenotype genotype = new BinaryGenotype(genotypeLength, random);

        // 3. Create an initial Population
        int populationSize = 100;
        Population<BinaryIndividual> population = new SimplePopulation<>(problem.getOptimizationType());
        for (int i = 0; i < populationSize; i++) {
            population.add(new BinaryIndividual(genotype.create()));
        }

        // 4. Create Mutation operator
        Mutation<BinaryIndividual> mutation = new SimpleMutation(random, 0.05);

        // 5. Create a Selection operator
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 5);

        // 6. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(1000);

        // 7. Create an LTGA algorithm instance
        Algorithm<BinaryIndividual> ltga = new LTGA(problem, population, selection, mutation,
                terminationCondition, genotypeLength, random);

        // 8. Run the algorithm
        ltga.run();

        // 9. Assert that the best individual has a high fitness
        assertTrue(ltga.getBest().getFitness() >= 15);
    }
}
