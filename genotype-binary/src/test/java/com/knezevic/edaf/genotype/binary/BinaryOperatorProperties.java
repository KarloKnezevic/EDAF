package com.knezevic.edaf.genotype.binary;

import com.knezevic.edaf.genotype.binary.crossing.NPointCrossover;
import com.knezevic.edaf.genotype.binary.crossing.OnePointCrossover;
import com.knezevic.edaf.genotype.binary.crossing.UniformCrossover;
import com.knezevic.edaf.genotype.binary.mutation.CompleteMixingMutation;
import com.knezevic.edaf.genotype.binary.mutation.SimpleMutation;
import net.jqwik.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BinaryOperatorProperties {

    @Provide
    Arbitrary<byte[]> binaryGenotypes() {
        return Arbitraries.integers().between(2, 50).flatMap(len ->
            Arbitraries.bytes().between((byte) 0, (byte) 1).array(byte[].class).ofSize(len));
    }

    @Property(tries = 200)
    void uniformCrossoverPreservesLength(@ForAll("binaryGenotypes") byte[] g1, @ForAll("binaryGenotypes") byte[] g2) {
        int len = Math.min(g1.length, g2.length);
        byte[] p1 = new byte[len];
        byte[] p2 = new byte[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        UniformCrossover crossover = new UniformCrossover(new Random());
        BinaryIndividual offspring = crossover.crossover(new BinaryIndividual(p1), new BinaryIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void uniformCrossoverBitsFromParents(@ForAll("binaryGenotypes") byte[] g1, @ForAll("binaryGenotypes") byte[] g2) {
        int len = Math.min(g1.length, g2.length);
        byte[] p1 = new byte[len];
        byte[] p2 = new byte[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        UniformCrossover crossover = new UniformCrossover(new Random());
        BinaryIndividual offspring = crossover.crossover(new BinaryIndividual(p1), new BinaryIndividual(p2));

        for (int i = 0; i < len; i++) {
            assertTrue(offspring.getGenotype()[i] == p1[i] || offspring.getGenotype()[i] == p2[i],
                "Offspring gene must come from one of the parents");
        }
    }

    @Property(tries = 200)
    void onePointCrossoverPreservesLength(@ForAll("binaryGenotypes") byte[] g1, @ForAll("binaryGenotypes") byte[] g2) {
        int len = Math.min(g1.length, g2.length);
        byte[] p1 = new byte[len];
        byte[] p2 = new byte[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        OnePointCrossover crossover = new OnePointCrossover(new Random());
        BinaryIndividual offspring = crossover.crossover(new BinaryIndividual(p1), new BinaryIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void nPointCrossoverPreservesLength(@ForAll("binaryGenotypes") byte[] g1, @ForAll("binaryGenotypes") byte[] g2) {
        int len = Math.min(g1.length, g2.length);
        byte[] p1 = new byte[len];
        byte[] p2 = new byte[len];
        System.arraycopy(g1, 0, p1, 0, len);
        System.arraycopy(g2, 0, p2, 0, len);

        NPointCrossover crossover = new NPointCrossover(new Random(), 3);
        BinaryIndividual offspring = crossover.crossover(new BinaryIndividual(p1), new BinaryIndividual(p2));

        assertEquals(len, offspring.getGenotype().length);
    }

    @Property(tries = 200)
    void simpleMutationPreservesBinaryDomain(@ForAll("binaryGenotypes") byte[] genotype) {
        BinaryIndividual individual = new BinaryIndividual(genotype.clone());
        SimpleMutation mutation = new SimpleMutation(new Random(), 0.5);

        mutation.mutate(individual);

        assertEquals(genotype.length, individual.getGenotype().length);
        for (byte b : individual.getGenotype()) {
            assertTrue(b == 0 || b == 1, "Bit must be 0 or 1, got: " + b);
        }
    }

    @Property(tries = 200)
    void completeMixingMutationPreservesBinaryDomain(@ForAll("binaryGenotypes") byte[] genotype) {
        BinaryIndividual individual = new BinaryIndividual(genotype.clone());
        CompleteMixingMutation mutation = new CompleteMixingMutation(new Random());

        mutation.mutate(individual);

        assertEquals(genotype.length, individual.getGenotype().length);
        for (byte b : individual.getGenotype()) {
            assertTrue(b == 0 || b == 1, "Bit must be 0 or 1, got: " + b);
        }
    }
}
