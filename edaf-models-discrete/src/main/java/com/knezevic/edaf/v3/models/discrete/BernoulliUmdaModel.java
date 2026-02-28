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
 * Univariate Marginal Distribution Algorithm (UMDA) model for bitstrings.
 *
 * <p>The model assumes conditional independence between loci and estimates a
 * Bernoulli probability per bit from selected elites:
 * <pre>
 *   p_i = s + (1 - 2s) * mean(x_i)
 * </pre>
 * where {@code s} is smoothing and {@code mean(x_i)} is empirical elite frequency
 * of bit {@code i} being one.
 *
 * <p>Smoothing keeps probabilities away from 0 and 1, preventing premature
 * fixation and enabling deterministic checkpoint/restart behavior.
 *
 * <p>References:
 * <ol>
 *   <li>H. Muehlenbein and G. Paass, "From recombination of genes to the estimation of
 *   distributions I. Binary parameters," PPSN IV, 1996.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms:
 *   A New Tool for Evolutionary Computation," Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BernoulliUmdaModel implements Model<BitString> {

    private final double smoothing;
    private double[] probabilities;

    /**
     * Creates a new BernoulliUmdaModel instance.
     *
     * @param smoothing probability-floor coefficient in {@code [0, 0.49]}
     */
    public BernoulliUmdaModel(double smoothing) {
        this.smoothing = Math.max(0.0, Math.min(0.49, smoothing));
    }

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    @Override
    public String name() {
        return "umda-bernoulli";
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

    /**
     * Returns a defensive copy of current Bernoulli probabilities.
     *
     * @return Bernoulli probability vector
     */
    public double[] probabilities() {
        return probabilities == null ? new double[0] : java.util.Arrays.copyOf(probabilities, probabilities.length);
    }

    /**
     * Restores model state from checkpoint payload.
     *
     * @param probabilities Bernoulli probability vector persisted in checkpoint
     */
    public void restore(double[] probabilities) {
        this.probabilities = java.util.Arrays.copyOf(probabilities, probabilities.length);
    }
}
