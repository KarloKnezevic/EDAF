package com.knezevic.edaf.algorithm.gga;

import com.knezevic.edaf.core.Individual;
import com.knezevic.edaf.core.Population;
import com.knezevic.edaf.core.Problem;
import com.knezevic.edaf.core.impl.TournamentSelection;
import com.knezevic.edaf.genotype.integer.IntegerIndividual;
import com.knezevic.edaf.genotype.integer.IntUniformCrossover;
import com.knezevic.edaf.genotype.integer.IntUniformMutation;
import com.knezevic.edaf.testing.IntTarget;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class gGATest {

    @Test
    void testGGASolvesIntTarget() {
        final int POP_SIZE = 10;
        final int NUM_EVALS = 1000;
        final int TOURNAMENT_SIZE = 2;
        final double CROSSOVER_PROB = 0.8;
        final double MUTATION_PROB = 0.1;
        final int GENE_SIZE = 10;

        Random random = new Random();

        // Problem definition
        Problem<IntIndividual> problem = new IntTarget(GENE_SIZE);

        // Algorithm configuration
        gGA<IntIndividual> gga = new gGA<>();
        gga.setPopulationSize(POP_SIZE);
        gga.setMaxEvals(NUM_EVALS);
        gga.setSelection(new TournamentSelection<>(TOURNAMENT_SIZE, random));
        gga.setCrossover(new IntUniformCrossover(CROSSOVER_PROB, random));
        gga.setMutation(new IntUniformMutation(MUTATION_PROB, random));
        gga.setProblem(problem);

        // Run the algorithm
        Population<IntIndividual> finalPopulation = gga.run();

        // Get the best individual
        Individual<Integer> best = finalPopulation.getBest();

        // The best individual should have a fitness of 0 (or very close to it)
        assertEquals(0.0, best.getFitness(), 1e-9, "The best individual should have a fitness of 0.");

        // All genes in the best individual should be equal to the target value (7)
        for (int gene : best.getGenes()) {
            assertEquals(7, gene, "Each gene in the best individual should be 7.");
        }
    }
}
