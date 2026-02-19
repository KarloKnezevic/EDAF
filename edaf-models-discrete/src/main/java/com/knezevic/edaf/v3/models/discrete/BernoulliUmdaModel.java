package com.knezevic.edaf.v3.models.discrete;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UMDA Bernoulli factorized model for bitstring representation.
 */
public final class BernoulliUmdaModel implements Model<BitString> {

    private final double smoothing;
    private double[] probabilities;

    public BernoulliUmdaModel(double smoothing) {
        this.smoothing = Math.max(0.0, Math.min(0.49, smoothing));
    }

    @Override
    public String name() {
        return "umda-bernoulli";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }
        int length = selected.get(0).genotype().length();
        probabilities = new double[length];

        for (Individual<BitString> individual : selected) {
            boolean[] genes = individual.genotype().genes();
            for (int i = 0; i < length; i++) {
                probabilities[i] += genes[i] ? 1.0 : 0.0;
            }
        }

        for (int i = 0; i < length; i++) {
            double mean = probabilities[i] / selected.size();
            probabilities[i] = smoothing + (1.0 - 2 * smoothing) * mean;
        }
    }

    @Override
    public List<BitString> sample(int count,
                                  Representation<BitString> representation,
                                  Problem<BitString> problem,
                                  ConstraintHandling<BitString> constraintHandling,
                                  RngStream rng) {
        if (probabilities == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<BitString> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            boolean[] genes = new boolean[probabilities.length];
            for (int i = 0; i < probabilities.length; i++) {
                genes[i] = rng.nextDouble() < probabilities[i];
            }
            BitString sampled = new BitString(genes);
            samples.add(constraintHandling.enforce(sampled, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (probabilities == null) {
            return ModelDiagnostics.empty();
        }
        double entropy = 0.0;
        for (double p : probabilities) {
            double q = 1.0 - p;
            entropy += term(p) + term(q);
        }
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("model_entropy", entropy);
        metrics.put("model_mean_probability", java.util.Arrays.stream(probabilities).average().orElse(0.0));
        return new ModelDiagnostics(metrics);
    }

    private static double term(double p) {
        if (p <= 0.0) {
            return 0.0;
        }
        return -p * (Math.log(p) / Math.log(2));
    }

    public double[] probabilities() {
        return probabilities == null ? new double[0] : java.util.Arrays.copyOf(probabilities, probabilities.length);
    }

    /**
     * Restores model state from checkpoint payload.
     */
    public void restore(double[] probabilities) {
        this.probabilities = java.util.Arrays.copyOf(probabilities, probabilities.length);
    }
}
