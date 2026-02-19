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
 * Diagonal Gaussian model for continuous EDAs.
 */
public final class DiagonalGaussianModel implements Model<RealVector> {

    private final double minSigma;
    private double[] mean;
    private double[] sigma;

    public DiagonalGaussianModel(double minSigma) {
        this.minSigma = Math.max(1e-8, minSigma);
    }

    @Override
    public String name() {
        return "gaussian-diag";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected.isEmpty()) {
            return;
        }

        int dim = selected.get(0).genotype().length();
        mean = new double[dim];
        sigma = new double[dim];

        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int d = 0; d < dim; d++) {
                mean[d] += x[d];
            }
        }
        for (int d = 0; d < dim; d++) {
            mean[d] /= selected.size();
        }

        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int d = 0; d < dim; d++) {
                double diff = x[d] - mean[d];
                sigma[d] += diff * diff;
            }
        }
        for (int d = 0; d < dim; d++) {
            sigma[d] = Math.sqrt(sigma[d] / Math.max(1, selected.size() - 1));
            sigma[d] = Math.max(minSigma, sigma[d]);
        }
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (mean == null || sigma == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }
        java.util.ArrayList<RealVector> result = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            double[] values = new double[mean.length];
            for (int d = 0; d < mean.length; d++) {
                values[d] = mean[d] + sigma[d] * rng.nextGaussian();
            }
            RealVector candidate = new RealVector(values);
            result.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return result;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (mean == null || sigma == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> numeric = new LinkedHashMap<>();
        numeric.put("gaussian_dim", (double) mean.length);
        numeric.put("gaussian_sigma_min", java.util.Arrays.stream(sigma).min().orElse(minSigma));
        numeric.put("gaussian_sigma_max", java.util.Arrays.stream(sigma).max().orElse(minSigma));
        double min = java.util.Arrays.stream(sigma).min().orElse(minSigma);
        double max = java.util.Arrays.stream(sigma).max().orElse(minSigma);
        numeric.put("cov_condition_number", max / Math.max(minSigma, min));
        return new ModelDiagnostics(numeric);
    }

    public double[] mean() {
        return mean == null ? new double[0] : java.util.Arrays.copyOf(mean, mean.length);
    }

    public double[] sigma() {
        return sigma == null ? new double[0] : java.util.Arrays.copyOf(sigma, sigma.length);
    }

    /**
     * Restores diagonal Gaussian state from checkpoint payload.
     */
    public void restore(double[] mean, double[] sigma) {
        this.mean = java.util.Arrays.copyOf(mean, mean.length);
        this.sigma = java.util.Arrays.copyOf(sigma, sigma.length);
    }
}
