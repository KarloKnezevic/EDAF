package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.MaxGenerations;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.selection.TournamentSelection;
import com.knezevic.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BmdaTest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Problem<BinaryIndividual> problem = new MaxOnes();

        // 2. Create a BinaryGenotype factory
        int genotypeLength = 20;
        Random random = new Random(42);
        BinaryGenotype genotype = new BinaryGenotype(genotypeLength, random);

        // 3. Create an initial Population
        int populationSize = 100;
        Population<BinaryIndividual> population = new SimplePopulation<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(new BinaryIndividual(genotype.create()));
        }

        // 4. Create a BmdaStatistics object
        Statistics<BinaryIndividual> statistics = new BmdaStatistics<>();

        // 5. Create a Selection operator
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 5);

        // 6. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(100);

        // 7. Create a Bmda algorithm instance
        int selectionSize = 50;
        Algorithm<BinaryIndividual> bmda = new Bmda<>(problem, population, selection, statistics,
                terminationCondition, selectionSize);

        // 8. Run the algorithm
        bmda.run();

        // 9. Assert that the best individual has a fitness equal to the genotype length
        assertEquals(0, bmda.getBest().getFitness());
    }
}
