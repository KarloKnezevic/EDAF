package hr.fer.zemris.edaf.statistics.umda;

import hr.fer.zemris.edaf.core.api.Genotype;
import hr.fer.zemris.edaf.core.api.Population;
import hr.fer.zemris.edaf.core.api.Statistics;
import hr.fer.zemris.edaf.core.impl.SimplePopulation;
import hr.fer.zemris.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * UMDA statistics for floating-point genotypes.
 */
public class UmdaFpStatistics implements Statistics<FpIndividual> {

    private final Genotype<double[]> genotype;
    private final Random random;
    private final double[] mean;
    private final double[] stdev;

    public UmdaFpStatistics(Genotype<double[]> genotype, Random random) {
        this.genotype = genotype;
        this.random = random;
        this.mean = new double[genotype.getLength()];
        this.stdev = new double[genotype.getLength()];
    }

    @Override
    public void estimate(Population<FpIndividual> population) {
        // Calculate mean
        for (int i = 0; i < mean.length; i++) {
            double sum = 0;
            for (FpIndividual individual : population) {
                sum += individual.getGenotype()[i];
            }
            mean[i] = sum / population.getSize();
        }

        // Calculate standard deviation
        for (int i = 0; i < stdev.length; i++) {
            double sum = 0;
            for (FpIndividual individual : population) {
                sum += Math.pow(individual.getGenotype()[i] - mean[i], 2);
            }
            stdev[i] = Math.sqrt(sum / population.getSize());
        }
    }

    @Override
    public void update(FpIndividual individual, double learningRate) {
        // Not used by UMDA
    }

    @Override
    public Population<FpIndividual> sample(int size) {
        Population<FpIndividual> newPopulation = new SimplePopulation<>();
        for (int i = 0; i < size; i++) {
            double[] newGenotype = new double[genotype.getLength()];
            for (int j = 0; j < newGenotype.length; j++) {
                newGenotype[j] = random.nextGaussian() * stdev[j] + mean[j];
            }
            newPopulation.add(new FpIndividual(newGenotype));
        }
        return newPopulation;
    }
}
