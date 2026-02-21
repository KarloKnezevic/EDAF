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
 * Separable NES model with rank-based natural-gradient updates.
 */
public final class SNesModel implements Model<RealVector> {

    private final double etaMean;
    private final double etaSigma;
    private final double minSigma;
    private final double maxSigma;

    private double[] mean;
    private double[] sigma;
    private double lastGradientNorm;

    public SNesModel(double etaMean, double etaSigma, double minSigma, double maxSigma) {
        this.etaMean = Math.max(1.0e-4, etaMean);
        this.etaSigma = Math.max(1.0e-4, etaSigma);
        this.minSigma = Math.max(1.0e-12, minSigma);
        this.maxSigma = Math.max(this.minSigma, maxSigma);
    }

    @Override
    public String name() {
        return "snes";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        int dim = selected.getFirst().genotype().length();
        if (mean == null || sigma == null || mean.length != dim) {
            initializeFromSelected(selected, dim);
        }

        double[] utilities = utilities(selected.size());
        double[] gradientMean = new double[dim];
        double[] gradientSigma = new double[dim];

        for (int rank = 0; rank < selected.size(); rank++) {
            double utility = utilities[rank];
            double[] x = selected.get(rank).genotype().values();
            for (int d = 0; d < dim; d++) {
                double standardized = (x[d] - mean[d]) / Math.max(minSigma, sigma[d]);
                gradientMean[d] += utility * standardized;
                gradientSigma[d] += utility * (standardized * standardized - 1.0);
            }
        }

        double norm = 0.0;
        for (int d = 0; d < dim; d++) {
            mean[d] += etaMean * sigma[d] * gradientMean[d];
            sigma[d] *= Math.exp(0.5 * etaSigma * gradientSigma[d]);
            sigma[d] = clamp(sigma[d], minSigma, maxSigma);
            norm += gradientMean[d] * gradientMean[d] + gradientSigma[d] * gradientSigma[d];
        }
        lastGradientNorm = Math.sqrt(norm);
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

        java.util.ArrayList<RealVector> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            double[] values = new double[mean.length];
            for (int d = 0; d < mean.length; d++) {
                values[d] = mean[d] + sigma[d] * rng.nextGaussian();
            }
            RealVector candidate = new RealVector(values);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (mean == null || sigma == null) {
            return ModelDiagnostics.empty();
        }
        double sigmaMin = Double.POSITIVE_INFINITY;
        double sigmaMax = 0.0;
        for (double value : sigma) {
            sigmaMin = Math.min(sigmaMin, value);
            sigmaMax = Math.max(sigmaMax, value);
        }

        Map<String, Double> values = new LinkedHashMap<>();
        values.put("nes_gradient_norm", lastGradientNorm);
        values.put("snes_sigma_min", sigmaMin);
        values.put("snes_sigma_max", sigmaMax);
        values.put("snes_dim", (double) mean.length);
        return new ModelDiagnostics(values);
    }

    private void initializeFromSelected(List<Individual<RealVector>> selected, int dim) {
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
            sigma[d] = clamp(sigma[d], minSigma, maxSigma);
        }
    }

    private static double[] utilities(int lambda) {
        double[] raw = new double[lambda];
        double sum = 0.0;
        for (int rank = 0; rank < lambda; rank++) {
            raw[rank] = Math.max(0.0, Math.log(lambda / 2.0 + 1.0) - Math.log(rank + 1.0));
            sum += raw[rank];
        }
        if (sum <= 0.0) {
            double uniform = 1.0 / lambda;
            java.util.Arrays.fill(raw, uniform);
            return raw;
        }
        for (int rank = 0; rank < lambda; rank++) {
            raw[rank] = raw[rank] / sum - (1.0 / lambda);
        }
        return raw;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
