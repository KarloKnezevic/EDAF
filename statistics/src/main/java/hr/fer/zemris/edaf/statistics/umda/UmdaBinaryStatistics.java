package hr.fer.zemris.edaf.statistics.umda;

import hr.fer.zemris.edaf.core.api.Genotype;
import hr.fer.zemris.edaf.core.api.Population;
import hr.fer.zemris.edaf.core.api.Statistics;
import hr.fer.zemris.edaf.core.impl.SimplePopulation;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * UMDA statistics for binary genotypes.
 */
public class UmdaBinaryStatistics implements Statistics<BinaryIndividual> {

    private final Genotype<byte[]> genotype;
    private final Random random;
    private final double[] p;

    public UmdaBinaryStatistics(Genotype<byte[]> genotype, Random random) {
        this.genotype = genotype;
        this.random = random;
        this.p = new double[genotype.getLength()];
        // Initialize with 0.5
        for (int i = 0; i < p.length; i++) {
            p[i] = 0.5;
        }
    }

    @Override
    public void estimate(Population<BinaryIndividual> population) {
        for (int i = 0; i < p.length; i++) {
            int count = 0;
            for (BinaryIndividual individual : population) {
                count += individual.getGenotype()[i];
            }
            p[i] = (double) count / population.getSize();
        }
    }

    @Override
    public void update(BinaryIndividual individual, double learningRate) {
        // Not used by UMDA
    }

    @Override
    public Population<BinaryIndividual> sample(int size) {
        Population<BinaryIndividual> newPopulation = new SimplePopulation<>();
        for (int i = 0; i < size; i++) {
            byte[] newGenotype = new byte[genotype.getLength()];
            for (int j = 0; j < newGenotype.length; j++) {
                newGenotype[j] = (byte) (random.nextDouble() < p[j] ? 1 : 0);
            }
            newPopulation.add(new BinaryIndividual(newGenotype));
        }
        return newPopulation;
    }
}
