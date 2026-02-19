package com.knezevic.edaf.v3.models.permutation;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Edge Histogram Model (EHM) for permutation EDAs.
 */
public final class EdgeHistogramModel implements Model<PermutationVector> {

    private final double epsilon;
    private double[][] transitions;

    public EdgeHistogramModel(double epsilon) {
        this.epsilon = Math.max(1e-9, epsilon);
    }

    @Override
    public String name() {
        return "ehm";
    }

    @Override
    public void fit(List<Individual<PermutationVector>> selected,
                    Representation<PermutationVector> representation,
                    RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        int n = selected.get(0).genotype().size();
        transitions = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transitions[i][j] = i == j ? 0.0 : epsilon;
            }
        }

        for (Individual<PermutationVector> individual : selected) {
            int[] order = individual.genotype().order();
            for (int i = 0; i < n; i++) {
                int from = order[i];
                int to = order[(i + 1) % n];
                transitions[from][to] += 1.0;
            }
        }

        normalizeRows();
    }

    @Override
    public List<PermutationVector> sample(int count,
                                          Representation<PermutationVector> representation,
                                          Problem<PermutationVector> problem,
                                          ConstraintHandling<PermutationVector> constraintHandling,
                                          RngStream rng) {
        if (transitions == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }
        java.util.ArrayList<PermutationVector> samples = new java.util.ArrayList<>(count);
        int n = transitions.length;

        for (int sampleIndex = 0; sampleIndex < count; sampleIndex++) {
            int[] permutation = new int[n];
            boolean[] used = new boolean[n];
            int current = rng.nextInt(n);
            permutation[0] = current;
            used[current] = true;

            for (int pos = 1; pos < n; pos++) {
                int next = sampleNext(current, used, rng);
                permutation[pos] = next;
                used[next] = true;
                current = next;
            }

            PermutationVector candidate = new PermutationVector(permutation);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }

        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (transitions == null) {
            return ModelDiagnostics.empty();
        }
        double entropy = 0.0;
        for (double[] row : transitions) {
            for (double p : row) {
                if (p > 0.0) {
                    entropy -= p * (Math.log(p) / Math.log(2));
                }
            }
        }
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("ehm_entropy", entropy);
        metrics.put("ehm_size", (double) transitions.length);
        return new ModelDiagnostics(metrics);
    }

    private int sampleNext(int current, boolean[] used, RngStream rng) {
        double total = 0.0;
        for (int city = 0; city < transitions[current].length; city++) {
            if (!used[city]) {
                total += transitions[current][city];
            }
        }

        if (total <= 0.0) {
            for (int city = 0; city < used.length; city++) {
                if (!used[city]) {
                    return city;
                }
            }
            return 0;
        }

        double r = rng.nextDouble() * total;
        double cumulative = 0.0;
        for (int city = 0; city < transitions[current].length; city++) {
            if (used[city]) {
                continue;
            }
            cumulative += transitions[current][city];
            if (cumulative >= r) {
                return city;
            }
        }

        for (int city = transitions[current].length - 1; city >= 0; city--) {
            if (!used[city]) {
                return city;
            }
        }
        return 0;
    }

    private void normalizeRows() {
        for (double[] row : transitions) {
            double sum = 0.0;
            for (double v : row) {
                sum += v;
            }
            if (sum <= 0.0) {
                continue;
            }
            for (int j = 0; j < row.length; j++) {
                row[j] /= sum;
            }
        }
    }

    /**
     * Returns a deep copy of transition matrix for checkpoint persistence.
     */
    public double[][] transitions() {
        if (transitions == null) {
            return new double[0][0];
        }
        double[][] copy = new double[transitions.length][];
        for (int i = 0; i < transitions.length; i++) {
            copy[i] = java.util.Arrays.copyOf(transitions[i], transitions[i].length);
        }
        return copy;
    }

    /**
     * Restores transition matrix state from checkpoint payload.
     */
    public void restore(double[][] transitions) {
        this.transitions = new double[transitions.length][];
        for (int i = 0; i < transitions.length; i++) {
            this.transitions[i] = java.util.Arrays.copyOf(transitions[i], transitions[i].length);
        }
    }
}
