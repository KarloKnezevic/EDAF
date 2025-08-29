package hr.fer.zemris.edaf.algorithm.ega;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryGenotype;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;
import hr.fer.zemris.edaf.genotype.binary.crossing.OnePointCrossover;
import hr.fer.zemris.edaf.genotype.binary.mutation.SimpleMutation;
import hr.fer.zemris.edaf.testing.problems.MaxOnes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class eGATest {

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

        // 4. Create Crossover and Mutation operators
        Crossover<BinaryIndividual> crossover = new OnePointCrossover(random);
        Mutation<BinaryIndividual> mutation = new SimpleMutation(random, 0.05);

        // 5. Create a Selection operator
        Selection<BinaryIndividual> selection = new TournamentSelection<>(random, 5);

        // 6. Create a TerminationCondition
        TerminationCondition<BinaryIndividual> terminationCondition = new MaxGenerations<>(1000);

        // 7. Create an eGA algorithm instance
        Algorithm<BinaryIndividual> ega = new eGA<>(problem, population, selection, crossover, mutation,
                terminationCondition);

        // 8. Run the algorithm
        ega.run();

        // 9. Assert that the best individual has a fitness of 0
        assertEquals(0, ega.getBest().getFitness());
    }
}
