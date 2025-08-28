package hr.fer.zemris.edaf.statistics.mimic;

import hr.fer.zemris.edaf.core.Genotype;
import hr.fer.zemris.edaf.core.Population;
import hr.fer.zemris.edaf.core.SimplePopulation;
import hr.fer.zemris.edaf.core.Statistics;
import hr.fer.zemris.edaf.genotype.binary.BinaryIndividual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MIMIC statistics for binary genotypes.
 */
public class MimicStatistics implements Statistics<BinaryIndividual> {

    private final Genotype<byte[]> genotype;
    private final Random random;
    private final int length;
    private int[] chain;
    private double[] p;
    private double[][] CPT;

    public MimicStatistics(Genotype<byte[]> genotype, Random random) {
        this.genotype = genotype;
        this.random = random;
        this.length = genotype.getLength();
        this.p = new double[length];
        this.CPT = new double[length][2];
    }

    @Override
    public void estimate(Population<BinaryIndividual> population) {
        // 1. Calculate univariate probabilities
        double[] px = new double[length];
        for (int i = 0; i < length; i++) {
            int count = 0;
            for (BinaryIndividual individual : population) {
                count += individual.getGenotype()[i];
            }
            px[i] = (double) count / population.size();
        }

        // 2. Calculate pairwise joint probabilities
        double[][][] pxy = new double[length][length][4];
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                int[] counts = new int[4];
                for (BinaryIndividual individual : population) {
                    int x = individual.getGenotype()[i];
                    int y = individual.getGenotype()[j];
                    if (x == 0 && y == 0) counts[0]++;
                    else if (x == 0 && y == 1) counts[1]++;
                    else if (x == 1 && y == 0) counts[2]++;
                    else counts[3]++;
                }
                for (int k = 0; k < 4; k++) {
                    pxy[i][j][k] = (double) counts[k] / population.size();
                }
            }
        }

        // 3. Calculate mutual information
        double[][] mi = new double[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                double mutualInformation = 0;
                double p_i = px[i];
                double p_j = px[j];
                if (p_i > 0 && p_i < 1 && p_j > 0 && p_j < 1) {
                    if (pxy[i][j][0] > 0) mutualInformation += pxy[i][j][0] * Math.log(pxy[i][j][0] / ((1 - p_i) * (1 - p_j)));
                    if (pxy[i][j][1] > 0) mutualInformation += pxy[i][j][1] * Math.log(pxy[i][j][1] / ((1 - p_i) * p_j));
                    if (pxy[i][j][2] > 0) mutualInformation += pxy[i][j][2] * Math.log(pxy[i][j][2] / (p_i * (1 - p_j)));
                    if (pxy[i][j][3] > 0) mutualInformation += pxy[i][j][3] * Math.log(pxy[i][j][3] / (p_i * p_j));
                }
                if (Double.isNaN(mutualInformation) || Double.isInfinite(mutualInformation)) {
                    mutualInformation = 0;
                }
                mi[i][j] = mi[j][i] = mutualInformation;
            }
        }
        System.out.println("MI: " + java.util.Arrays.deepToString(mi));

        // 4. Build a fixed chain
        chain = new int[length];
        for (int i = 0; i < length; i++) {
            chain[i] = i;
        }

        System.out.println("Chain: " + java.util.Arrays.toString(chain));

        // 5. Learn CPTs for the chain with Laplace smoothing
        p[chain[0]] = px[chain[0]];
        for (int i = 1; i < length; i++) {
            int current = chain[i];
            int prev = chain[i - 1];
            int countPrev0 = 0, countPrev1 = 0;
            int countBoth1 = 0, countCurrent1Prev0 = 0;
            for (BinaryIndividual individual : population) {
                if (individual.getGenotype()[prev] == 0) countPrev0++;
                else countPrev1++;

                if (individual.getGenotype()[current] == 1) {
                    if (individual.getGenotype()[prev] == 0) countCurrent1Prev0++;
                    else countBoth1++;
                }
            }
            CPT[current][0] = (double) (countCurrent1Prev0 + 1) / (countPrev0 + 2);
            CPT[current][1] = (double) (countBoth1 + 1) / (countPrev1 + 2);
        }
        System.out.println("p: " + java.util.Arrays.toString(p));
        System.out.println("CPT: " + java.util.Arrays.deepToString(CPT));
    }

    @Override
    public void update(BinaryIndividual individual, double learningRate) {
        // Not used by MIMIC
    }

    @Override
    public Population<BinaryIndividual> sample(int size) {
        Population<BinaryIndividual> newPopulation = new SimplePopulation<>();
        for (int i = 0; i < size; i++) {
            byte[] newGenotype = new byte[length];
            // Sample first variable
            newGenotype[chain[0]] = random.nextDouble() < p[chain[0]] ? (byte) 1 : (byte) 0;
            // Sample the rest of the chain
            for (int j = 1; j < length; j++) {
                int current = chain[j];
                int prev = chain[j-1];
                double prob = (newGenotype[prev] == 0) ? CPT[current][0] : CPT[current][1];
                newGenotype[current] = random.nextDouble() < prob ? (byte) 1 : (byte) 0;
            }
            newPopulation.add(new BinaryIndividual(newGenotype));
        }
        return newPopulation;
    }
}
