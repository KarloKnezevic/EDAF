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
 * Plackett-Luce permutation model.
 */
public final class PlackettLuceModel implements Model<PermutationVector> {

    private double[] weights;

    @Override
    public String name() {
        return "plackett-luce";
    }

    @Override
    public void fit(List<Individual<PermutationVector>> selected,
                    Representation<PermutationVector> representation,
                    RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        int n = selected.get(0).genotype().size();
        weights = new double[n];
        for (Individual<PermutationVector> individual : selected) {
            int[] order = individual.genotype().order();
            for (int pos = 0; pos < order.length; pos++) {
                int item = order[pos];
                weights[item] += (n - pos);
            }
        }

        double sum = 0.0;
        for (double weight : weights) {
            sum += weight;
        }
        if (sum > 0) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= sum;
            }
        }
    }

    @Override
    public List<PermutationVector> sample(int count,
                                          Representation<PermutationVector> representation,
                                          Problem<PermutationVector> problem,
                                          ConstraintHandling<PermutationVector> constraintHandling,
                                          RngStream rng) {
        if (weights == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<PermutationVector> samples = new java.util.ArrayList<>(count);
        int n = weights.length;
        for (int sampleIndex = 0; sampleIndex < count; sampleIndex++) {
            int[] permutation = new int[n];
            boolean[] used = new boolean[n];
            for (int pos = 0; pos < n; pos++) {
                permutation[pos] = sampleOne(used, rng);
                used[permutation[pos]] = true;
            }
            samples.add(constraintHandling.enforce(new PermutationVector(permutation), representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (weights == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("pl_item_count", (double) weights.length);
        values.put("pl_max_weight", java.util.Arrays.stream(weights).max().orElse(0.0));
        return new ModelDiagnostics(values);
    }

    private int sampleOne(boolean[] used, RngStream rng) {
        double total = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (!used[i]) {
                total += weights[i];
            }
        }

        double r = rng.nextDouble() * total;
        double cumulative = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (used[i]) {
                continue;
            }
            cumulative += weights[i];
            if (cumulative >= r) {
                return i;
            }
        }

        for (int i = weights.length - 1; i >= 0; i--) {
            if (!used[i]) {
                return i;
            }
        }
        return 0;
    }
}
