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
 * Lightweight flow-based density model for continuous EDAs.
 *
 * <p>Sampling transformation:
 * <pre>
 *   z ~ N(0, I)
 *   u_i = z_i + alpha_i * tanh(z_i)
 *   x = mu + L u
 * </pre>
 * where {@code L} is the Cholesky factor of a smoothed covariance estimate and
 * {@code alpha_i} is adapted from whitened-sample skewness.</p>
 */
public final class NormalizingFlowModel implements Model<RealVector> {

    private final double jitter;
    private final double learningRate;
    private final double maxSkew;

    private double[] mean;
    private double[][] covariance;
    private double[][] cholesky;
    private double[] skew;

    public NormalizingFlowModel(double jitter, double learningRate, double maxSkew) {
        this.jitter = Math.max(1e-10, jitter);
        this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
        this.maxSkew = Math.max(0.01, Math.min(2.0, maxSkew));
    }

    @Override
    public String name() {
        return "normalizing-flow";
    }

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
            skew = new double[mean.length];
        } else {
            ContinuousModelMath.blendInPlace(mean, estimatedMean, learningRate);
            ContinuousModelMath.blendInPlace(covariance, estimatedCovariance, learningRate);
        }

        ContinuousModelMath.regularizeSymmetric(covariance, jitter);
        cholesky = ContinuousModelMath.choleskyWithRetry(covariance, jitter);

        double[] estimatedSkew = estimateWhitenedSkew(selected);
        if (skew == null || skew.length != estimatedSkew.length) {
            skew = estimatedSkew;
        } else {
            ContinuousModelMath.blendInPlace(skew, estimatedSkew, learningRate);
        }
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (mean == null || cholesky == null || skew == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<RealVector> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            double[] z = new double[mean.length];
            for (int i = 0; i < z.length; i++) {
                z[i] = rng.nextGaussian();
            }

            double[] transformedLatent = new double[z.length];
            for (int i = 0; i < z.length; i++) {
                transformedLatent[i] = z[i] + skew[i] * Math.tanh(z[i]);
            }

            double[] correlated = ContinuousModelMath.multiplyLowerTriangular(cholesky, transformedLatent);
            double[] values = new double[mean.length];
            for (int i = 0; i < mean.length; i++) {
                values[i] = mean[i] + correlated[i];
            }

            samples.add(constraintHandling.enforce(new RealVector(values), representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (mean == null || covariance == null || skew == null) {
            return ModelDiagnostics.empty();
        }

        double skewL1 = 0.0;
        double skewMax = 0.0;
        for (double value : skew) {
            double abs = Math.abs(value);
            skewL1 += abs;
            skewMax = Math.max(skewMax, abs);
        }

        Map<String, Double> values = new LinkedHashMap<>();
        values.put("flow_dim", (double) mean.length);
        values.put("flow_skew_abs_mean", skewL1 / mean.length);
        values.put("flow_skew_abs_max", skewMax);
        values.put("flow_learning_rate", learningRate);
        values.put("cov_condition_number", ContinuousModelMath.conditionNumberFromDiagonal(covariance, jitter));
        return new ModelDiagnostics(values);
    }

    public double[] mean() {
        return mean == null ? new double[0] : java.util.Arrays.copyOf(mean, mean.length);
    }

    public double[][] covariance() {
        return covariance == null ? new double[0][0] : ContinuousModelMath.deepCopy(covariance);
    }

    public double[] skew() {
        return skew == null ? new double[0] : java.util.Arrays.copyOf(skew, skew.length);
    }

    /**
     * Restores flow model state from checkpoint payload.
     */
    public void restore(double[] mean, double[][] covariance, double[] skew) {
        if (mean == null || covariance == null || covariance.length != mean.length || skew == null || skew.length != mean.length) {
            throw new IllegalArgumentException("NormalizingFlowModel restore requires compatible mean/covariance/skew dimensions");
        }
        this.mean = java.util.Arrays.copyOf(mean, mean.length);
        this.covariance = ContinuousModelMath.deepCopy(covariance);
        this.skew = java.util.Arrays.copyOf(skew, skew.length);
        ContinuousModelMath.regularizeSymmetric(this.covariance, jitter);
        this.cholesky = ContinuousModelMath.choleskyWithRetry(this.covariance, jitter);
    }

    private double[] estimateWhitenedSkew(List<Individual<RealVector>> selected) {
        int dim = mean.length;
        double[] skewness = new double[dim];
        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            double[] centered = new double[dim];
            for (int i = 0; i < dim; i++) {
                centered[i] = x[i] - mean[i];
            }
            double[] whitened = ContinuousModelMath.solveLowerTriangular(cholesky, centered, jitter);
            for (int i = 0; i < dim; i++) {
                skewness[i] += whitened[i] * whitened[i] * whitened[i];
            }
        }

        double invN = 1.0 / selected.size();
        for (int i = 0; i < dim; i++) {
            double raw = skewness[i] * invN;
            skewness[i] = clamp(raw / 6.0, -maxSkew, maxSkew);
        }
        return skewness;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
