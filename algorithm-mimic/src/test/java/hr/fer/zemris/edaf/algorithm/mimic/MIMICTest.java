package hr.fer.zemris.edaf.algorithm.mimic;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryGenotype;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;
import hr.fer.zemris.edaf.statistics.mimic.MimicStatistics;
import hr.fer.zemris.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MIMICTest {

    @Test
    void testMaxOnes() {
        // 1. Create a MaxOnes problem
        Problem<BinaryIndividual> problem = new MaxOnes();

        // 2. Create a BinaryGenotype factory
        int genotypeLength = 4;
        Random random = new Random(42);
        BinaryGenotype genotype = new BinaryGenotype(genotypeLength, random);

        // 3. Create an initial Population
        int populationSize = 20;
        Population<BinaryIndividual> population = new SimplePopulation<>();
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

        // 9. Assert that the best individual has a fitness of 0
        assertEquals(0, mimic.getBest().getFitness());
    }
}
