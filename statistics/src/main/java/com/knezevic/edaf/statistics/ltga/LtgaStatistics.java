package com.knezevic.edaf.statistics.ltga;

import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.*;

/**
 * LTGA statistics for binary genotypes.
 */
public class LtgaStatistics implements Statistics<BinaryIndividual> {

    private final Genotype<byte[]> genotype;
    private final Random random;
    private final int length;
    private List<Set<Integer>> tree;

    public LtgaStatistics(Genotype<byte[]> genotype, Random random) {
        this.genotype = genotype;
        this.random = random;
        this.length = genotype.getLength();
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
            px[i] = (double) count / population.getSize();
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
                    pxy[i][j][k] = (double) counts[k] / population.getSize();
                }
            }
        }

        // 3. Calculate mutual information
        double[][] mi = new double[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                double mutualInformation = 0;
                if (pxy[i][j][0] > 0) mutualInformation += pxy[i][j][0] * Math.log(pxy[i][j][0] / ((1 - px[i]) * (1 - px[j])));
                if (pxy[i][j][1] > 0) mutualInformation += pxy[i][j][1] * Math.log(pxy[i][j][1] / ((1 - px[i]) * px[j]));
                if (pxy[i][j][2] > 0) mutualInformation += pxy[i][j][2] * Math.log(pxy[i][j][2] / (px[i] * (1 - px[j])));
                if (pxy[i][j][3] > 0) mutualInformation += pxy[i][j][3] * Math.log(pxy[i][j][3] / (px[i] * px[j]));
                mi[i][j] = mi[j][i] = mutualInformation;
            }
        }

        // 4. Build linkage tree using UPGMA
        List<Set<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Set<Integer> cluster = new HashSet<>();
            cluster.add(i);
            clusters.add(cluster);
        }

        tree = new ArrayList<>(clusters);

        while (clusters.size() > 1) {
            double maxMi = -1;
            int c1 = -1, c2 = -1;
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double avgMi = 0;
                    int count = 0;
                    for (int v1 : clusters.get(i)) {
                        for (int v2 : clusters.get(j)) {
                            avgMi += mi[v1][v2];
                            count++;
                        }
                    }
                    avgMi /= count;
                    if (avgMi > maxMi) {
                        maxMi = avgMi;
                        c1 = i;
                        c2 = j;
                    }
                }
            }

            Set<Integer> merged = new HashSet<>(clusters.get(c1));
            merged.addAll(clusters.get(c2));
            tree.add(merged);
            clusters.set(c1, merged);
            clusters.remove(c2);
        }
    }

    @Override
    public void update(BinaryIndividual individual, double learningRate) {
        // Not used by LTGA
    }

    @Override
    public Population<BinaryIndividual> sample(int size) {
        Population<BinaryIndividual> newPopulation = new SimplePopulation<>();
        for (int i = 0; i < size; i++) {
            byte[] newGenotype = new byte[length];
            // TODO: Implement sampling from the linkage tree
            newPopulation.add(new BinaryIndividual(newGenotype));
        }
        return newPopulation;
    }
}
