package com.knezevic.edaf.algorithm.umda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.statistics.distribution.BernoulliDistribution;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UmdaTest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Map<String, Object> params = new HashMap<>();
        params.put("optimizationType", OptimizationType.max);
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

        // 4. Create a BernoulliDistribution object
        Statistics<BinaryIndividual> statistics = new BernoulliDistribution(genotype, random);

        // 5. Create a Selection operator
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 5);

        // 6. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(100);

        // 7. Create an Umda algorithm instance
        int selectionSize = 50;
        Algorithm<BinaryIndividual> umda = new Umda<>(problem, population, selection, statistics,
                terminationCondition, selectionSize);

        // 8. Run the algorithm
        umda.run();

        // 9. Assert that the best individual has a fitness of genotypeLength
        assertEquals(genotypeLength, umda.getBest().getFitness());
    }
}
