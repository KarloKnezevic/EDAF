package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hierarchical BOA-style sparse Bayesian-network model for bitstrings.
 *
 * <p>Structure learning uses pairwise mutual information and variable-entropy ordering.
 * Each variable can have at most one parent, yielding a tractable conditional model:
 * <pre>
 *   p(x) = product_i p(x_i | x_parent(i))
 * </pre>
 * where root variables use marginals {@code p(x_i)}.</p>
 */
public final class HierarchicalBoaModel implements Model<BitString> {

    private final double smoothing;
    private final double minMutualInformation;
    private final double learningRate;

    private int[] order;
    private int[] parent;
    private double[] marginalOne;
    private double[][] conditionalOne;
    private double meanMutualInformation;
    private int edgeCount;

    public HierarchicalBoaModel(double smoothing, double minMutualInformation, double learningRate) {
        this.smoothing = Math.max(1e-9, smoothing);
        this.minMutualInformation = Math.max(0.0, minMutualInformation);
        this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
    }

    @Override
    public String name() {
        return "hboa-network";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        int dim = selected.getFirst().genotype().length();
        boolean[][] data = toMatrix(selected, dim);

        double[] estimatedMarginal = estimateMarginals(data, dim);
        double[][] mutualInformation = estimateMutualInformation(data, dim);
        int[] estimatedOrder = entropyOrder(estimatedMarginal);
        Structure structure = estimateStructure(estimatedOrder, mutualInformation);
        double[][] estimatedConditional = estimateConditionals(data, dim, structure.parent());

        if (marginalOne != null && marginalOne.length == dim) {
            blendInPlace(estimatedMarginal, marginalOne, learningRate);
        }
        if (conditionalOne != null && conditionalOne.length == dim) {
            blendInPlace(estimatedConditional, conditionalOne, learningRate);
        }

        this.order = estimatedOrder;
        this.parent = structure.parent();
        this.marginalOne = estimatedMarginal;
        this.conditionalOne = estimatedConditional;
        this.edgeCount = structure.edgeCount();
        this.meanMutualInformation = structure.meanMutualInformation();
    }

    @Override
    public List<BitString> sample(int count,
                                  Representation<BitString> representation,
                                  Problem<BitString> problem,
                                  ConstraintHandling<BitString> constraintHandling,
                                  RngStream rng) {
        if (order == null || parent == null || marginalOne == null || conditionalOne == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<BitString> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            boolean[] genes = new boolean[marginalOne.length];
            for (int index : order) {
                int parentIndex = parent[index];
                double pOne = parentIndex < 0
                        ? marginalOne[index]
                        : conditionalOne[index][genes[parentIndex] ? 1 : 0];
                genes[index] = rng.nextDouble() < pOne;
            }
            samples.add(constraintHandling.enforce(new BitString(genes), representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (marginalOne == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("hboa_edge_count", (double) edgeCount);
        values.put("hboa_mean_mutual_information", meanMutualInformation);
        values.put("hboa_entropy", conditionalEntropy());
        return new ModelDiagnostics(values);
    }

    public int[] order() {
        return order == null ? new int[0] : Arrays.copyOf(order, order.length);
    }

    public int[] parent() {
        return parent == null ? new int[0] : Arrays.copyOf(parent, parent.length);
    }

    public double[] marginalOne() {
        return marginalOne == null ? new double[0] : Arrays.copyOf(marginalOne, marginalOne.length);
    }

    public double[][] conditionalOne() {
        if (conditionalOne == null) {
            return new double[0][0];
        }
        double[][] copy = new double[conditionalOne.length][];
        for (int i = 0; i < conditionalOne.length; i++) {
            copy[i] = Arrays.copyOf(conditionalOne[i], conditionalOne[i].length);
        }
        return copy;
    }

    /**
     * Restores model state from checkpoint payload.
     */
    public void restore(int[] order, int[] parent, double[] marginalOne, double[][] conditionalOne) {
        if (order == null || parent == null || marginalOne == null || conditionalOne == null) {
            throw new IllegalArgumentException("HierarchicalBoaModel restore payload must be non-null");
        }
        if (order.length != parent.length || order.length != marginalOne.length || order.length != conditionalOne.length) {
            throw new IllegalArgumentException("HierarchicalBoaModel restore payload dimensions must match");
        }
        this.order = Arrays.copyOf(order, order.length);
        this.parent = Arrays.copyOf(parent, parent.length);
        this.marginalOne = Arrays.copyOf(marginalOne, marginalOne.length);
        this.conditionalOne = new double[conditionalOne.length][];
        for (int i = 0; i < conditionalOne.length; i++) {
            this.conditionalOne[i] = Arrays.copyOf(conditionalOne[i], conditionalOne[i].length);
        }
        this.edgeCount = (int) Arrays.stream(this.parent).filter(p -> p >= 0).count();
    }

    private double conditionalEntropy() {
        double entropy = 0.0;
        for (int i = 0; i < marginalOne.length; i++) {
            if (parent[i] < 0) {
                entropy += binaryEntropy(marginalOne[i]);
            } else {
                double pParent = marginalOne[parent[i]];
                entropy += (1.0 - pParent) * binaryEntropy(conditionalOne[i][0])
                        + pParent * binaryEntropy(conditionalOne[i][1]);
            }
        }
        return entropy;
    }

    private boolean[][] toMatrix(List<Individual<BitString>> selected, int dim) {
        boolean[][] data = new boolean[selected.size()][dim];
        for (int r = 0; r < selected.size(); r++) {
            boolean[] genes = selected.get(r).genotype().genes();
            System.arraycopy(genes, 0, data[r], 0, dim);
        }
        return data;
    }

    private double[] estimateMarginals(boolean[][] data, int dim) {
        double[] marginals = new double[dim];
        for (int i = 0; i < dim; i++) {
            int ones = 0;
            for (boolean[] row : data) {
                if (row[i]) {
                    ones++;
                }
            }
            marginals[i] = clamp((ones + smoothing) / (data.length + 2.0 * smoothing), 1e-9, 1.0 - 1e-9);
        }
        return marginals;
    }

    private double[][] estimateMutualInformation(boolean[][] data, int dim) {
        double[][] matrix = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                int c00 = 0;
                int c01 = 0;
                int c10 = 0;
                int c11 = 0;
                for (boolean[] row : data) {
                    boolean xi = row[i];
                    boolean xj = row[j];
                    if (!xi && !xj) {
                        c00++;
                    } else if (!xi) {
                        c01++;
                    } else if (!xj) {
                        c10++;
                    } else {
                        c11++;
                    }
                }
                double total = data.length + 4.0 * smoothing;
                double p00 = (c00 + smoothing) / total;
                double p01 = (c01 + smoothing) / total;
                double p10 = (c10 + smoothing) / total;
                double p11 = (c11 + smoothing) / total;

                double px0 = p00 + p01;
                double px1 = p10 + p11;
                double py0 = p00 + p10;
                double py1 = p01 + p11;

                double mi = 0.0;
                mi += p00 * log2Safe(p00 / (px0 * py0));
                mi += p01 * log2Safe(p01 / (px0 * py1));
                mi += p10 * log2Safe(p10 / (px1 * py0));
                mi += p11 * log2Safe(p11 / (px1 * py1));

                matrix[i][j] = Math.max(0.0, mi);
                matrix[j][i] = matrix[i][j];
            }
        }
        return matrix;
    }

    private int[] entropyOrder(double[] marginals) {
        Integer[] indices = new Integer[marginals.length];
        for (int i = 0; i < marginals.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (a, b) -> Double.compare(binaryEntropy(marginals[b]), binaryEntropy(marginals[a])));
        int[] order = new int[marginals.length];
        for (int i = 0; i < marginals.length; i++) {
            order[i] = indices[i];
        }
        return order;
    }

    private Structure estimateStructure(int[] order, double[][] mi) {
        int[] parent = new int[order.length];
        Arrays.fill(parent, -1);

        int edges = 0;
        double totalMi = 0.0;
        for (int pos = 1; pos < order.length; pos++) {
            int child = order[pos];
            int bestParent = -1;
            double bestMi = minMutualInformation;
            for (int q = 0; q < pos; q++) {
                int candidate = order[q];
                double candidateMi = mi[child][candidate];
                if (candidateMi > bestMi) {
                    bestMi = candidateMi;
                    bestParent = candidate;
                }
            }
            parent[child] = bestParent;
            if (bestParent >= 0) {
                edges++;
                totalMi += bestMi;
            }
        }
        return new Structure(parent, edges, edges == 0 ? 0.0 : totalMi / edges);
    }

    private double[][] estimateConditionals(boolean[][] data, int dim, int[] parent) {
        double[][] conditional = new double[dim][2];
        for (int i = 0; i < dim; i++) {
            int p = parent[i];
            if (p < 0) {
                conditional[i][0] = 0.5;
                conditional[i][1] = 0.5;
                continue;
            }

            int parent0 = 0;
            int parent1 = 0;
            int oneGivenParent0 = 0;
            int oneGivenParent1 = 0;
            for (boolean[] row : data) {
                if (row[p]) {
                    parent1++;
                    if (row[i]) {
                        oneGivenParent1++;
                    }
                } else {
                    parent0++;
                    if (row[i]) {
                        oneGivenParent0++;
                    }
                }
            }

            conditional[i][0] = clamp((oneGivenParent0 + smoothing) / (parent0 + 2.0 * smoothing), 1e-9, 1.0 - 1e-9);
            conditional[i][1] = clamp((oneGivenParent1 + smoothing) / (parent1 + 2.0 * smoothing), 1e-9, 1.0 - 1e-9);
        }
        return conditional;
    }

    private void blendInPlace(double[] estimate, double[] previous, double alpha) {
        for (int i = 0; i < estimate.length; i++) {
            estimate[i] = clamp((1.0 - alpha) * previous[i] + alpha * estimate[i], 1e-9, 1.0 - 1e-9);
        }
    }

    private void blendInPlace(double[][] estimate, double[][] previous, double alpha) {
        for (int i = 0; i < estimate.length; i++) {
            for (int b = 0; b < estimate[i].length; b++) {
                estimate[i][b] = clamp((1.0 - alpha) * previous[i][b] + alpha * estimate[i][b], 1e-9, 1.0 - 1e-9);
            }
        }
    }

    private static double binaryEntropy(double p) {
        return -p * log2Safe(p) - (1.0 - p) * log2Safe(1.0 - p);
    }

    private static double log2Safe(double value) {
        if (value <= 0.0) {
            return 0.0;
        }
        return Math.log(value) / Math.log(2.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Structure(int[] parent, int edgeCount, double meanMutualInformation) {
    }
}
