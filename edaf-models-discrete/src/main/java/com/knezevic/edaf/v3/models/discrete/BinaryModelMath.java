package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Shared statistics and sampling utilities for binary dependency models.
 */
final class BinaryModelMath {

    private BinaryModelMath() {
        // utility class
    }

    static double[] marginalOne(List<Individual<BitString>> selected, double smoothing) {
        int length = selected.getFirst().genotype().length();
        double[] marginal = new double[length];
        for (Individual<BitString> individual : selected) {
            boolean[] genes = individual.genotype().genes();
            for (int i = 0; i < length; i++) {
                marginal[i] += genes[i] ? 1.0 : 0.0;
            }
        }
        double denom = selected.size() + 2.0 * smoothing;
        for (int i = 0; i < length; i++) {
            marginal[i] = (marginal[i] + smoothing) / denom;
            marginal[i] = clampProbability(marginal[i]);
        }
        return marginal;
    }

    static double[][] mutualInformation(List<Individual<BitString>> selected, double smoothing) {
        int length = selected.getFirst().genotype().length();
        double[][] matrix = new double[length][length];
        if (selected.size() <= 1) {
            return matrix;
        }

        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                double[][] joint = new double[2][2];
                for (Individual<BitString> individual : selected) {
                    boolean[] genes = individual.genotype().genes();
                    int xi = genes[i] ? 1 : 0;
                    int xj = genes[j] ? 1 : 0;
                    joint[xi][xj] += 1.0;
                }

                double total = selected.size() + 4.0 * smoothing;
                double[] pi = new double[2];
                double[] pj = new double[2];
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < 2; xj++) {
                        joint[xi][xj] = (joint[xi][xj] + smoothing) / total;
                        pi[xi] += joint[xi][xj];
                        pj[xj] += joint[xi][xj];
                    }
                }

                double mi = 0.0;
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < 2; xj++) {
                        double pxy = joint[xi][xj];
                        double denom = pi[xi] * pj[xj];
                        if (pxy > 0.0 && denom > 0.0) {
                            mi += pxy * Math.log(pxy / denom);
                        }
                    }
                }
                matrix[i][j] = mi;
                matrix[j][i] = mi;
            }
        }
        return matrix;
    }

    static int maxEntropyIndex(double[] marginalOne) {
        int best = 0;
        double entropy = entropy(marginalOne[0]);
        for (int i = 1; i < marginalOne.length; i++) {
            double current = entropy(marginalOne[i]);
            if (current > entropy) {
                entropy = current;
                best = i;
            }
        }
        return best;
    }

    static int minEntropyIndex(double[] marginalOne) {
        int best = 0;
        double entropy = entropy(marginalOne[0]);
        for (int i = 1; i < marginalOne.length; i++) {
            double current = entropy(marginalOne[i]);
            if (current < entropy) {
                entropy = current;
                best = i;
            }
        }
        return best;
    }

    static int[] maximumSpanningTree(double[][] weights, int root) {
        int n = weights.length;
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        boolean[] visited = new boolean[n];
        visited[root] = true;

        for (int edge = 0; edge < n - 1; edge++) {
            double bestWeight = Double.NEGATIVE_INFINITY;
            int from = -1;
            int to = -1;
            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (visited[j]) {
                        continue;
                    }
                    if (weights[i][j] > bestWeight) {
                        bestWeight = weights[i][j];
                        from = i;
                        to = j;
                    }
                }
            }
            if (to < 0) {
                break;
            }
            visited[to] = true;
            parent[to] = from;
        }
        return parent;
    }

    static int[] topologicalOrderFromTree(int[] parent, int root) {
        int n = parent.length;
        List<List<Integer>> children = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            children.add(new ArrayList<>());
        }
        for (int node = 0; node < n; node++) {
            int p = parent[node];
            if (p >= 0) {
                children.get(p).add(node);
            }
        }

        int[] order = new int[n];
        int index = 0;
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int node = queue.removeFirst();
            order[index++] = node;
            for (int child : children.get(node)) {
                queue.addLast(child);
            }
        }
        while (index < n) {
            order[index] = index;
            index++;
        }
        return order;
    }

    static double[][] conditionalOneForTree(List<Individual<BitString>> selected, int[] parent, double smoothing) {
        int n = parent.length;
        double[][] conditional = new double[n][2];
        double[] parentZero = new double[n];
        double[] parentOne = new double[n];
        double[] childOneGivenParentZero = new double[n];
        double[] childOneGivenParentOne = new double[n];

        for (int i = 0; i < n; i++) {
            if (parent[i] < 0) {
                continue;
            }
            for (Individual<BitString> individual : selected) {
                boolean[] genes = individual.genotype().genes();
                int parentBit = genes[parent[i]] ? 1 : 0;
                if (parentBit == 0) {
                    parentZero[i] += 1.0;
                    if (genes[i]) {
                        childOneGivenParentZero[i] += 1.0;
                    }
                } else {
                    parentOne[i] += 1.0;
                    if (genes[i]) {
                        childOneGivenParentOne[i] += 1.0;
                    }
                }
            }

            conditional[i][0] = (childOneGivenParentZero[i] + smoothing) / (parentZero[i] + 2.0 * smoothing);
            conditional[i][1] = (childOneGivenParentOne[i] + smoothing) / (parentOne[i] + 2.0 * smoothing);
            conditional[i][0] = clampProbability(conditional[i][0]);
            conditional[i][1] = clampProbability(conditional[i][1]);
        }
        return conditional;
    }

    static BitString sampleTree(int[] order,
                                int[] parent,
                                double[] marginalOne,
                                double[][] conditionalOne,
                                RngStream rng) {
        boolean[] genes = new boolean[parent.length];
        for (int node : order) {
            int parentNode = parent[node];
            double pOne = parentNode < 0
                    ? marginalOne[node]
                    : conditionalOne[node][genes[parentNode] ? 1 : 0];
            genes[node] = rng.nextDouble() < clampProbability(pOne);
        }
        return new BitString(genes);
    }

    static int[] orderByEntropy(double[] marginalOne, boolean descending) {
        Integer[] indices = new Integer[marginalOne.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        Comparator<Integer> comparator = Comparator.comparingDouble(i -> entropy(marginalOne[i]));
        if (descending) {
            comparator = comparator.reversed();
        }
        Arrays.sort(indices, comparator);
        int[] order = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            order[i] = indices[i];
        }
        return order;
    }

    static double entropy(double pOne) {
        double p = clampProbability(pOne);
        double q = 1.0 - p;
        return -p * Math.log(p) - q * Math.log(q);
    }

    static double clampProbability(double value) {
        return Math.max(1.0e-9, Math.min(1.0 - 1.0e-9, value));
    }
}
