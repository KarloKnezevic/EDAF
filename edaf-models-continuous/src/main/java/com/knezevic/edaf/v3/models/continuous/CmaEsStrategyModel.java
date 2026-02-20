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
 * CMA-ES strategy model with evolution paths, rank-1/rank-mu covariance updates,
 * cumulative step-size adaptation, and IPOP-like internal restarts.
 *
 * This model assumes selected individuals are already ranked best-first by the
 * outer selection policy (truncation/tournament winner list).
 */
public final class CmaEsStrategyModel implements Model<RealVector> {

    private static final double OFFDIAGONAL_EPSILON = 1e-12;

    private final double minSigma;
    private final double maxSigma;
    private final double jitter;
    private final double initialSigma;

    private final boolean restartEnabled;
    private final int restartPatience;
    private final double restartSigmaMultiplier;
    private final double restartConditionThreshold;
    private final double restartImprovementEpsilon;

    private double[] mean;
    private double sigma;
    private double[][] covariance;
    private double[][] eigenvectors;
    private double[] eigenSqrt;
    private double[][] invSqrtCovariance;
    private double[] pathSigma;
    private double[] pathCovariance;
    private int generation;
    private double muEff;
    private double conditionNumber;

    private int restartCount;
    private int stagnationIterations;
    private double bestFitnessSeen;

    public CmaEsStrategyModel() {
        this(1.0e-12, 5.0, 1.0e-12, -1.0,
                true, 30, 2.0, 1.0e12, 1.0e-12);
    }

    public CmaEsStrategyModel(double minSigma, double maxSigma, double jitter, double initialSigma) {
        this(minSigma, maxSigma, jitter, initialSigma,
                true, 30, 2.0, 1.0e12, 1.0e-12);
    }

    public CmaEsStrategyModel(double minSigma,
                              double maxSigma,
                              double jitter,
                              double initialSigma,
                              boolean restartEnabled,
                              int restartPatience,
                              double restartSigmaMultiplier,
                              double restartConditionThreshold,
                              double restartImprovementEpsilon) {
        this.minSigma = Math.max(1.0e-14, minSigma);
        this.maxSigma = Math.max(this.minSigma, maxSigma);
        this.jitter = Math.max(1.0e-14, jitter);
        this.initialSigma = initialSigma;
        this.restartEnabled = restartEnabled;
        this.restartPatience = Math.max(1, restartPatience);
        this.restartSigmaMultiplier = Math.max(1.01, restartSigmaMultiplier);
        this.restartConditionThreshold = Math.max(10.0, restartConditionThreshold);
        this.restartImprovementEpsilon = Math.max(0.0, restartImprovementEpsilon);
        this.bestFitnessSeen = Double.POSITIVE_INFINITY;
    }

    @Override
    public String name() {
        return "cma-es";
    }

    @Override
    public void fit(List<Individual<RealVector>> selected, Representation<RealVector> representation, RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        double currentBest = selected.stream().mapToDouble(i -> i.fitness().scalar()).min().orElse(Double.POSITIVE_INFINITY);

        if (mean == null || covariance == null) {
            initializeState(selected, currentBest);
            return;
        }

        updateStagnation(currentBest);
        if (shouldRestart()) {
            restartState(selected, currentBest);
            return;
        }

        updateState(selected);
    }

    @Override
    public List<RealVector> sample(int count,
                                   Representation<RealVector> representation,
                                   Problem<RealVector> problem,
                                   ConstraintHandling<RealVector> constraintHandling,
                                   RngStream rng) {
        if (mean == null || eigenvectors == null || eigenSqrt == null || sigma <= 0.0) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        int dim = mean.length;
        java.util.ArrayList<RealVector> samples = new java.util.ArrayList<>(count);
        for (int n = 0; n < count; n++) {
            double[] z = new double[dim];
            for (int i = 0; i < dim; i++) {
                z[i] = rng.nextGaussian();
            }
            double[] y = transformByCovarianceSqrt(z);
            double[] x = new double[dim];
            for (int i = 0; i < dim; i++) {
                x[i] = mean[i] + sigma * y[i];
            }
            RealVector candidate = new RealVector(x);
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
        values.put("gaussian_dim", (double) mean.length);
        values.put("cma_generation", (double) generation);
        values.put("cma_sigma", sigma);
        values.put("cma_step_size", sigma);
        values.put("cma_mu_eff", muEff);
        values.put("cov_condition_number", conditionNumber);
        values.put("cma_cov_trace", trace(covariance));
        values.put("cma_path_sigma_norm", norm(pathSigma));
        values.put("cma_path_cov_norm", norm(pathCovariance));
        values.put("cma_restart_count", (double) restartCount);
        values.put("cma_stagnation_iters", (double) stagnationIterations);
        values.put("cma_best_fitness_seen", bestFitnessSeen);
        return new ModelDiagnostics(values);
    }

    /**
     * Returns copy of current mean vector for checkpoint persistence.
     */
    public double[] mean() {
        return mean == null ? new double[0] : java.util.Arrays.copyOf(mean, mean.length);
    }

    /**
     * Returns current global step size.
     */
    public double sigma() {
        return sigma;
    }

    /**
     * Returns deep copy of current covariance matrix for checkpoint persistence.
     */
    public double[][] covariance() {
        return deepCopy(covariance);
    }

    /**
     * Returns copy of sigma-path vector for checkpoint persistence.
     */
    public double[] pathSigma() {
        return pathSigma == null ? new double[0] : java.util.Arrays.copyOf(pathSigma, pathSigma.length);
    }

    /**
     * Returns copy of covariance-path vector for checkpoint persistence.
     */
    public double[] pathCovariance() {
        return pathCovariance == null ? new double[0] : java.util.Arrays.copyOf(pathCovariance, pathCovariance.length);
    }

    /**
     * Returns current generation counter for checkpoint persistence.
     */
    public int generation() {
        return generation;
    }

    /**
     * Returns number of internal CMA restarts.
     */
    public int restartCount() {
        return restartCount;
    }

    /**
     * Returns current stagnation counter.
     */
    public int stagnationIterations() {
        return stagnationIterations;
    }

    /**
     * Returns best observed fitness tracked by model-level restart logic.
     */
    public double bestFitnessSeen() {
        return bestFitnessSeen;
    }

    /**
     * Restores full CMA-ES state from checkpoint payload.
     */
    public void restore(double[] mean,
                        double sigma,
                        double[][] covariance,
                        double[] pathSigma,
                        double[] pathCovariance,
                        int generation) {
        restore(mean, sigma, covariance, pathSigma, pathCovariance, generation, 0, 0, Double.POSITIVE_INFINITY);
    }

    /**
     * Restores full CMA-ES state including restart counters.
     */
    public void restore(double[] mean,
                        double sigma,
                        double[][] covariance,
                        double[] pathSigma,
                        double[] pathCovariance,
                        int generation,
                        int restartCount,
                        int stagnationIterations,
                        double bestFitnessSeen) {
        if (mean == null || mean.length == 0) {
            throw new IllegalArgumentException("mean must not be empty");
        }
        if (covariance == null || covariance.length != mean.length) {
            throw new IllegalArgumentException("covariance dimension must match mean length");
        }

        this.mean = java.util.Arrays.copyOf(mean, mean.length);
        this.sigma = clamp(sigma, minSigma, maxSigma);
        this.covariance = deepCopy(covariance);
        this.pathSigma = pathSigma == null || pathSigma.length != mean.length
                ? new double[mean.length]
                : java.util.Arrays.copyOf(pathSigma, pathSigma.length);
        this.pathCovariance = pathCovariance == null || pathCovariance.length != mean.length
                ? new double[mean.length]
                : java.util.Arrays.copyOf(pathCovariance, pathCovariance.length);
        this.generation = Math.max(0, generation);
        this.restartCount = Math.max(0, restartCount);
        this.stagnationIterations = Math.max(0, stagnationIterations);
        this.bestFitnessSeen = Double.isFinite(bestFitnessSeen) ? bestFitnessSeen : Double.POSITIVE_INFINITY;

        regularizeCovariance(this.covariance, jitter);
        recomputeDecomposition();
    }

    private void initializeState(List<Individual<RealVector>> selected, double currentBest) {
        int dim = selected.get(0).genotype().length();
        int mu = selected.size();

        double[] weights = recombinationWeights(mu);
        this.muEff = effectiveMass(weights);

        this.mean = weightedMean(selected, weights, dim);

        double[][] empiricalCovariance = weightedCovariance(selected, mean, weights, dim);
        double trace = trace(empiricalCovariance);
        double sigmaEstimate = initialSigma > 0.0
                ? initialSigma
                : Math.sqrt(Math.max(jitter, trace / Math.max(1, dim)));
        this.sigma = clamp(sigmaEstimate, minSigma, maxSigma);

        this.covariance = new double[dim][dim];
        double sigmaSq = Math.max(minSigma * minSigma, this.sigma * this.sigma);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                this.covariance[i][j] = empiricalCovariance[i][j] / sigmaSq;
            }
            this.covariance[i][i] += jitter;
        }

        this.pathSigma = new double[dim];
        this.pathCovariance = new double[dim];
        this.generation = 0;
        this.stagnationIterations = 0;
        this.bestFitnessSeen = currentBest;

        regularizeCovariance(this.covariance, jitter);
        recomputeDecomposition();
    }

    private void updateState(List<Individual<RealVector>> selected) {
        int dim = mean.length;
        int mu = Math.min(selected.size(), dim > 1 ? selected.size() : 1);
        double[] weights = recombinationWeights(mu);
        this.muEff = effectiveMass(weights);

        double[] previousMean = java.util.Arrays.copyOf(mean, mean.length);
        double[][] previousCovariance = deepCopy(covariance);
        double previousSigma = sigma;

        double[] updatedMean = weightedMean(selected, weights, dim);
        double[] yW = new double[dim];
        for (int i = 0; i < dim; i++) {
            yW[i] = (updatedMean[i] - previousMean[i]) / Math.max(minSigma, previousSigma);
        }

        double n = dim;
        double cSigma = (muEff + 2.0) / (n + muEff + 5.0);
        double dSigma = 1.0 + 2.0 * Math.max(0.0, Math.sqrt((muEff - 1.0) / (n + 1.0)) - 1.0) + cSigma;
        double cC = (4.0 + muEff / n) / (n + 4.0 + 2.0 * muEff / n);
        double c1 = 2.0 / (Math.pow(n + 1.3, 2.0) + muEff);
        double cMu = Math.min(1.0 - c1,
                2.0 * (muEff - 2.0 + 1.0 / muEff) / (Math.pow(n + 2.0, 2.0) + muEff));

        double[] invSqrtTimesYW = multiply(invSqrtCovariance, yW);
        double psFactor = Math.sqrt(cSigma * (2.0 - cSigma) * muEff);
        for (int i = 0; i < dim; i++) {
            pathSigma[i] = (1.0 - cSigma) * pathSigma[i] + psFactor * invSqrtTimesYW[i];
        }

        double normPs = norm(pathSigma);
        double chiN = expectedNorm(dim);
        double decay = Math.pow(1.0 - cSigma, 2.0 * (generation + 1.0));
        double hsigThreshold = (1.4 + 2.0 / (n + 1.0)) * chiN * Math.sqrt(Math.max(1e-16, 1.0 - decay));
        boolean hsig = normPs < hsigThreshold;

        double pcFactor = hsig ? Math.sqrt(cC * (2.0 - cC) * muEff) : 0.0;
        for (int i = 0; i < dim; i++) {
            pathCovariance[i] = (1.0 - cC) * pathCovariance[i] + pcFactor * yW[i];
        }

        double[][] rankMu = new double[dim][dim];
        for (int k = 0; k < mu; k++) {
            double[] x = selected.get(k).genotype().values();
            double[] y = new double[dim];
            for (int i = 0; i < dim; i++) {
                y[i] = (x[i] - previousMean[i]) / Math.max(minSigma, previousSigma);
            }
            addOuter(rankMu, y, y, weights[k]);
        }

        double alpha = 1.0 - c1 - cMu;
        if (!hsig) {
            alpha += c1 * cC * (2.0 - cC);
        }
        alpha = Math.max(1.0e-16, alpha);

        double[][] updatedCovariance = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                updatedCovariance[i][j] = alpha * previousCovariance[i][j]
                        + c1 * pathCovariance[i] * pathCovariance[j]
                        + cMu * rankMu[i][j];
            }
        }

        regularizeCovariance(updatedCovariance, jitter);

        double sigmaFactor = Math.exp((cSigma / dSigma) * (normPs / chiN - 1.0));
        this.sigma = clamp(previousSigma * sigmaFactor, minSigma, maxSigma);
        this.mean = updatedMean;
        this.covariance = updatedCovariance;
        this.generation += 1;

        recomputeDecomposition();
    }

    private void updateStagnation(double currentBest) {
        if (currentBest + restartImprovementEpsilon < bestFitnessSeen) {
            bestFitnessSeen = currentBest;
            stagnationIterations = 0;
        } else {
            stagnationIterations += 1;
        }
    }

    private boolean shouldRestart() {
        if (!restartEnabled || mean == null) {
            return false;
        }

        boolean stalled = stagnationIterations >= restartPatience;
        boolean degenerateSigma = sigma <= minSigma * 1.05;
        boolean degenerateCondition = !Double.isFinite(conditionNumber) || conditionNumber > restartConditionThreshold;
        return stalled || degenerateSigma || degenerateCondition;
    }

    private void restartState(List<Individual<RealVector>> selected, double currentBest) {
        int dim = selected.get(0).genotype().length();
        this.mean = java.util.Arrays.copyOf(selected.get(0).genotype().values(), dim);

        double seedSigma;
        if (initialSigma > 0.0) {
            seedSigma = initialSigma;
        } else {
            seedSigma = Math.max(sigma, estimateSigmaFromSelected(selected));
        }

        double expandedSigma = seedSigma * Math.pow(restartSigmaMultiplier, restartCount + 1.0);
        this.sigma = clamp(expandedSigma, minSigma, maxSigma);

        this.covariance = identity(dim);
        for (int i = 0; i < dim; i++) {
            covariance[i][i] = Math.max(1.0, covariance[i][i]) + jitter;
        }

        this.pathSigma = new double[dim];
        this.pathCovariance = new double[dim];
        this.generation = 0;
        this.stagnationIterations = 0;
        this.restartCount += 1;
        this.bestFitnessSeen = Math.min(bestFitnessSeen, currentBest);

        double[] weights = recombinationWeights(Math.max(1, selected.size()));
        this.muEff = effectiveMass(weights);

        regularizeCovariance(this.covariance, jitter);
        recomputeDecomposition();
    }

    private double estimateSigmaFromSelected(List<Individual<RealVector>> selected) {
        int dim = selected.get(0).genotype().length();
        int mu = selected.size();
        double[] weights = recombinationWeights(mu);
        double[] center = weightedMean(selected, weights, dim);
        double[][] empiricalCovariance = weightedCovariance(selected, center, weights, dim);
        return Math.sqrt(Math.max(jitter, trace(empiricalCovariance) / Math.max(1, dim)));
    }

    private void recomputeDecomposition() {
        int dim = covariance.length;
        EigenDecomposition eigen = symmetricEigenDecomposition(covariance, jitter);
        this.eigenvectors = eigen.eigenvectors();
        this.eigenSqrt = new double[dim];

        double minEigen = Double.POSITIVE_INFINITY;
        double maxEigen = 0.0;
        for (int i = 0; i < dim; i++) {
            double value = Math.max(jitter, eigen.eigenvalues()[i]);
            this.eigenSqrt[i] = Math.sqrt(value);
            minEigen = Math.min(minEigen, value);
            maxEigen = Math.max(maxEigen, value);
        }

        this.conditionNumber = maxEigen / Math.max(jitter, minEigen);
        this.invSqrtCovariance = new double[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                double sum = 0.0;
                for (int k = 0; k < dim; k++) {
                    sum += eigenvectors[i][k] * (1.0 / Math.max(Math.sqrt(jitter), eigenSqrt[k])) * eigenvectors[j][k];
                }
                invSqrtCovariance[i][j] = sum;
            }
        }
    }

    private double[] transformByCovarianceSqrt(double[] z) {
        int dim = z.length;
        double[] y = new double[dim];
        for (int i = 0; i < dim; i++) {
            double sum = 0.0;
            for (int k = 0; k < dim; k++) {
                sum += eigenvectors[i][k] * (eigenSqrt[k] * z[k]);
            }
            y[i] = sum;
        }
        return y;
    }

    private static double[] recombinationWeights(int mu) {
        double[] weights = new double[mu];
        double sum = 0.0;
        for (int i = 0; i < mu; i++) {
            weights[i] = Math.log(mu + 0.5) - Math.log(i + 1.0);
            sum += weights[i];
        }
        for (int i = 0; i < mu; i++) {
            weights[i] /= Math.max(1e-16, sum);
        }
        return weights;
    }

    private static double effectiveMass(double[] weights) {
        double sumSquares = 0.0;
        for (double weight : weights) {
            sumSquares += weight * weight;
        }
        return 1.0 / Math.max(1e-16, sumSquares);
    }

    private static double[] weightedMean(List<Individual<RealVector>> selected, double[] weights, int dim) {
        double[] value = new double[dim];
        for (int i = 0; i < weights.length; i++) {
            double[] x = selected.get(i).genotype().values();
            for (int d = 0; d < dim; d++) {
                value[d] += weights[i] * x[d];
            }
        }
        return value;
    }

    private static double[][] weightedCovariance(List<Individual<RealVector>> selected,
                                                 double[] center,
                                                 double[] weights,
                                                 int dim) {
        double[][] cov = new double[dim][dim];
        for (int k = 0; k < weights.length; k++) {
            double[] x = selected.get(k).genotype().values();
            double[] diff = new double[dim];
            for (int i = 0; i < dim; i++) {
                diff[i] = x[i] - center[i];
            }
            addOuter(cov, diff, diff, weights[k]);
        }
        regularizeCovariance(cov, 1e-16);
        return cov;
    }

    private static void addOuter(double[][] matrix, double[] left, double[] right, double scale) {
        for (int i = 0; i < left.length; i++) {
            for (int j = 0; j < right.length; j++) {
                matrix[i][j] += scale * left[i] * right[j];
            }
        }
    }

    private static void regularizeCovariance(double[][] matrix, double jitter) {
        int dim = matrix.length;
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                double a = matrix[i][j];
                double b = matrix[j][i];
                double sym = 0.5 * (a + b);
                if (!Double.isFinite(sym)) {
                    sym = 0.0;
                }
                matrix[i][j] = sym;
                matrix[j][i] = sym;
            }
            if (!Double.isFinite(matrix[i][i])) {
                matrix[i][i] = 1.0;
            }
            matrix[i][i] = Math.max(jitter, matrix[i][i]);
        }
    }

    private static EigenDecomposition symmetricEigenDecomposition(double[][] matrix, double jitter) {
        int n = matrix.length;
        double[][] a = deepCopy(matrix);
        double[][] v = identity(n);

        int maxIterations = Math.max(16, 12 * n * n);
        for (int iter = 0; iter < maxIterations; iter++) {
            int p = 0;
            int q = 1;
            double max = Math.abs(a[p][q]);

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double candidate = Math.abs(a[i][j]);
                    if (candidate > max) {
                        max = candidate;
                        p = i;
                        q = j;
                    }
                }
            }

            if (max < OFFDIAGONAL_EPSILON) {
                break;
            }

            double app = a[p][p];
            double aqq = a[q][q];
            double apq = a[p][q];

            double phi = 0.5 * Math.atan2(2.0 * apq, aqq - app);
            double c = Math.cos(phi);
            double s = Math.sin(phi);

            for (int i = 0; i < n; i++) {
                if (i == p || i == q) {
                    continue;
                }
                double aip = a[i][p];
                double aiq = a[i][q];
                double newAip = c * aip - s * aiq;
                double newAiq = s * aip + c * aiq;
                a[i][p] = newAip;
                a[p][i] = newAip;
                a[i][q] = newAiq;
                a[q][i] = newAiq;
            }

            double newApp = c * c * app - 2.0 * s * c * apq + s * s * aqq;
            double newAqq = s * s * app + 2.0 * s * c * apq + c * c * aqq;
            a[p][p] = newApp;
            a[q][q] = newAqq;
            a[p][q] = 0.0;
            a[q][p] = 0.0;

            for (int i = 0; i < n; i++) {
                double vip = v[i][p];
                double viq = v[i][q];
                v[i][p] = c * vip - s * viq;
                v[i][q] = s * vip + c * viq;
            }
        }

        double[] eigenvalues = new double[n];
        for (int i = 0; i < n; i++) {
            eigenvalues[i] = Math.max(jitter, a[i][i]);
        }

        int[] order = sortIndicesDescending(eigenvalues);
        double[] sortedEigenvalues = new double[n];
        double[][] sortedEigenvectors = new double[n][n];
        for (int col = 0; col < n; col++) {
            int source = order[col];
            sortedEigenvalues[col] = eigenvalues[source];
            for (int row = 0; row < n; row++) {
                sortedEigenvectors[row][col] = v[row][source];
            }
        }

        return new EigenDecomposition(sortedEigenvalues, sortedEigenvectors);
    }

    private static int[] sortIndicesDescending(double[] values) {
        Integer[] indices = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            indices[i] = i;
        }
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(values[b], values[a]));

        int[] primitive = new int[values.length];
        for (int i = 0; i < indices.length; i++) {
            primitive[i] = indices[i];
        }
        return primitive;
    }

    private static double[] multiply(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < matrix.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < vector.length; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }

    private static double norm(double[] vector) {
        if (vector == null) {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    private static double expectedNorm(int dim) {
        double n = dim;
        return Math.sqrt(n) * (1.0 - 1.0 / (4.0 * n) + 1.0 / (21.0 * n * n));
    }

    private static double trace(double[][] matrix) {
        if (matrix == null) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double[][] identity(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1.0;
        }
        return matrix;
    }

    private static double[][] deepCopy(double[][] source) {
        if (source == null) {
            return null;
        }
        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = java.util.Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private record EigenDecomposition(double[] eigenvalues, double[][] eigenvectors) {
    }
}
