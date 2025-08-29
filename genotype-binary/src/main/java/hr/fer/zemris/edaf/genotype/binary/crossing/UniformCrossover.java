package hr.fer.zemris.edaf.genotype.binary.crossing;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * Uniform crossover for binary individuals.
 */
public class UniformCrossover implements Crossover<BinaryIndividual> {

    private final Random random;

    public UniformCrossover(Random random) {
        this.random = random;
    }

    @Override
    public BinaryIndividual crossover(BinaryIndividual parent1, BinaryIndividual parent2) {
        byte[] p1 = parent1.getGenotype();
        byte[] p2 = parent2.getGenotype();
        int length = p1.length;
        byte[] offspring = new byte[length];

        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                offspring[i] = p1[i];
            } else {
                offspring[i] = p2[i];
            }
        }

        return new BinaryIndividual(offspring);
    }
}
