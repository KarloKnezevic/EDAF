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
 * xNES-style full-covariance model with rank-based natural gradient updates.
 */
public final class XNesModel implements Model<RealVector> {

    private final double etaMean;
    private final double etaCovariance;
    private final double jitter;

    private double[] mean;
    private double[][] covariance;
    private double[][] cholesky;
    private double lastUpdateNorm;

    public XNesModel(double etaMean, double etaCovariance, double jitter) {
        this.etaMean = Math.max(1.0e-4, etaMean);
        this.etaCovariance = Math.max(1.0e-4, etaCovariance);
        this.jitter = Math.max(1.0e-12, jitter);
    }

    @Override
    public String name() {
        return "xnes";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        if (mean == null || covariance == null || cholesky == null) {
            initializeFromSelected(selected);
        }

        int dim = mean.length;
        double[] utilities = utilities(selected.size());
        double[] gradMean = new double[dim];
        double[][] gradCovariance = new double[dim][dim];

        for (int rank = 0; rank < selected.size(); rank++) {
            double utility = utilities[rank];
            double[] x = selected.get(rank).genotype().values();

            double[] centered = new double[dim];
            for (int d = 0; d < dim; d++) {
                centered[d] = x[d] - mean[d];
            }

            double[] z = ContinuousModelMath.solveLowerTriangular(cholesky, centered, jitter);
            for (int d = 0; d < dim; d++) {
                gradMean[d] += utility * z[d];
            }
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    double contribution = z[i] * z[j];
                    if (i == j) {
                        contribution -= 1.0;
                    }
                    gradCovariance[i][j] += utility * contribution;
                }
            }
        }

        double[] meanStep = ContinuousModelMath.multiplyLowerTriangular(cholesky, gradMean);
        for (int d = 0; d < dim; d++) {
            mean[d] += etaMean * meanStep[d];
        }

        double[][] covStep = covariantStep(gradCovariance);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                covariance[i][j] += etaCovariance * covStep[i][j];
            }
        }

        ContinuousModelMath.regularizeSymmetric(covariance, jitter);
        cholesky = ContinuousModelMath.choleskyWithRetry(covariance, jitter);

        lastUpdateNorm = stepNorm(meanStep, covStep);
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
        for (int sample = 0; sample < count; sample++) {
            double[] z = new double[mean.length];
            for (int d = 0; d < mean.length; d++) {
                z[d] = rng.nextGaussian();
            }
            double[] offset = ContinuousModelMath.multiplyLowerTriangular(cholesky, z);
            double[] values = new double[mean.length];
            for (int d = 0; d < mean.length; d++) {
                values[d] = mean[d] + offset[d];
            }
            RealVector candidate = new RealVector(values);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (mean == null || covariance == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("xnes_update_step", lastUpdateNorm);
        values.put("xnes_dim", (double) mean.length);
        values.put("cov_condition_number", ContinuousModelMath.conditionNumberFromDiagonal(covariance, jitter));
        return new ModelDiagnostics(values);
    }

    private void initializeFromSelected(List<Individual<RealVector>> selected) {
        mean = ContinuousModelMath.empiricalMean(selected);
        covariance = ContinuousModelMath.empiricalCovariance(selected, mean, jitter);
        ContinuousModelMath.regularizeSymmetric(covariance, jitter);
        cholesky = ContinuousModelMath.choleskyWithRetry(covariance, jitter);
    }

    private double[][] covariantStep(double[][] naturalGradient) {
        int dim = naturalGradient.length;
        double[][] left = new double[dim][dim];
        double[][] step = new double[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                double value = 0.0;
                for (int k = 0; k <= i; k++) {
                    value += cholesky[i][k] * naturalGradient[k][j];
                }
                left[i][j] = value;
            }
        }

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                double value = 0.0;
                for (int k = 0; k < dim; k++) {
                    double right = j >= k ? cholesky[j][k] : 0.0;
                    value += left[i][k] * right;
                }
                step[i][j] = value;
            }
        }
        return step;
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

    private static double stepNorm(double[] meanStep, double[][] covStep) {
        double sum = 0.0;
        for (double value : meanStep) {
            sum += value * value;
        }
        for (double[] row : covStep) {
            for (double value : row) {
                sum += value * value;
            }
        }
        return Math.sqrt(sum);
    }
}
