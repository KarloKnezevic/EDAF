package hr.fer.zemris.edaf.genotype.fp.operators;

import hr.fer.zemris.edaf.genotype.fp.FpIndividual;
import hr.fer.zemris.edaf.genotype.fp.crossing.SimulatedBinaryCrossover;
import hr.fer.zemris.edaf.genotype.fp.mutation.PolynomialMutation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FpOperatorTest {

    @Test
    void testSimulatedBinaryCrossover() {
        Random random = new Random(42);
        SimulatedBinaryCrossover crossover = new SimulatedBinaryCrossover(random, 100.0);

        FpIndividual parent1 = new FpIndividual(new double[]{1.0, 1.0, 1.0});
        FpIndividual parent2 = new FpIndividual(new double[]{2.0, 2.0, 2.0});

        FpIndividual offspring = crossover.crossover(parent1, parent2);

        assertNotNull(offspring);
        assertEquals(parent1.getGenotype().length, offspring.getGenotype().length);
    }

    @Test
    void testPolynomialMutation() {
        Random random = new Random(42);
        double lowerBound = -5.0;
        double upperBound = 5.0;
        // A lower distribution index creates a more significant mutation.
        PolynomialMutation mutation = new PolynomialMutation(random, 1.0, 10.0, lowerBound, upperBound);

        double[] initialGenotype = {0.0, 0.0, 0.0};
        FpIndividual individual = new FpIndividual(initialGenotype.clone());

        mutation.mutate(individual);

        boolean changed = false;
        for (int i = 0; i < initialGenotype.length; i++) {
            if (Math.abs(initialGenotype[i] - individual.getGenotype()[i]) > 1e-9) {
                changed = true;
                break;
            }
        }
        assertTrue(changed, "Mutation with 100% probability should change the genotype.");

        for (double gene : individual.getGenotype()) {
            assertTrue(gene >= lowerBound && gene <= upperBound, "Mutated gene " + gene + " is out of bounds.");
        }
    }
}
