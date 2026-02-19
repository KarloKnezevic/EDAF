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
 * Full-covariance Gaussian model with Cholesky sampling.
 */
public final class FullGaussianModel implements Model<RealVector> {

    private final double jitter;
    private double[] mean;
    private double[][] covariance;
    private double[][] cholesky;

    public FullGaussianModel(double jitter) {
        this.jitter = Math.max(1e-10, jitter);
    }

    @Override
    public String name() {
        return "gaussian-full";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }
        int dim = selected.get(0).genotype().length();
        mean = new double[dim];
        covariance = new double[dim][dim];

        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int i = 0; i < dim; i++) {
                mean[i] += x[i];
            }
        }
        for (int i = 0; i < dim; i++) {
            mean[i] /= selected.size();
        }

        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    covariance[i][j] += (x[i] - mean[i]) * (x[j] - mean[j]);
                }
            }
        }

        double denom = Math.max(1, selected.size() - 1);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                covariance[i][j] /= denom;
            }
            covariance[i][i] += jitter;
        }

        cholesky = cholesky(covariance, jitter);
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

    @Override
    public ModelDiagnostics diagnostics() {
        if (covariance == null) {
            return ModelDiagnostics.empty();
        }
        double minDiag = Double.POSITIVE_INFINITY;
        double maxDiag = 0.0;
        for (int i = 0; i < covariance.length; i++) {
            minDiag = Math.min(minDiag, covariance[i][i]);
            maxDiag = Math.max(maxDiag, covariance[i][i]);
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("cov_condition_number", maxDiag / Math.max(jitter, minDiag));
        values.put("gaussian_dim", (double) covariance.length);
        return new ModelDiagnostics(values);
    }

    private static double[][] cholesky(double[][] matrix, double jitter) {
        int n = matrix.length;
        double[][] l = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = matrix[i][j];
                for (int k = 0; k < j; k++) {
                    sum -= l[i][k] * l[j][k];
                }
                if (i == j) {
                    l[i][j] = Math.sqrt(Math.max(jitter, sum));
                } else {
                    l[i][j] = sum / l[j][j];
                }
            }
        }
        return l;
    }
}
