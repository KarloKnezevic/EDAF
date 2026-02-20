package com.knezevic.edaf.v3.models.continuous;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;

/**
 * Shared linear-algebra/statistics helpers for continuous probabilistic models.
 */
final class ContinuousModelMath {

    private ContinuousModelMath() {
        // utility class
    }

    static double[] empiricalMean(List<Individual<RealVector>> selected) {
        int dim = selected.getFirst().genotype().length();
        double[] mean = new double[dim];
        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int i = 0; i < dim; i++) {
                mean[i] += x[i];
            }
        }
        double invN = 1.0 / selected.size();
        for (int i = 0; i < dim; i++) {
            mean[i] *= invN;
        }
        return mean;
    }

    static double[][] empiricalCovariance(List<Individual<RealVector>> selected, double[] mean, double jitter) {
        int dim = mean.length;
        double[][] covariance = new double[dim][dim];
        for (Individual<RealVector> individual : selected) {
            double[] x = individual.genotype().values();
            for (int i = 0; i < dim; i++) {
                double di = x[i] - mean[i];
                for (int j = 0; j < dim; j++) {
                    covariance[i][j] += di * (x[j] - mean[j]);
                }
            }
        }

        double denom = Math.max(1.0, selected.size() - 1.0);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                covariance[i][j] /= denom;
            }
            covariance[i][i] += jitter;
        }
        return covariance;
    }

    static void blendInPlace(double[] base, double[] estimate, double learningRate) {
        double alpha = clamp(learningRate, 0.0, 1.0);
        for (int i = 0; i < base.length; i++) {
            base[i] = (1.0 - alpha) * base[i] + alpha * estimate[i];
        }
    }

    static void blendInPlace(double[][] base, double[][] estimate, double learningRate) {
        double alpha = clamp(learningRate, 0.0, 1.0);
        for (int i = 0; i < base.length; i++) {
            for (int j = 0; j < base[i].length; j++) {
                base[i][j] = (1.0 - alpha) * base[i][j] + alpha * estimate[i][j];
            }
        }
    }

    static void applyDiagonalShrinkage(double[][] covariance, double shrinkage) {
        double lambda = clamp(shrinkage, 0.0, 1.0);
        if (lambda <= 0.0) {
            return;
        }
        double keep = 1.0 - lambda;
        for (int i = 0; i < covariance.length; i++) {
            for (int j = 0; j < covariance.length; j++) {
                if (i != j) {
                    covariance[i][j] *= keep;
                }
            }
        }
    }

    static void regularizeSymmetric(double[][] covariance, double jitter) {
        int dim = covariance.length;
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                double sym = 0.5 * (covariance[i][j] + covariance[j][i]);
                covariance[i][j] = sym;
                covariance[j][i] = sym;
            }
            if (!Double.isFinite(covariance[i][i])) {
                covariance[i][i] = 1.0;
            }
            covariance[i][i] = Math.max(jitter, covariance[i][i]);
        }
    }

    static double[][] choleskyWithRetry(double[][] covariance, double jitter) {
        double scale = 1.0;
        for (int attempt = 0; attempt < 7; attempt++) {
            double[][] candidate = deepCopy(covariance);
            for (int i = 0; i < candidate.length; i++) {
                candidate[i][i] += jitter * scale;
            }
            double[][] factor = cholesky(candidate);
            if (factor != null) {
                return factor;
            }
            scale *= 10.0;
        }

        // Final deterministic fallback to diagonal factor.
        int dim = covariance.length;
        double[][] diagonal = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            diagonal[i][i] = Math.sqrt(Math.max(jitter, covariance[i][i]));
        }
        return diagonal;
    }

    static double[] multiplyLowerTriangular(double[][] lower, double[] vector) {
        double[] output = new double[lower.length];
        for (int i = 0; i < lower.length; i++) {
            double sum = 0.0;
            for (int j = 0; j <= i; j++) {
                sum += lower[i][j] * vector[j];
            }
            output[i] = sum;
        }
        return output;
    }

    static double[] solveLowerTriangular(double[][] lower, double[] rhs, double jitter) {
        double[] x = new double[lower.length];
        for (int i = 0; i < lower.length; i++) {
            double sum = rhs[i];
            for (int j = 0; j < i; j++) {
                sum -= lower[i][j] * x[j];
            }
            double pivot = Math.max(jitter, lower[i][i]);
            x[i] = sum / pivot;
        }
        return x;
    }

    static double conditionNumberFromDiagonal(double[][] covariance, double minFloor) {
        double minDiag = Double.POSITIVE_INFINITY;
        double maxDiag = 0.0;
        for (int i = 0; i < covariance.length; i++) {
            minDiag = Math.min(minDiag, covariance[i][i]);
            maxDiag = Math.max(maxDiag, covariance[i][i]);
        }
        return maxDiag / Math.max(minFloor, minDiag);
    }

    static double[][] deepCopy(double[][] matrix) {
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = java.util.Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private static double[][] cholesky(double[][] matrix) {
        int n = matrix.length;
        double[][] lower = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = matrix[i][j];
                for (int k = 0; k < j; k++) {
                    sum -= lower[i][k] * lower[j][k];
                }
                if (i == j) {
                    if (!Double.isFinite(sum) || sum <= 0.0) {
                        return null;
                    }
                    lower[i][j] = Math.sqrt(sum);
                } else {
                    double pivot = lower[j][j];
                    if (!Double.isFinite(pivot) || Math.abs(pivot) < 1e-14) {
                        return null;
                    }
                    lower[i][j] = sum / pivot;
                }
            }
        }
        return lower;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
