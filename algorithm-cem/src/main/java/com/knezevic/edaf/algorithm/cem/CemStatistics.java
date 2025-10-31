package com.knezevic.edaf.algorithm.cem;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Random;

/**
 * Statistics component for the Cross-Entropy Method (CEM).
 * 
 * Supports both binary (Bernoulli) and continuous (Gaussian) distributions.
 * Updates distribution parameters based on elite solutions to minimize cross-entropy.
 */
public class CemStatistics implements Statistics<Individual> {

    private final int length;
    private final Random random;
    private final boolean isBinary;
    
    // For binary problems: probabilities
    private double[] probabilities;
    
    // For continuous problems: means and standard deviations
    private double[] means;
    private double[] stdDevs;

    public CemStatistics(int length, Random random, boolean isBinary) {
        this.length = length;
        this.random = random;
        this.isBinary = isBinary;
        
        if (isBinary) {
            this.probabilities = new double[length];
            // Initialize with uniform distribution
            for (int i = 0; i < length; i++) {
                probabilities[i] = 0.5;
            }
        } else {
            this.means = new double[length];
            this.stdDevs = new double[length];
            // Initialize with zero mean and unit variance
            for (int i = 0; i < length; i++) {
                means[i] = 0.0;
                stdDevs[i] = 1.0;
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void estimate(Population<Individual> population) {
        if (population.getSize() == 0) {
            return;
        }
        
        if (isBinary) {
            estimateBinary(population);
        } else {
            estimateContinuous(population);
        }
    }

    @SuppressWarnings("unchecked")
    private void estimateBinary(Population<Individual> population) {
        // Update probabilities based on elite solutions
        for (int i = 0; i < length; i++) {
            int count = 0;
            int total = 0;
            for (Individual individual : population) {
                if (individual instanceof BinaryIndividual bi) {
                    count += bi.getGenotype()[i];
                    total++;
                }
            }
            probabilities[i] = total > 0 ? (double) count / total : 0.5;
            // Clip to avoid degenerate distributions
            probabilities[i] = Math.max(0.05, Math.min(0.95, probabilities[i]));
        }
    }

    @SuppressWarnings("unchecked")
    private void estimateContinuous(Population<Individual> population) {
        // Calculate sample mean and standard deviation from elite solutions
        double[] sum = new double[length];
        double[] sumSq = new double[length];
        int count = 0;
        
        for (Individual individual : population) {
            if (individual instanceof FpIndividual fp) {
                double[] genotype = fp.getGenotype();
                for (int i = 0; i < length; i++) {
                    sum[i] += genotype[i];
                    sumSq[i] += genotype[i] * genotype[i];
                }
                count++;
            }
        }
        
        if (count > 0) {
            for (int i = 0; i < length; i++) {
                means[i] = sum[i] / count;
                double variance = (sumSq[i] / count) - (means[i] * means[i]);
                stdDevs[i] = Math.sqrt(Math.max(0.01, variance)); // Minimum std dev to avoid collapse
            }
        }
    }

    @Override
    public void update(Individual individual, double learningRate) {
        // For CEM, we update based on elite population in estimate()
        // This method is called with elite solutions via estimate()
        if (isBinary) {
            if (individual instanceof BinaryIndividual bi) {
                byte[] genotype = bi.getGenotype();
                for (int i = 0; i < length; i++) {
                    probabilities[i] = (1 - learningRate) * probabilities[i] + learningRate * genotype[i];
                    probabilities[i] = Math.max(0.05, Math.min(0.95, probabilities[i]));
                }
            }
        } else {
            if (individual instanceof FpIndividual fp) {
                double[] genotype = fp.getGenotype();
                for (int i = 0; i < length; i++) {
                    means[i] = (1 - learningRate) * means[i] + learningRate * genotype[i];
                    double diff = Math.abs(genotype[i] - means[i]);
                    stdDevs[i] = (1 - learningRate) * stdDevs[i] + learningRate * diff;
                    stdDevs[i] = Math.max(0.01, stdDevs[i]);
                }
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Population<Individual> sample(int size) {
        Population<Individual> newPopulation = new SimplePopulation<>(OptimizationType.min);
        
        if (isBinary) {
            for (int i = 0; i < size; i++) {
                byte[] genotype = new byte[length];
                for (int j = 0; j < length; j++) {
                    genotype[j] = (byte) (random.nextDouble() < probabilities[j] ? 1 : 0);
                }
                newPopulation.add(new BinaryIndividual(genotype));
            }
        } else {
            for (int i = 0; i < size; i++) {
                double[] genotype = new double[length];
                for (int j = 0; j < length; j++) {
                    genotype[j] = random.nextGaussian() * stdDevs[j] + means[j];
                }
                newPopulation.add(new FpIndividual(genotype));
            }
        }
        
        return newPopulation;
    }
    
    public boolean isBinary() {
        return isBinary;
    }
    
    public double[] getProbabilities() {
        return probabilities != null ? probabilities.clone() : null;
    }
    
    public double[] getMeans() {
        return means != null ? means.clone() : null;
    }
    
    public double[] getStdDevs() {
        return stdDevs != null ? stdDevs.clone() : null;
    }
}

