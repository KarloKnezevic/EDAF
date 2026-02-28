/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MIMIC-style model using a Chow-Liu maximum-weight dependency tree.
 *
 * <p>The model approximates the elite distribution with the best tree-structured
 * factorization under pairwise mutual information. In this implementation,
 * the root is chosen as the minimum-entropy variable to stabilize chain/tree
 * sampling on highly decisive loci.
 *
 * <p>References:
 * <ol>
 *   <li>P. A. Bosman and D. Thierens, "MIMIC from a Bayesian perspective,"
 *   PPSN, 2000.</li>
 *   <li>C. K. Chow and C. N. Liu, "Approximating discrete probability distributions
 *   with dependence trees," IEEE Transactions on Information Theory, 1968.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MimicChowLiuModel implements Model<BitString> {

    private final double smoothing;

    private int root;
    private int[] parent;
    private int[] order;
    private double[] marginalOne;
    private double[][] conditionalOne;
    private double averageMutualInformation;

    /**
     * Creates a new MimicChowLiuModel instance.
     *
     * @param smoothing Laplace smoothing for marginal and conditional estimates
     */
    public MimicChowLiuModel(double smoothing) {
        this.smoothing = Math.max(1.0e-9, smoothing);
    }

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    @Override
    public String name() {
        return "mimic-chow-liu";
    }

    /**
     * Fits the probabilistic model parameters from selected elite individuals.
     *
     * @param selected selected individual list
     * @param representation genotype representation
     * @param rng random stream
     */
    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        this.marginalOne = BinaryModelMath.marginalOne(selected, smoothing);
        double[][] mutualInformation = BinaryModelMath.mutualInformation(selected, smoothing);
        this.root = BinaryModelMath.minEntropyIndex(marginalOne);
        this.parent = BinaryModelMath.maximumSpanningTree(mutualInformation, root);
        this.order = BinaryModelMath.topologicalOrderFromTree(parent, root);
        this.conditionalOne = BinaryModelMath.conditionalOneForTree(selected, parent, smoothing);
        this.averageMutualInformation = averageEdgeMutualInformation(mutualInformation, parent);
    }

    @Override
    public List<BitString> sample(int count,
                                  Representation<BitString> representation,
                                  Problem<BitString> problem,
                                  ConstraintHandling<BitString> constraintHandling,
                                  RngStream rng) {
        if (marginalOne == null || parent == null || conditionalOne == null || order == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }
        List<BitString> samples = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            BitString raw = BinaryModelMath.sampleTree(order, parent, marginalOne, conditionalOne, rng);
            samples.add(constraintHandling.enforce(raw, representation, problem, rng));
        }
        return samples;
    }

    /**
     * Returns model diagnostics snapshot.
     *
     * @return diagnostics snapshot
     */
    @Override
    public ModelDiagnostics diagnostics() {
        if (marginalOne == null || parent == null) {
            return ModelDiagnostics.empty();
        }

        int maxDepth = maxDepth(parent, root);
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("mimic_tree_depth", (double) maxDepth);
        metrics.put("mimic_avg_mutual_information", averageMutualInformation);
        metrics.put("mimic_root_entropy", BinaryModelMath.entropy(marginalOne[root]));
        return new ModelDiagnostics(metrics);
    }

    private static int maxDepth(int[] parent, int root) {
        int maxDepth = 0;
        for (int node = 0; node < parent.length; node++) {
            int depth = 0;
            int current = node;
            while (current >= 0 && current != root) {
                current = parent[current];
                depth++;
            }
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }

    private static double averageEdgeMutualInformation(double[][] mutualInformation, int[] parent) {
        double sum = 0.0;
        int edges = 0;
        for (int node = 0; node < parent.length; node++) {
            int p = parent[node];
            if (p >= 0) {
                sum += mutualInformation[node][p];
                edges++;
            }
        }
        return edges == 0 ? 0.0 : sum / edges;
    }
}
