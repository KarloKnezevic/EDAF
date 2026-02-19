package com.knezevic.edaf.algorithm.bmda;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.*;

/**
 * Implements the statistics component for the Bivariate Marginal Distribution Algorithm (BMDA).
 * <p>
 * Uses a Chow-Liu tree to model pairwise dependencies between variables.
 * The tree structure is learned by computing pairwise mutual information from
 * bivariate distributions and building a maximum-weight spanning tree.
 * Sampling is performed in BFS order from the tree root using conditional probabilities.
 * </p>
 *
 * @param <T> The type of individual in the population.
 */
public class BmdaStatistics<T extends Individual<byte[]>> implements Statistics<T> {

    private BivariateDistribution[][] distributions;
    private int chromosomeLength;
    private final Random random;
    private OptimizationType optimizationType = OptimizationType.min;

    // Marginal probabilities P(X_i = 1)
    private double[] marginals;

    // Chow-Liu tree structure: treeParent[i] = parent of node i (-1 for root)
    private int[] treeParent;

    // BFS traversal order for sampling
    private int[] bfsOrder;

    public BmdaStatistics() {
        this.random = new Random();
    }

    public BmdaStatistics(Random random) {
        this.random = random;
    }

    @Override
    public void estimate(Population<T> population) {
        if (population.getSize() == 0) {
            return;
        }
        optimizationType = population.getOptimizationType();

        // Assuming all individuals have the same length.
        T first = population.iterator().next();
        chromosomeLength = first.getGenotype().length;
        distributions = new BivariateDistribution[chromosomeLength][chromosomeLength];

        // Initialize the distributions.
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j] = new BivariateDistribution();
            }
        }

        // Update the distributions with the individuals from the population.
        for (T individual : population) {
            updateDistributions(individual);
        }

        // Normalize the distributions.
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j].normalize(population.getSize());
            }
        }

        // Compute marginal probabilities P(X_i = 1)
        marginals = new double[chromosomeLength];
        for (int i = 0; i < chromosomeLength; i++) {
            int count = 0;
            for (T individual : population) {
                count += individual.getGenotype()[i];
            }
            marginals[i] = (double) count / population.getSize();
        }

        // Compute pairwise mutual information
        double[][] mi = new double[chromosomeLength][chromosomeLength];
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                double mutualInfo = 0;
                double pi = marginals[i];
                double pj = marginals[j];
                for (int a = 0; a < 2; a++) {
                    for (int b = 0; b < 2; b++) {
                        double pxy = distributions[i][j].getProbability(a, b);
                        double px = (a == 1) ? pi : (1 - pi);
                        double py = (b == 1) ? pj : (1 - pj);
                        if (pxy > 0 && px > 0 && py > 0) {
                            mutualInfo += pxy * Math.log(pxy / (px * py));
                        }
                    }
                }
                if (Double.isNaN(mutualInfo) || Double.isInfinite(mutualInfo)) {
                    mutualInfo = 0;
                }
                mi[i][j] = mi[j][i] = mutualInfo;
            }
        }

        // Build maximum-weight spanning tree using Prim's algorithm
        buildChowLiuTree(mi);
    }

    /**
     * Builds a maximum-weight spanning tree (Chow-Liu tree) using Prim's algorithm.
     * The tree is rooted at node 0 and a BFS traversal order is computed for sampling.
     */
    private void buildChowLiuTree(double[][] mi) {
        treeParent = new int[chromosomeLength];
        Arrays.fill(treeParent, -1);

        boolean[] inTree = new boolean[chromosomeLength];
        double[] maxWeight = new double[chromosomeLength];
        Arrays.fill(maxWeight, Double.NEGATIVE_INFINITY);

        // Start from node 0
        maxWeight[0] = 0;

        for (int step = 0; step < chromosomeLength; step++) {
            // Find the node not yet in tree with maximum weight
            int u = -1;
            double best = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < chromosomeLength; i++) {
                if (!inTree[i] && maxWeight[i] > best) {
                    best = maxWeight[i];
                    u = i;
                }
            }
            if (u == -1) break;

            inTree[u] = true;

            // Update weights for neighbors not in tree
            for (int v = 0; v < chromosomeLength; v++) {
                if (!inTree[v] && mi[u][v] > maxWeight[v]) {
                    maxWeight[v] = mi[u][v];
                    treeParent[v] = u;
                }
            }
        }

        // Compute BFS traversal order from root (node 0)
        bfsOrder = new int[chromosomeLength];
        boolean[] visited = new boolean[chromosomeLength];
        // Build adjacency list for the tree
        List<List<Integer>> children = new ArrayList<>();
        for (int i = 0; i < chromosomeLength; i++) {
            children.add(new ArrayList<>());
        }
        for (int i = 1; i < chromosomeLength; i++) {
            if (treeParent[i] >= 0) {
                children.get(treeParent[i]).add(i);
            }
        }

        // BFS from root
        int head = 0, tail = 0;
        bfsOrder[tail++] = 0;
        visited[0] = true;
        while (head < tail) {
            int node = bfsOrder[head++];
            for (int child : children.get(node)) {
                if (!visited[child]) {
                    visited[child] = true;
                    bfsOrder[tail++] = child;
                }
            }
        }
    }

    private void updateDistributions(T individual) {
        byte[] chromosome = individual.getGenotype();
        for (int i = 0; i < chromosomeLength; i++) {
            for (int j = i + 1; j < chromosomeLength; j++) {
                distributions[i][j].update(chromosome[i], chromosome[j]);
            }
        }
    }

    @Override
    public void update(T individual, double learningRate) {
        // Not used by BMDA
    }

    @Override
    public Population<T> sample(int size) {
        Population<T> newPopulation = new SimplePopulation<>(optimizationType);
        for (int i = 0; i < size; i++) {
            byte[] chromosome = new byte[chromosomeLength];

            // Sample variables in BFS order from the Chow-Liu tree
            for (int idx = 0; idx < chromosomeLength; idx++) {
                int node = bfsOrder[idx];
                int parent = treeParent[node];

                if (parent < 0) {
                    // Root node: sample from marginal probability
                    chromosome[node] = (byte) (random.nextDouble() < marginals[node] ? 1 : 0);
                } else {
                    // Non-root: sample from conditional P(X_node | X_parent)
                    int parentVal = chromosome[parent];
                    double pConditional = getConditionalProbability(node, parent, parentVal);
                    chromosome[node] = (byte) (random.nextDouble() < pConditional ? 1 : 0);
                }
            }

            @SuppressWarnings("unchecked")
            T newIndividual = (T) new BinaryIndividual(chromosome);
            newPopulation.add(newIndividual);
        }
        return newPopulation;
    }

    /**
     * Computes P(X_node = 1 | X_parent = parentVal) from the bivariate distribution
     * with Laplace smoothing to avoid zero probabilities.
     */
    private double getConditionalProbability(int node, int parent, int parentVal) {
        // Ensure we access distributions with the lower index first
        int lo = Math.min(node, parent);
        int hi = Math.max(node, parent);
        BivariateDistribution dist = distributions[lo][hi];

        double pJoint1, pJoint0;
        if (node < parent) {
            // dist is distributions[node][parent], so P(X_node=val, X_parent=parentVal)
            pJoint1 = dist.getProbability(1, parentVal);
            pJoint0 = dist.getProbability(0, parentVal);
        } else {
            // dist is distributions[parent][node], so P(X_parent=parentVal, X_node=val)
            pJoint1 = dist.getProbability(parentVal, 1);
            pJoint0 = dist.getProbability(parentVal, 0);
        }

        double denom = pJoint0 + pJoint1;
        if (denom <= 0) {
            // Fallback to marginal if no data
            return marginals[node];
        }
        return pJoint1 / denom;
    }
}
