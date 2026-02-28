/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full-covariance Gaussian model ({@code N(μ, Σ)}) with Cholesky sampling.
 *
 * <p>Model update uses empirical elite estimates blended with optional momentum:
 * <pre>
 *   μ <- (1-α) μ + α μ_hat
 *   Σ <- (1-α) Σ + α Σ_hat
 * </pre>
 * followed by optional diagonal shrinkage and symmetric regularization.
 *
 * <p>Sampling draws {@code z ~ N(0, I)} and computes
 * {@code x = μ + Lz}, where {@code LLᵀ = Σ}.
 *
 * <p>References:
 * <ol>
 *   <li>P. Larranaga, "Optimization in continuous domains by learning and simulation
 *   of Gaussian networks," GECCO Workshop, 2000.</li>
 *   <li>P. Larranaga and J. A. Lozano (eds.), "Estimation of Distribution Algorithms:
 *   A New Tool for Evolutionary Computation," Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class FullGaussianModel implements Model<RealVector> {

    private final double jitter;
    private final double learningRate;
    private final double shrinkage;
    private double[] mean;
    private double[][] covariance;
    private double[][] cholesky;

    /**
     * Creates a new FullGaussianModel instance.
     *
     * @param jitter diagonal regularization term added before decomposition
     */
    public FullGaussianModel(double jitter) {
        this(jitter, 1.0, 0.0);
    }

    /**
     * Creates a new FullGaussianModel instance.
     *
     * @param jitter diagonal regularization term
     * @param learningRate blend factor used for temporal smoothing of mean/covariance
     * @param shrinkage off-diagonal shrinkage factor in {@code [0,1]}
     */
    public FullGaussianModel(double jitter, double learningRate, double shrinkage) {
        this.jitter = Math.max(1e-10, jitter);
        this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
        this.shrinkage = Math.max(0.0, Math.min(1.0, shrinkage));
    }

    /**
     * Returns component name identifier.
     *
     * @return component name
     */
    @Override
    public String name() {
        return "gaussian-full";
    }

    /**
     * Fits the probabilistic model parameters from selected elite individuals.
     *
     * @param selected selected individual list
     * @param representation genotype representation
     * @param rng random stream
     */
    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        double[] estimatedMean = ContinuousModelMath.empiricalMean(selected);
        double[][] estimatedCovariance = ContinuousModelMath.empiricalCovariance(selected, estimatedMean, jitter);

        if (mean == null || covariance == null || mean.length != estimatedMean.length) {
            mean = estimatedMean;
            covariance = estimatedCovariance;
        } else {
            ContinuousModelMath.blendInPlace(mean, estimatedMean, learningRate);
            ContinuousModelMath.blendInPlace(covariance, estimatedCovariance, learningRate);
        }

        ContinuousModelMath.applyDiagonalShrinkage(covariance, shrinkage);
        ContinuousModelMath.regularizeSymmetric(covariance, jitter);
        cholesky = ContinuousModelMath.choleskyWithRetry(covariance, jitter);
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (mean == null || cholesky == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }
        java.util.ArrayList<RealVector> samples = new java.util.ArrayList<>(count);
        int dim = mean.length;
        for (int n = 0; n < count; n++) {
            double[] z = new double[dim];
            for (int i = 0; i < dim; i++) {
                z[i] = rng.nextGaussian();
            }
            double[] x = new double[dim];
            for (int i = 0; i < dim; i++) {
                double acc = 0.0;
                for (int j = 0; j <= i; j++) {
                    acc += cholesky[i][j] * z[j];
                }
                x[i] = mean[i] + acc;
            }
            samples.add(constraintHandling.enforce(new RealVector(x), representation, problem, rng));
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
        if (covariance == null) {
            return ModelDiagnostics.empty();
        }

        Map<String, Double> values = new LinkedHashMap<>();
        values.put("cov_condition_number", ContinuousModelMath.conditionNumberFromDiagonal(covariance, jitter));
        values.put("gaussian_dim", (double) covariance.length);
        values.put("gaussian_learning_rate", learningRate);
        values.put("gaussian_shrinkage", shrinkage);
        return new ModelDiagnostics(values);
    }

    /**
     * Returns a defensive copy of the current mean vector.
     *
     * @return mean vector {@code μ}
     */
    public double[] mean() {
        return mean == null ? new double[0] : java.util.Arrays.copyOf(mean, mean.length);
    }

    /**
     * Returns a defensive copy of the current covariance matrix.
     *
     * @return covariance matrix {@code Σ}
     */
    public double[][] covariance() {
        return covariance == null ? new double[0][0] : ContinuousModelMath.deepCopy(covariance);
    }

    /**
     * Restores full Gaussian state from checkpoint payload.
     *
     * @param mean mean vector {@code μ}
     * @param covariance covariance matrix {@code Σ}
     */
    public void restore(double[] mean, double[][] covariance) {
        if (mean == null || covariance == null || covariance.length != mean.length) {
            throw new IllegalArgumentException("FullGaussianModel restore requires compatible mean/covariance dimensions");
        }
        this.mean = java.util.Arrays.copyOf(mean, mean.length);
        this.covariance = ContinuousModelMath.deepCopy(covariance);
        ContinuousModelMath.regularizeSymmetric(this.covariance, jitter);
        this.cholesky = ContinuousModelMath.choleskyWithRetry(this.covariance, jitter);
    }
}
