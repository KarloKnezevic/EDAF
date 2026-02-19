package com.knezevic.edaf.genotype.integer;

import com.knezevic.edaf.genotype.integer.crossing.OnePointCrossover;
import com.knezevic.edaf.genotype.integer.crossing.TwoPointCrossover;
import com.knezevic.edaf.genotype.integer.mutation.SimpleIntegerMutation;
import net.jqwik.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class IntegerOperatorProperties {

    private static final int MIN_BOUND = 0;
    private static final int MAX_BOUND = 100;

    @Provide
    Arbitrary<int[]> intGenotypes() {
        return Arbitraries.integers().between(2, 30).flatMap(len ->
            Arbitraries.integers().between(MIN_BOUND, MAX_BOUND)
                .array(int[].class).ofSize(len));
    }

    @Property(tries = 200)
    void onePointCrossoverPreservesLength(@ForAll("intGenotypes") int[] g1, @ForAll("intGenotypes") int[] g2) {
        int len = Math.min(g1.length, g2.length);
        int[] p1 = new int[len];
        int[] p2 = new int[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        OnePointCrossover crossover = new OnePointCrossover(new Random());
        IntegerIndividual offspring = crossover.crossover(new IntegerIndividual(p1), new IntegerIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void onePointCrossoverGenesFromParents(@ForAll("intGenotypes") int[] g1, @ForAll("intGenotypes") int[] g2) {
        int len = Math.min(g1.length, g2.length);
        int[] p1 = new int[len];
        int[] p2 = new int[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        OnePointCrossover crossover = new OnePointCrossover(new Random());
        IntegerIndividual offspring = crossover.crossover(new IntegerIndividual(p1), new IntegerIndividual(p2));

        for (int i = 0; i < len; i++) {
            assertTrue(offspring.getGenotype()[i] == p1[i] || offspring.getGenotype()[i] == p2[i],
                "Offspring gene must come from one of the parents");
        }
    }

    @Property(tries = 200)
    void twoPointCrossoverPreservesLength(@ForAll("intGenotypes") int[] g1, @ForAll("intGenotypes") int[] g2) {
        int len = Math.min(g1.length, g2.length);
        int[] p1 = new int[len];
        int[] p2 = new int[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        TwoPointCrossover crossover = new TwoPointCrossover(new Random());
        IntegerIndividual offspring = crossover.crossover(new IntegerIndividual(p1), new IntegerIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void simpleIntegerMutationPreservesLength(@ForAll("intGenotypes") int[] genotype) {
        IntegerIndividual individual = new IntegerIndividual(genotype.clone());
        SimpleIntegerMutation mutation = new SimpleIntegerMutation(new Random(), 0.5, MIN_BOUND, MAX_BOUND);

        mutation.mutate(individual);

        assertEquals(genotype.length, individual.getGenotype().length);
    }

    @Property(tries = 200)
    void simpleIntegerMutationPreservesBounds(@ForAll("intGenotypes") int[] genotype) {
        IntegerIndividual individual = new IntegerIndividual(genotype.clone());
        SimpleIntegerMutation mutation = new SimpleIntegerMutation(new Random(), 1.0, MIN_BOUND, MAX_BOUND);

        mutation.mutate(individual);

        for (int gene : individual.getGenotype()) {
            assertTrue(gene >= MIN_BOUND && gene <= MAX_BOUND,
                "Gene " + gene + " out of bounds [" + MIN_BOUND + ", " + MAX_BOUND + "]");
        }
    }
}
