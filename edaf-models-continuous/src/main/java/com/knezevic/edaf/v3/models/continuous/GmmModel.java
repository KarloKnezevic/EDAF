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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagonal-covariance Gaussian Mixture Model with EM fitting and mixture sampling.
 */
public final class GmmModel implements Model<RealVector> {

    private final int components;
    private final int emIterations;
    private final double minVariance;

    private double[] weights;
    private double[][] means;
    private double[][] variances;
    private double lastLogLikelihood;

    public GmmModel(int components, int emIterations, double minVariance) {
        this.components = Math.max(1, components);
        this.emIterations = Math.max(1, emIterations);
        this.minVariance = Math.max(1.0e-10, minVariance);
    }

    @Override
    public String name() {
        return "gmm";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        double[][] data = selected.stream().map(i -> i.genotype().values()).toArray(double[][]::new);
        int sampleCount = data.length;
        int dimension = data[0].length;
        int k = Math.min(components, sampleCount);

        initializeParameters(data, k, dimension);

        double[][] responsibilities = new double[sampleCount][k];
        for (int iteration = 0; iteration < emIterations; iteration++) {
            expectationStep(data, responsibilities, k);
            maximizationStep(data, responsibilities, k, dimension);
        }

        this.lastLogLikelihood = logLikelihood(data, k);
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (weights == null || means == null || variances == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        List<RealVector> samples = new ArrayList<>(count);
        for (int sample = 0; sample < count; sample++) {
            int component = sampleComponent(rng);
            double[] values = new double[means[component].length];
            for (int d = 0; d < values.length; d++) {
                double sigma = Math.sqrt(Math.max(minVariance, variances[component][d]));
                values[d] = means[component][d] + sigma * rng.nextGaussian();
            }
            RealVector candidate = new RealVector(values);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (weights == null || means == null || variances == null) {
            return ModelDiagnostics.empty();
        }
        double entropy = 0.0;
        double maxWeight = 0.0;
        for (double weight : weights) {
            if (weight > 0.0) {
                entropy -= weight * Math.log(weight);
            }
            maxWeight = Math.max(maxWeight, weight);
        }

        Map<String, Double> values = new LinkedHashMap<>();
        values.put("gmm_components", (double) weights.length);
        values.put("gmm_max_component_weight", maxWeight);
        values.put("gmm_component_entropy", entropy);
        values.put("gmm_log_likelihood", lastLogLikelihood);
        return new ModelDiagnostics(values);
    }

    private void initializeParameters(double[][] data, int k, int dimension) {
        this.weights = new double[k];
        Arrays.fill(weights, 1.0 / k);

        this.means = new double[k][dimension];
        this.variances = new double[k][dimension];

        double[] globalMean = new double[dimension];
        for (double[] sample : data) {
            for (int d = 0; d < dimension; d++) {
                globalMean[d] += sample[d];
            }
        }
        for (int d = 0; d < dimension; d++) {
            globalMean[d] /= data.length;
        }

        double[] globalVariance = new double[dimension];
        for (double[] sample : data) {
            for (int d = 0; d < dimension; d++) {
                double diff = sample[d] - globalMean[d];
                globalVariance[d] += diff * diff;
            }
        }
        for (int d = 0; d < dimension; d++) {
            globalVariance[d] = globalVariance[d] / Math.max(1, data.length - 1);
            globalVariance[d] = Math.max(minVariance, globalVariance[d]);
        }

        for (int component = 0; component < k; component++) {
            int source = (int) Math.round((component * (data.length - 1.0)) / Math.max(1.0, k - 1.0));
            means[component] = Arrays.copyOf(data[source], dimension);
            variances[component] = Arrays.copyOf(globalVariance, dimension);
        }
    }

    private void expectationStep(double[][] data, double[][] responsibilities, int k) {
        for (int n = 0; n < data.length; n++) {
            double[] logProbabilities = new double[k];
            double maxLog = Double.NEGATIVE_INFINITY;
            for (int component = 0; component < k; component++) {
                double log = Math.log(Math.max(1.0e-14, weights[component]))
                        + logDiagonalGaussian(data[n], means[component], variances[component]);
                logProbabilities[component] = log;
                maxLog = Math.max(maxLog, log);
            }

            double sumExp = 0.0;
            for (int component = 0; component < k; component++) {
                sumExp += Math.exp(logProbabilities[component] - maxLog);
            }
            double logDenominator = maxLog + Math.log(Math.max(1.0e-300, sumExp));
            for (int component = 0; component < k; component++) {
                responsibilities[n][component] = Math.exp(logProbabilities[component] - logDenominator);
            }
        }
    }

    private void maximizationStep(double[][] data, double[][] responsibilities, int k, int dimension) {
        double[] nk = new double[k];
        for (int n = 0; n < data.length; n++) {
            for (int component = 0; component < k; component++) {
                nk[component] += responsibilities[n][component];
            }
        }

        for (int component = 0; component < k; component++) {
            double effective = Math.max(1.0e-12, nk[component]);
            weights[component] = effective / data.length;

            Arrays.fill(means[component], 0.0);
            for (int n = 0; n < data.length; n++) {
                double responsibility = responsibilities[n][component];
                for (int d = 0; d < dimension; d++) {
                    means[component][d] += responsibility * data[n][d];
                }
            }
            for (int d = 0; d < dimension; d++) {
                means[component][d] /= effective;
            }

            Arrays.fill(variances[component], 0.0);
            for (int n = 0; n < data.length; n++) {
                double responsibility = responsibilities[n][component];
                for (int d = 0; d < dimension; d++) {
                    double diff = data[n][d] - means[component][d];
                    variances[component][d] += responsibility * diff * diff;
                }
            }
            for (int d = 0; d < dimension; d++) {
                variances[component][d] = Math.max(minVariance, variances[component][d] / effective);
            }
        }
    }

    private double logLikelihood(double[][] data, int k) {
        double total = 0.0;
        for (double[] sample : data) {
            double[] logProbabilities = new double[k];
            double maxLog = Double.NEGATIVE_INFINITY;
            for (int component = 0; component < k; component++) {
                double value = Math.log(Math.max(1.0e-14, weights[component]))
                        + logDiagonalGaussian(sample, means[component], variances[component]);
                logProbabilities[component] = value;
                maxLog = Math.max(maxLog, value);
            }
            double sumExp = 0.0;
            for (double value : logProbabilities) {
                sumExp += Math.exp(value - maxLog);
            }
            total += maxLog + Math.log(Math.max(1.0e-300, sumExp));
        }
        return total;
    }

    private static double logDiagonalGaussian(double[] x, double[] mean, double[] variance) {
        double log = 0.0;
        for (int d = 0; d < x.length; d++) {
            double var = Math.max(1.0e-14, variance[d]);
            double diff = x[d] - mean[d];
            log += -0.5 * (Math.log(2.0 * Math.PI * var) + (diff * diff) / var);
        }
        return log;
    }

    private int sampleComponent(RngStream rng) {
        double target = rng.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (cumulative >= target) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
