package hr.fer.zemris.edaf.genotype.integer.crossing;

import hr.fer.zemris.edaf.core.Crossover;
import hr.fer.zemris.edaf.genotype.integer.IntegerIndividual;

import java.util.Random;

/**
 * One-point crossover for integer individuals.
 */
public class OnePointCrossover implements Crossover<IntegerIndividual> {

    private final Random random;

    public OnePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public IntegerIndividual crossover(IntegerIndividual parent1, IntegerIndividual parent2) {
        int[] p1 = parent1.getGenotype();
        int[] p2 = parent2.getGenotype();
        int length = p1.length;
        int[] offspring = new int[length];

        int crossoverPoint = random.nextInt(length);

        for (int i = 0; i < length; i++) {
            if (i < crossoverPoint) {
                offspring[i] = p1[i];
            } else {
                offspring[i] = p2[i];
            }
        }

        return new IntegerIndividual(offspring);
    }
}
