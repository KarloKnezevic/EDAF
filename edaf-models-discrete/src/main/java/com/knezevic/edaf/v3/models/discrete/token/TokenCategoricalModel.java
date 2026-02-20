package com.knezevic.edaf.v3.models.discrete.token;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Categorical token model for variable-length integer vectors.
 */
public final class TokenCategoricalModel implements Model<VariableLengthVector<Integer>> {

    private final int maxToken;
    private final double smoothing;

    private int minLength;
    private int maxLength;
    private double[] lengthProbabilities;
    private double[][] tokenProbabilities;

    public TokenCategoricalModel(int maxToken, double smoothing) {
        this.maxToken = Math.max(2, maxToken);
        this.smoothing = Math.max(0.0, Math.min(0.2, smoothing));
    }

    @Override
    public String name() {
        return "token-categorical";
    }

    @Override
    public void fit(List<Individual<VariableLengthVector<Integer>>> selected,
                    Representation<VariableLengthVector<Integer>> representation,
                    RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        this.minLength = Integer.MAX_VALUE;
        this.maxLength = 0;
        for (Individual<VariableLengthVector<Integer>> individual : selected) {
            int length = individual.genotype().size();
            minLength = Math.min(minLength, length);
            maxLength = Math.max(maxLength, length);
        }

        int lengthRange = Math.max(1, maxLength - minLength + 1);
        lengthProbabilities = new double[lengthRange];
        tokenProbabilities = new double[maxLength][maxToken];

        for (Individual<VariableLengthVector<Integer>> individual : selected) {
            List<Integer> values = individual.genotype().values();
            int length = values.size();
            lengthProbabilities[length - minLength] += 1.0;

            for (int i = 0; i < values.size(); i++) {
                int token = Math.floorMod(values.get(i), maxToken);
                tokenProbabilities[i][token] += 1.0;
            }
        }

        normalize(lengthProbabilities, smoothing);
        for (int i = 0; i < tokenProbabilities.length; i++) {
            normalize(tokenProbabilities[i], smoothing);
        }
    }

    @Override
    public List<VariableLengthVector<Integer>> sample(int count,
                                                      Representation<VariableLengthVector<Integer>> representation,
                                                      Problem<VariableLengthVector<Integer>> problem,
                                                      ConstraintHandling<VariableLengthVector<Integer>> constraintHandling,
                                                      RngStream rng) {
        if (lengthProbabilities == null || tokenProbabilities == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<VariableLengthVector<Integer>> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            int lengthOffset = sampleIndex(lengthProbabilities, rng);
            int length = minLength + lengthOffset;

            java.util.ArrayList<Integer> tokens = new java.util.ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                int token = sampleIndex(tokenProbabilities[Math.min(i, tokenProbabilities.length - 1)], rng);
                tokens.add(token);
            }

            VariableLengthVector<Integer> candidate = new VariableLengthVector<>(tokens);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (lengthProbabilities == null) {
            return ModelDiagnostics.empty();
        }

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("token_model_min_length", (double) minLength);
        metrics.put("token_model_max_length", (double) maxLength);
        metrics.put("token_model_mean_length", expectedLength());
        metrics.put("token_model_length_entropy", entropy(lengthProbabilities));
        return new ModelDiagnostics(metrics);
    }

    private double expectedLength() {
        double mean = 0.0;
        for (int i = 0; i < lengthProbabilities.length; i++) {
            mean += (minLength + i) * lengthProbabilities[i];
        }
        return mean;
    }

    private static int sampleIndex(double[] probabilities, RngStream rng) {
        double threshold = rng.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (threshold <= cumulative) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    private static void normalize(double[] values, double smoothing) {
        double total = 0.0;
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.max(0.0, values[i]) + smoothing;
            total += values[i];
        }
        if (total <= 0.0) {
            java.util.Arrays.fill(values, 1.0 / values.length);
            return;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] /= total;
        }
    }

    private static double entropy(double[] probabilities) {
        double sum = 0.0;
        for (double p : probabilities) {
            if (p > 0.0) {
                sum -= p * (Math.log(p) / Math.log(2.0));
            }
        }
        return sum;
    }
}
