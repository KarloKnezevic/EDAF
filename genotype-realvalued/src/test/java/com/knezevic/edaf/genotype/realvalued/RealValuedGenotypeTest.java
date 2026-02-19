package com.knezevic.edaf.genotype.realvalued;

import com.knezevic.edaf.genotype.realvalued.crossing.DiscreteRecombination;
import com.knezevic.edaf.genotype.realvalued.crossing.IntermediateRecombination;
import com.knezevic.edaf.genotype.realvalued.mutation.SelfAdaptiveMutation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RealValuedGenotypeTest {

    @Test
    void testGenotypeCreation() {
        Random random = new Random(42);
        RealValuedGenotype genotype = new RealValuedGenotype(10, -5.0, 5.0, 1.0, random);
        RealValuedIndividual individual = genotype.createIndividual();

        assertEquals(10, individual.getGenotype().length);
        assertEquals(10, individual.getSigmas().length);

        for (double v : individual.getGenotype()) {
            assertTrue(v >= -5.0 && v <= 5.0);
        }
        for (double s : individual.getSigmas()) {
            assertEquals(1.0, s);
        }
    }

    @Test
    void testSelfAdaptiveMutation() {
        Random random = new Random(42);
        double[] genotype = {0.0, 0.0, 0.0};
        double[] sigmas = {1.0, 1.0, 1.0};
        RealValuedIndividual individual = new RealValuedIndividual(genotype, sigmas);

        SelfAdaptiveMutation mutation = new SelfAdaptiveMutation(random, -5.0, 5.0);
        mutation.mutate(individual);

        // Sigmas should have changed
        boolean sigmasChanged = false;
        for (double s : individual.getSigmas()) {
            if (s != 1.0) sigmasChanged = true;
            assertTrue(s > 0, "Sigma must remain positive");
        }
        assertTrue(sigmasChanged, "Sigmas should change after mutation");

        // Genotype should be within bounds
        for (double v : individual.getGenotype()) {
            assertTrue(v >= -5.0 && v <= 5.0, "Genotype value must be within bounds");
        }
    }

    @Test
    void testIntermediateRecombination() {
        double[] g1 = {1.0, 2.0, 3.0};
        double[] s1 = {0.5, 0.5, 0.5};
        double[] g2 = {3.0, 4.0, 5.0};
        double[] s2 = {1.5, 1.5, 1.5};

        RealValuedIndividual p1 = new RealValuedIndividual(g1, s1);
        RealValuedIndividual p2 = new RealValuedIndividual(g2, s2);

        IntermediateRecombination crossover = new IntermediateRecombination();
        RealValuedIndividual child = crossover.crossover(p1, p2);

        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, child.getGenotype());
        assertArrayEquals(new double[]{1.0, 1.0, 1.0}, child.getSigmas());
    }

    @Test
    void testDiscreteRecombination() {
        Random random = new Random(42);
        double[] g1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] s1 = {0.1, 0.2, 0.3, 0.4, 0.5};
        double[] g2 = {10.0, 20.0, 30.0, 40.0, 50.0};
        double[] s2 = {1.0, 2.0, 3.0, 4.0, 5.0};

        RealValuedIndividual p1 = new RealValuedIndividual(g1, s1);
        RealValuedIndividual p2 = new RealValuedIndividual(g2, s2);

        DiscreteRecombination crossover = new DiscreteRecombination(random);
        RealValuedIndividual child = crossover.crossover(p1, p2);

        assertEquals(5, child.getGenotype().length);
        for (int i = 0; i < 5; i++) {
            assertTrue(child.getGenotype()[i] == g1[i] || child.getGenotype()[i] == g2[i],
                    "Each gene must come from one parent");
        }
    }

    @Test
    void testCopy() {
        double[] genotype = {1.0, 2.0, 3.0};
        double[] sigmas = {0.5, 0.5, 0.5};
        RealValuedIndividual original = new RealValuedIndividual(genotype, sigmas);
        original.setFitness(42.0);

        RealValuedIndividual copy = original.copy();
        assertEquals(original.getFitness(), copy.getFitness());
        assertArrayEquals(original.getGenotype(), copy.getGenotype());
        assertArrayEquals(original.getSigmas(), copy.getSigmas());

        // Modifying copy should not affect original
        copy.getGenotype()[0] = 999.0;
        assertEquals(1.0, original.getGenotype()[0]);
    }
}
