package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kernel density estimator with Gaussian kernels and Silverman-style bandwidth.
 */
public final class KdeModel implements Model<RealVector> {

    private final double bandwidthScale;
    private final double minBandwidth;

    private double[][] kernels;
    private double[] bandwidth;

    public KdeModel(double bandwidthScale, double minBandwidth) {
        this.bandwidthScale = Math.max(1.0e-6, bandwidthScale);
        this.minBandwidth = Math.max(1.0e-10, minBandwidth);
    }

    @Override
    public String name() {
        return "kde";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        int n = selected.size();
        int dim = selected.getFirst().genotype().length();
        kernels = new double[n][dim];
        for (int i = 0; i < n; i++) {
            kernels[i] = selected.get(i).genotype().values().clone();
        }

        double[] mean = new double[dim];
        for (double[] sample : kernels) {
            for (int d = 0; d < dim; d++) {
                mean[d] += sample[d];
            }
        }
        for (int d = 0; d < dim; d++) {
            mean[d] /= n;
        }

        double[] std = new double[dim];
        for (double[] sample : kernels) {
            for (int d = 0; d < dim; d++) {
                double diff = sample[d] - mean[d];
                std[d] += diff * diff;
            }
        }
        for (int d = 0; d < dim; d++) {
            std[d] = Math.sqrt(std[d] / Math.max(1, n - 1));
        }

        bandwidth = new double[dim];
        double silvermanFactor = Math.pow(n, -1.0 / (dim + 4.0));
        for (int d = 0; d < dim; d++) {
            double candidate = bandwidthScale * silvermanFactor * Math.max(minBandwidth, std[d]);
            bandwidth[d] = Math.max(minBandwidth, candidate);
        }
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (kernels == null || bandwidth == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }
        List<RealVector> samples = new ArrayList<>(count);
        for (int sampleIndex = 0; sampleIndex < count; sampleIndex++) {
            int kernelIndex = rng.nextInt(kernels.length);
            double[] center = kernels[kernelIndex];
            double[] values = new double[center.length];
            for (int d = 0; d < center.length; d++) {
                values[d] = center[d] + bandwidth[d] * rng.nextGaussian();
            }
            RealVector candidate = new RealVector(values);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (kernels == null || bandwidth == null) {
            return ModelDiagnostics.empty();
        }
        double meanBandwidth = 0.0;
        double maxBandwidth = 0.0;
        for (double value : bandwidth) {
            meanBandwidth += value;
            maxBandwidth = Math.max(maxBandwidth, value);
        }
        meanBandwidth /= bandwidth.length;

        Map<String, Double> values = new LinkedHashMap<>();
        values.put("kde_kernel_count", (double) kernels.length);
        values.put("kde_bandwidth_mean", meanBandwidth);
        values.put("kde_bandwidth_max", maxBandwidth);
        return new ModelDiagnostics(values);
    }
}
