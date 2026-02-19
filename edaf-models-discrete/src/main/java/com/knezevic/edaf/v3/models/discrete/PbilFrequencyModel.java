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
 * PBIL-style frequency model with exponential moving-average updates.
 */
public final class PbilFrequencyModel implements Model<BitString> {

    private final double learningRate;
    private double[] probabilities;

    public PbilFrequencyModel(double learningRate) {
        this.learningRate = Math.max(0.01, Math.min(1.0, learningRate));
    }

    @Override
    public String name() {
        return "pbil-frequency";
    }

    @Override
    public void fit(List<Individual<BitString>> selected, Representation<BitString> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }
        int length = selected.get(0).genotype().length();
        if (probabilities == null) {
            probabilities = new double[length];
            java.util.Arrays.fill(probabilities, 0.5);
        }

        double[] empirical = new double[length];
        for (Individual<BitString> individual : selected) {
            boolean[] genes = individual.genotype().genes();
            for (int i = 0; i < length; i++) {
                empirical[i] += genes[i] ? 1.0 : 0.0;
            }
        }
        for (int i = 0; i < length; i++) {
            empirical[i] /= selected.size();
            probabilities[i] = (1.0 - learningRate) * probabilities[i] + learningRate * empirical[i];
            probabilities[i] = Math.max(1e-6, Math.min(1.0 - 1e-6, probabilities[i]));
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
        java.util.ArrayList<BitString> result = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            boolean[] genes = new boolean[probabilities.length];
            for (int i = 0; i < genes.length; i++) {
                genes[i] = rng.nextDouble() < probabilities[i];
            }
            BitString candidate = new BitString(genes);
            result.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return result;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (probabilities == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> numeric = new LinkedHashMap<>();
        numeric.put("pbil_learning_rate", learningRate);
        numeric.put("pbil_mean_probability", java.util.Arrays.stream(probabilities).average().orElse(0.5));
        return new ModelDiagnostics(numeric);
    }
}
