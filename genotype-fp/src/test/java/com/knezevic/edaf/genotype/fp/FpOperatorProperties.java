package com.knezevic.edaf.genotype.fp;

import com.knezevic.edaf.genotype.fp.crossing.DiscreteRecombination;
import com.knezevic.edaf.genotype.fp.crossing.SimpleArithmeticRecombination;
import com.knezevic.edaf.genotype.fp.crossing.SimulatedBinaryCrossover;
import com.knezevic.edaf.genotype.fp.crossing.WholeArithmeticRecombination;
import com.knezevic.edaf.genotype.fp.mutation.PolynomialMutation;
import net.jqwik.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FpOperatorProperties {

    private static final double L_BOUND = -10.0;
    private static final double U_BOUND = 10.0;

    @Provide
    Arbitrary<double[]> fpGenotypes() {
        return Arbitraries.integers().between(2, 20).flatMap(len ->
            Arbitraries.doubles().between(L_BOUND, U_BOUND)
                .array(double[].class).ofSize(len));
    }

    @Property(tries = 200)
    void sbxCrossoverPreservesLength(@ForAll("fpGenotypes") double[] g1, @ForAll("fpGenotypes") double[] g2) {
        int len = Math.min(g1.length, g2.length);
        double[] p1 = new double[len];
        double[] p2 = new double[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        SimulatedBinaryCrossover crossover = new SimulatedBinaryCrossover(new Random(), 20.0);
        FpIndividual offspring = crossover.crossover(new FpIndividual(p1), new FpIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void discreteRecombinationPreservesLength(@ForAll("fpGenotypes") double[] g1, @ForAll("fpGenotypes") double[] g2) {
        int len = Math.min(g1.length, g2.length);
        double[] p1 = new double[len];
        double[] p2 = new double[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        DiscreteRecombination crossover = new DiscreteRecombination(new Random());
        FpIndividual offspring = crossover.crossover(new FpIndividual(p1), new FpIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
        for (int i = 0; i < len; i++) {
            assertTrue(offspring.getGenotype()[i] == p1[i] || offspring.getGenotype()[i] == p2[i],
                "Offspring gene must come from one of the parents");
        }
    }

    @Property(tries = 200)
    void simpleArithmeticRecombinationPreservesLength(@ForAll("fpGenotypes") double[] g1, @ForAll("fpGenotypes") double[] g2) {
        int len = Math.min(g1.length, g2.length);
        double[] p1 = new double[len];
        double[] p2 = new double[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        SimpleArithmeticRecombination crossover = new SimpleArithmeticRecombination(new Random(), 0.5);
        FpIndividual offspring = crossover.crossover(new FpIndividual(p1), new FpIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void wholeArithmeticRecombinationPreservesLength(@ForAll("fpGenotypes") double[] g1, @ForAll("fpGenotypes") double[] g2) {
        int len = Math.min(g1.length, g2.length);
        double[] p1 = new double[len];
        double[] p2 = new double[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        WholeArithmeticRecombination crossover = new WholeArithmeticRecombination(0.5);
        FpIndividual offspring = crossover.crossover(new FpIndividual(p1), new FpIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void polynomialMutationPreservesBounds(@ForAll("fpGenotypes") double[] genotype) {
        FpIndividual individual = new FpIndividual(genotype.clone());
        PolynomialMutation mutation = new PolynomialMutation(new Random(), 1.0, 20.0, L_BOUND, U_BOUND);

        mutation.mutate(individual);

        assertEquals(genotype.length, individual.getGenotype().length);
        for (double gene : individual.getGenotype()) {
            assertTrue(gene >= L_BOUND && gene <= U_BOUND,
                "Gene " + gene + " out of bounds [" + L_BOUND + ", " + U_BOUND + "]");
        }
    }

    @Property(tries = 200)
    void polynomialMutationPreservesLength(@ForAll("fpGenotypes") double[] genotype) {
        FpIndividual individual = new FpIndividual(genotype.clone());
        PolynomialMutation mutation = new PolynomialMutation(new Random(), 0.3, 20.0, L_BOUND, U_BOUND);

        mutation.mutate(individual);

        assertEquals(genotype.length, individual.getGenotype().length);
    }
}
