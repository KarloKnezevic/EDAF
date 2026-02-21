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
 * BMDA model with explicit bivariate dependency tree and conditional sampling.
 */
public final class BmdaModel implements Model<BitString> {

    private final double smoothing;

    private int root;
    private int[] parent;
    private int[] order;
    private double[] marginalOne;
    private double[][] conditionalOne;
    private double averageMutualInformation;

    public BmdaModel(double smoothing) {
        this.smoothing = Math.max(1.0e-9, smoothing);
    }

    @Override
    public String name() {
        return "bmda";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        this.marginalOne = BinaryModelMath.marginalOne(selected, smoothing);
        double[][] mutualInformation = BinaryModelMath.mutualInformation(selected, smoothing);
        this.root = BinaryModelMath.maxEntropyIndex(marginalOne);
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

    @Override
    public ModelDiagnostics diagnostics() {
        if (marginalOne == null || parent == null) {
            return ModelDiagnostics.empty();
        }

        int edgeCount = 0;
        for (int value : parent) {
            if (value >= 0) {
                edgeCount++;
            }
        }

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("bmda_dependency_edges", (double) edgeCount);
        metrics.put("bmda_avg_mutual_information", averageMutualInformation);
        metrics.put("bmda_root_entropy", BinaryModelMath.entropy(marginalOne[root]));
        return new ModelDiagnostics(metrics);
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
