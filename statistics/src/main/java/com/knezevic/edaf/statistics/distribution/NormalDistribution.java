package com.knezevic.edaf.statistics.distribution;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

public class NormalDistribution implements Statistics<FpIndividual> {

    private final int length;
    private final Random random;
    private final double[] means;
    private final double[] stdDevs;

    public NormalDistribution(int length, Random random) {
        this.length = length;
        this.random = random;
        this.means = new double[length];
        this.stdDevs = new double[length];
        for (int i = 0; i < length; i++) {
            means[i] = 0;
            stdDevs[i] = 1;
        }
    }

    @Override
    public void estimate(Population<FpIndividual> population) {
        // Not needed for PBIL
    }

    @Override
    public Population<FpIndividual> sample(int size) {
        Population<FpIndividual> newPopulation = new SimplePopulation<>(OptimizationType.min);
        for (int i = 0; i < size; i++) {
            double[] genotype = new double[length];
            for (int j = 0; j < length; j++) {
                genotype[j] = random.nextGaussian() * stdDevs[j] + means[j];
            }
            newPopulation.add(new FpIndividual(genotype));
        }
        return newPopulation;
    }

    @Override
    public void update(FpIndividual best, double learningRate) {
        double[] genotype = best.getGenotype();
        for (int i = 0; i < length; i++) {
            means[i] = means[i] * (1 - learningRate) + genotype[i] * learningRate;
            stdDevs[i] = stdDevs[i] * (1 - learningRate) + Math.abs(genotype[i] - means[i]) * learningRate;
        }
    }
}
