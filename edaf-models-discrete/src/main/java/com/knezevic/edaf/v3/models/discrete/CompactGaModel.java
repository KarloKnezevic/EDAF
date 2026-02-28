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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compact Genetic Algorithm (cGA)-style probability-vector model.
 *
 * <p>The implementation keeps a single Bernoulli vector and moves it by
 * fixed {@code step} toward elite empirical frequencies. This preserves the
 * memory-light cGA flavor while keeping integration with the generic EDA
 * model API deterministic and checkpoint-safe.</p>
 *
 * <p>Update direction (per locus):
 * <pre>
 *   p_i <- clip(p_i + step * sign(mean_elite(x_i) - p_i))
 * </pre>
 * with clipping to {@code [1e-6, 1-1e-6]}.
 *
 * <p>References:
 * <ol>
 *   <li>G. R. Harik, F. G. Lobo, and D. E. Goldberg, "The compact genetic algorithm,"
 *   IEEE Transactions on Evolutionary Computation, 1999.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms:
 *   A New Tool for Evolutionary Computation," Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CompactGaModel implements Model<BitString> {

    private final double step;
    private double[] probabilities;

    /**
     * Creates a new CompactGaModel instance.
     *
     * @param step probability move size per fit iteration
     */
    public CompactGaModel(double step) {
        this.step = Math.max(1e-4, step);
    }

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    @Override
    public String name() {
        return "cga-frequency";
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
            double direction = empirical[i] > probabilities[i] ? 1.0 : -1.0;
            probabilities[i] = probabilities[i] + direction * step;
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
            result.add(constraintHandling.enforce(new BitString(genes), representation, problem, rng));
        }
        return result;
    }

    /**
     * Returns model diagnostics snapshot.
     *
     * @return diagnostics snapshot
     */
    @Override
    public ModelDiagnostics diagnostics() {
        if (probabilities == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> numeric = new LinkedHashMap<>();
        numeric.put("cga_step", step);
        numeric.put("cga_mean_probability", java.util.Arrays.stream(probabilities).average().orElse(0.5));
        return new ModelDiagnostics(numeric);
    }
}
