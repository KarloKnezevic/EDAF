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
 * Gaussian copula baseline with empirical marginals.
 */
public final class CopulaBaselineModel implements Model<RealVector> {

    private final double jitter;

    private double[][] sortedMarginals;
    private double[][] correlation;
    private double[][] cholesky;
    private double averageAbsoluteCorrelation;

    public CopulaBaselineModel(double jitter) {
        this.jitter = Math.max(1.0e-12, jitter);
    }

    @Override
    public String name() {
        return "copula-baseline";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        int n = selected.size();
        int dim = selected.getFirst().genotype().length();
        sortedMarginals = new double[dim][n];
        double[][] z = new double[n][dim];

        for (int d = 0; d < dim; d++) {
            for (int i = 0; i < n; i++) {
                sortedMarginals[d][i] = selected.get(i).genotype().values()[d];
            }
            java.util.Arrays.sort(sortedMarginals[d]);
        }

        for (int i = 0; i < n; i++) {
            double[] values = selected.get(i).genotype().values();
            for (int d = 0; d < dim; d++) {
                double rank = rankOf(values[d], sortedMarginals[d]);
                double u = (rank + 0.5) / n;
                z[i][d] = GaussianMath.inverseNormalCdf(u);
            }
        }

        correlation = estimateCorrelation(z);
        cholesky = ContinuousModelMath.choleskyWithRetry(correlation, jitter);

        double sumAbs = 0.0;
        int countAbs = 0;
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                sumAbs += Math.abs(correlation[i][j]);
                countAbs++;
            }
        }
        averageAbsoluteCorrelation = countAbs == 0 ? 0.0 : sumAbs / countAbs;
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (sortedMarginals == null || cholesky == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        java.util.ArrayList<RealVector> samples = new java.util.ArrayList<>(count);
        int dim = sortedMarginals.length;
        for (int n = 0; n < count; n++) {
            double[] independent = new double[dim];
            for (int i = 0; i < dim; i++) {
                independent[i] = rng.nextGaussian();
            }
            double[] correlated = ContinuousModelMath.multiplyLowerTriangular(cholesky, independent);

            double[] values = new double[dim];
            for (int d = 0; d < dim; d++) {
                double u = GaussianMath.normalCdf(correlated[d]);
                values[d] = empiricalQuantile(sortedMarginals[d], u);
            }

            RealVector candidate = new RealVector(values);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (sortedMarginals == null || correlation == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("copula_rank_dependence", averageAbsoluteCorrelation);
        values.put("copula_dim", (double) sortedMarginals.length);
        values.put("cov_condition_number", ContinuousModelMath.conditionNumberFromDiagonal(correlation, jitter));
        return new ModelDiagnostics(values);
    }

    private double[][] estimateCorrelation(double[][] z) {
        int n = z.length;
        int dim = z[0].length;
        double[][] covariance = new double[dim][dim];
        double[] mean = new double[dim];

        for (double[] row : z) {
            for (int d = 0; d < dim; d++) {
                mean[d] += row[d];
            }
        }
        for (int d = 0; d < dim; d++) {
            mean[d] /= n;
        }

        for (double[] row : z) {
            for (int i = 0; i < dim; i++) {
                double di = row[i] - mean[i];
                for (int j = 0; j < dim; j++) {
                    covariance[i][j] += di * (row[j] - mean[j]);
                }
            }
        }

        double denom = Math.max(1.0, n - 1.0);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                covariance[i][j] /= denom;
            }
        }

        double[][] correlationMatrix = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            double varI = Math.max(jitter, covariance[i][i]);
            for (int j = 0; j < dim; j++) {
                double varJ = Math.max(jitter, covariance[j][j]);
                double value = covariance[i][j] / Math.sqrt(varI * varJ);
                correlationMatrix[i][j] = Double.isFinite(value) ? value : 0.0;
            }
            correlationMatrix[i][i] = 1.0;
        }
        ContinuousModelMath.regularizeSymmetric(correlationMatrix, jitter);
        return correlationMatrix;
    }

    private static double rankOf(double value, double[] sorted) {
        int index = java.util.Arrays.binarySearch(sorted, value);
        if (index >= 0) {
            return index;
        }
        int insertionPoint = -index - 1;
        return Math.max(0, Math.min(sorted.length - 1, insertionPoint));
    }

    private static double empiricalQuantile(double[] sorted, double u) {
        double clipped = Math.max(0.0, Math.min(1.0, u));
        double position = clipped * (sorted.length - 1.0);
        int lower = (int) Math.floor(position);
        int upper = (int) Math.ceil(position);
        if (lower == upper) {
            return sorted[lower];
        }
        double weight = position - lower;
        return sorted[lower] * (1.0 - weight) + sorted[upper] * weight;
    }
}
