package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BOA/EBNA-inspired sparse Bayesian-network model over bitstrings.
 */
public final class BoaEbnaModel implements Model<BitString> {

    private final int maxParents;
    private final double smoothing;

    private int[] order;
    private int[][] parents;
    private double[] marginalOne;
    private double[][] conditionalProbabilities;
    private int totalEdgeCount;

    public BoaEbnaModel(int maxParents, double smoothing) {
        this.maxParents = Math.max(0, Math.min(8, maxParents));
        this.smoothing = Math.max(1.0e-9, smoothing);
    }

    @Override
    public String name() {
        return "boa-ebna";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        this.marginalOne = BinaryModelMath.marginalOne(selected, smoothing);
        double[][] mutualInformation = BinaryModelMath.mutualInformation(selected, smoothing);
        this.order = BinaryModelMath.orderByEntropy(marginalOne, true);
        this.parents = new int[marginalOne.length][];
        this.conditionalProbabilities = new double[marginalOne.length][];

        totalEdgeCount = 0;
        int[] position = inversePermutation(order);
        for (int node = 0; node < marginalOne.length; node++) {
            int[] parentList = selectParents(node, mutualInformation, position);
            parents[node] = parentList;
            totalEdgeCount += parentList.length;
            conditionalProbabilities[node] = estimateConditionalTable(selected, node, parentList, smoothing);
        }
    }

    @Override
    public List<BitString> sample(int count,
                                  Representation<BitString> representation,
                                  Problem<BitString> problem,
                                  ConstraintHandling<BitString> constraintHandling,
                                  RngStream rng) {
        if (order == null || parents == null || conditionalProbabilities == null || marginalOne == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        List<BitString> samples = new ArrayList<>(count);
        for (int sampleIndex = 0; sampleIndex < count; sampleIndex++) {
            boolean[] genes = new boolean[marginalOne.length];
            for (int node : order) {
                int[] nodeParents = parents[node];
                double probability;
                if (nodeParents.length == 0) {
                    probability = marginalOne[node];
                } else {
                    int parentState = encodeParentState(genes, nodeParents);
                    probability = conditionalProbabilities[node][parentState];
                }
                genes[node] = rng.nextDouble() < BinaryModelMath.clampProbability(probability);
            }
            BitString raw = new BitString(genes);
            samples.add(constraintHandling.enforce(raw, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (order == null || parents == null || marginalOne == null) {
            return ModelDiagnostics.empty();
        }

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("boa_network_edges", (double) totalEdgeCount);
        metrics.put("boa_mean_parent_count", totalEdgeCount / (double) parents.length);
        metrics.put("boa_max_parent_count", (double) Arrays.stream(parents).mapToInt(p -> p.length).max().orElse(0));
        metrics.put("boa_order_entropy_head", BinaryModelMath.entropy(marginalOne[order[0]]));
        return new ModelDiagnostics(metrics);
    }

    private int[] selectParents(int node, double[][] mutualInformation, int[] position) {
        List<Integer> candidates = new ArrayList<>();
        int nodePosition = position[node];
        for (int i = 0; i < position.length; i++) {
            if (position[i] < nodePosition) {
                candidates.add(i);
            }
        }
        candidates.sort((a, b) -> Double.compare(mutualInformation[node][b], mutualInformation[node][a]));
        int size = Math.min(maxParents, candidates.size());
        int[] selected = new int[size];
        for (int i = 0; i < size; i++) {
            selected[i] = candidates.get(i);
        }
        return selected;
    }

    private static double[] estimateConditionalTable(List<Individual<BitString>> selected,
                                                     int node,
                                                     int[] parents,
                                                     double smoothing) {
        if (parents.length == 0) {
            return new double[]{0.5};
        }
        int states = 1 << parents.length;
        double[] totals = new double[states];
        double[] ones = new double[states];
        for (Individual<BitString> individual : selected) {
            boolean[] genes = individual.genotype().genes();
            int state = encodeParentState(genes, parents);
            totals[state] += 1.0;
            if (genes[node]) {
                ones[state] += 1.0;
            }
        }

        double[] probabilities = new double[states];
        for (int state = 0; state < states; state++) {
            probabilities[state] = (ones[state] + smoothing) / (totals[state] + 2.0 * smoothing);
            probabilities[state] = BinaryModelMath.clampProbability(probabilities[state]);
        }
        return probabilities;
    }

    private static int encodeParentState(boolean[] genes, int[] parents) {
        int state = 0;
        for (int i = 0; i < parents.length; i++) {
            if (genes[parents[i]]) {
                state |= (1 << i);
            }
        }
        return state;
    }

    private static int[] inversePermutation(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            inverse[permutation[i]] = i;
        }
        return inverse;
    }
}
