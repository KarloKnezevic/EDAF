package com.knezevic.edaf.algorithm.mimic;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.statistics.mimic.MimicStatistics;
import com.knezevic.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MIMICTest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Map<String, Object> params = new HashMap<>();
        params.put("optimizationType", OptimizationType.max);
        Problem<BinaryIndividual> problem = new MaxOnes(params);

        // 2. Create a BinaryGenotype factory
        int genotypeLength = 4;
        Random random = new Random(42);
        BinaryGenotype genotype = new BinaryGenotype(genotypeLength, random);

        // 3. Create an initial Population
        int populationSize = 20;
        Population<BinaryIndividual> population = new SimplePopulation<>(problem.getOptimizationType());
        for (int i = 0; i < populationSize; i++) {
            population.add(new BinaryIndividual(genotype.create()));
        }

        // 4. Create a MimicStatistics object
        Statistics<BinaryIndividual> statistics = new MimicStatistics(genotype, random);

        // 5. Create a Selection operator
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 5);

        // 6. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(50);

        // 7. Create a MIMIC algorithm instance
        int selectionSize = 10;
        Algorithm<BinaryIndividual> mimic = new MIMIC(problem, population, selection, statistics,
                terminationCondition, selectionSize);

        // 8. Run the algorithm
        mimic.run();

        // 9. Assert that the best individual has a high fitness
        assertTrue(mimic.getBest().getFitness() >= 3);
    }
}
