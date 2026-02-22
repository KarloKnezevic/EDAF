package com.knezevic.edaf.v3.core.metrics;

import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.LatentTelemetry;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.util.Params;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation-aware latent-knowledge metrics extracted from population, elite samples, and model state.
 *
 * <p>The analyzer intentionally emits both scalar summaries (for compact storage and filtering)
 * and structured payloads (for heatmaps/graphs in web and offline reports).</p>
 */
public final class LatentKnowledgeAnalyzer {

    private static final double LOG_2 = Math.log(2.0);

    private LatentKnowledgeAnalyzer() {
        // utility class
    }

    /**
     * Computes representation-specific latent telemetry.
     */
    public static <G> LatentTelemetry analyze(Population<G> population,
                                              List<Individual<G>> elite,
                                              Model<G> model,
                                              LatentTelemetry previous,
                                              Map<String, Object> params) {
        if ((population == null || population.size() == 0) && (elite == null || elite.isEmpty())) {
            return LatentTelemetry.empty();
        }

        Object sample = firstGenotype(population, elite);
        if (sample == null) {
            return LatentTelemetry.empty();
        }

        if (extractBooleanArray(sample, "genes") != null) {
            return analyzeBinary(population, elite, previous, params);
        }
        if (extractIntArray(sample, "order") != null) {
            return analyzePermutation(population, elite, previous, params);
        }
        if (extractDoubleArray(sample, "values") != null) {
            return analyzeReal(population, elite, model, previous, params);
        }

        return LatentTelemetry.empty();
    }

    private static <G> LatentTelemetry analyzeBinary(Population<G> population,
                                                     List<Individual<G>> elite,
                                                     LatentTelemetry previous,
                                                     Map<String, Object> params) {
        List<boolean[]> populationGenes = extractBitstrings(population);
        List<boolean[]> eliteGenes = extractBitstrings(elite);
        if (eliteGenes.isEmpty()) {
            eliteGenes = populationGenes;
        }
        if (eliteGenes.isEmpty()) {
            return LatentTelemetry.empty();
        }

        int length = eliteGenes.getFirst().length;
        int topK = Math.max(1, Params.integer(params, "latentTopK", 10));
        int dependencyTopK = Math.max(1, Params.integer(params, "latentDependencyTopK", 16));
        int maxPairwiseDimensions = Math.max(2, Params.integer(params, "latentPairwiseMaxDimensions", 64));
        int pairLimit = Math.max(1, Params.integer(params, "latentPairSampleLimit", 1200));
        double fixationEps = clamp(Params.dbl(params, "latentFixationEpsilon", 0.02), 1.0e-6, 0.499);
        boolean dependencyEnabled = Params.bool(params, "latentDependencyEnabled", true);

        double[] p = new double[length];
        for (boolean[] genes : eliteGenes) {
            for (int i = 0; i < length; i++) {
                p[i] += genes[i] ? 1.0 : 0.0;
            }
        }
        for (int i = 0; i < length; i++) {
            p[i] /= eliteGenes.size();
        }

        double[] entropy = new double[length];
        double meanEntropy = 0.0;
        double minEntropy = Double.POSITIVE_INFINITY;
        double maxEntropy = Double.NEGATIVE_INFINITY;
        int fixed = 0;
        double meanCertainty = 0.0;

        for (int i = 0; i < length; i++) {
            entropy[i] = binaryEntropy(p[i]);
            meanEntropy += entropy[i];
            minEntropy = Math.min(minEntropy, entropy[i]);
            maxEntropy = Math.max(maxEntropy, entropy[i]);
            if (p[i] < fixationEps || p[i] > 1.0 - fixationEps) {
                fixed++;
            }
            meanCertainty += Math.abs(p[i] - 0.5) * 2.0;
        }
        meanEntropy /= length;
        meanCertainty /= length;
        double fixationRatio = fixed / (double) length;

        List<Map<String, Object>> topDecidedBits = topRankedBitsByScore(p, topK, true);
        List<Map<String, Object>> topUncertainBits = topRankedBitsByEntropy(entropy, topK);

        List<Map<String, Object>> dependencyEdges = List.of();
        List<List<Integer>> dependencyClusters = List.of();
        double strongestDependency = 0.0;

        if (dependencyEnabled) {
            int dims = Math.min(length, maxPairwiseDimensions);
            DependencySummary summary = dependencySummary(eliteGenes, dims, dependencyTopK);
            dependencyEdges = summary.edges();
            dependencyClusters = summary.clusters();
            strongestDependency = summary.maxWeight();
        }

        Map<String, Double> drift = new LinkedHashMap<>();
        double[] previousP = toDoubleArray(previous.insights().get("probabilities"));
        if (previousP != null && previousP.length == p.length) {
            drift.put("binary_prob_l1", l1Distance(p, previousP));
            drift.put("binary_prob_l2", l2Distance(p, previousP));
            drift.put("binary_prob_kl", binaryKl(p, previousP));
            drift.put("binary_entropy_delta",
                    previous.metrics().getOrDefault("binary_mean_entropy", meanEntropy) - meanEntropy);
        } else {
            drift.put("binary_prob_l1", 0.0);
            drift.put("binary_prob_l2", 0.0);
            drift.put("binary_prob_kl", 0.0);
            drift.put("binary_entropy_delta", 0.0);
        }

        Map<String, Double> diversity = new LinkedHashMap<>();
        diversity.put("hamming_population", averageHamming(populationGenes, pairLimit));
        diversity.put("hamming_elite", averageHamming(eliteGenes, pairLimit));

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("binary_length", (double) length);
        metrics.put("binary_mean_probability", Arrays.stream(p).average().orElse(0.0));
        metrics.put("binary_mean_entropy", meanEntropy);
        metrics.put("binary_min_entropy", minEntropy);
        metrics.put("binary_max_entropy", maxEntropy);
        metrics.put("binary_fixation_ratio", fixationRatio);
        metrics.put("binary_mean_certainty", meanCertainty);
        metrics.put("binary_dependency_max", strongestDependency);

        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("probabilities", toList(p));
        insights.put("entropyPerBit", toList(entropy));
        insights.put("topDecidedBits", topDecidedBits);
        insights.put("topUncertainBits", topUncertainBits);
        insights.put("dependencyEdges", dependencyEdges);
        insights.put("dependencyClusters", dependencyClusters);

        return new LatentTelemetry("binary", metrics, insights, drift, diversity);
    }

    private static <G> LatentTelemetry analyzePermutation(Population<G> population,
                                                          List<Individual<G>> elite,
                                                          LatentTelemetry previous,
                                                          Map<String, Object> params) {
        List<int[]> populationPermutations = extractPermutations(population);
        List<int[]> elitePermutations = extractPermutations(elite);
        if (elitePermutations.isEmpty()) {
            elitePermutations = populationPermutations;
        }
        if (elitePermutations.isEmpty()) {
            return LatentTelemetry.empty();
        }

        int n = elitePermutations.getFirst().length;
        int topK = Math.max(1, Params.integer(params, "latentTopK", 10));
        int pairLimit = Math.max(1, Params.integer(params, "latentPairSampleLimit", 900));

        double[][] positionDistribution = new double[n][n];
        for (int[] permutation : elitePermutations) {
            for (int position = 0; position < n; position++) {
                int item = permutation[position];
                if (item >= 0 && item < n) {
                    positionDistribution[item][position] += 1.0;
                }
            }
        }
        for (int item = 0; item < n; item++) {
            for (int position = 0; position < n; position++) {
                positionDistribution[item][position] /= elitePermutations.size();
            }
        }

        double[] positionEntropy = new double[n];
        double meanPositionEntropy = 0.0;
        double minPositionEntropy = Double.POSITIVE_INFINITY;
        double maxPositionEntropy = Double.NEGATIVE_INFINITY;
        for (int item = 0; item < n; item++) {
            double entropy = 0.0;
            for (int position = 0; position < n; position++) {
                entropy += entropyTerm(positionDistribution[item][position]);
            }
            positionEntropy[item] = entropy;
            meanPositionEntropy += entropy;
            minPositionEntropy = Math.min(minPositionEntropy, entropy);
            maxPositionEntropy = Math.max(maxPositionEntropy, entropy);
        }
        meanPositionEntropy /= n;

        int[] consensus = consensusPermutation(elitePermutations, n);
        int[] previousConsensus = toIntArray(previous.insights().get("consensusPermutation"));

        Map<String, Double> adjacency = adjacencyFrequencies(elitePermutations);
        Map<String, Double> previousAdjacency = toDoubleMap(previous.insights().get("adjacencyFrequencies"));
        List<Map<String, Object>> topAdjacency = topAdjacencyEdges(adjacency, previousAdjacency, topK);

        Map<String, Double> drift = new LinkedHashMap<>();
        if (previousConsensus != null && previousConsensus.length == consensus.length) {
            drift.put("consensus_kendall",
                    kendallDistanceNormalized(consensus, previousConsensus));
        } else {
            drift.put("consensus_kendall", 0.0);
        }

        Map<String, Double> diversity = new LinkedHashMap<>();
        diversity.put("kendall_population", averageKendall(populationPermutations, pairLimit));
        diversity.put("kendall_elite", averageKendall(elitePermutations, pairLimit));

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("perm_size", (double) n);
        metrics.put("perm_position_entropy_mean", meanPositionEntropy);
        metrics.put("perm_position_entropy_min", minPositionEntropy);
        metrics.put("perm_position_entropy_max", maxPositionEntropy);
        metrics.put("perm_top_edge_frequency", topAdjacency.isEmpty()
                ? 0.0
                : asDouble(topAdjacency.getFirst().get("frequency"), 0.0));

        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("positionDistribution", toNestedList(positionDistribution));
        insights.put("positionEntropyPerItem", toList(positionEntropy));
        insights.put("consensusPermutation", toIntList(consensus));
        insights.put("topAdjacencyEdges", topAdjacency);
        insights.put("adjacencyFrequencies", adjacency);

        return new LatentTelemetry("permutation", metrics, insights, drift, diversity);
    }

    private static <G> LatentTelemetry analyzeReal(Population<G> population,
                                                   List<Individual<G>> elite,
                                                   Model<G> model,
                                                   LatentTelemetry previous,
                                                   Map<String, Object> params) {
        List<double[]> populationVectors = extractRealVectors(population);
        List<double[]> eliteVectors = extractRealVectors(elite);
        if (eliteVectors.isEmpty()) {
            eliteVectors = populationVectors;
        }
        if (eliteVectors.isEmpty()) {
            return LatentTelemetry.empty();
        }

        int dim = eliteVectors.getFirst().length;
        int topK = Math.max(1, Params.integer(params, "latentTopK", 10));
        int pairLimit = Math.max(1, Params.integer(params, "latentPairSampleLimit", 800));
        double sigmaCollapseThreshold = Math.max(1.0e-12, Params.dbl(params, "realSigmaCollapseThreshold", 1.0e-3));
        double nearIdenticalThreshold = Math.max(0.0, Params.dbl(params, "realNearIdenticalThreshold", 1.0e-7));

        double[] mean = estimateMean(eliteVectors, dim);
        double[] sigma = estimateSigma(eliteVectors, mean, dim);

        double[] modelMean = extractDoubleArray(model, "mean");
        if (modelMean != null && modelMean.length == dim) {
            mean = modelMean;
        }
        double[] modelSigma = extractDoubleArray(model, "sigma");
        if (modelSigma != null && modelSigma.length == dim) {
            sigma = clipSigma(modelSigma);
        }

        double[][] covariance = extractDoubleMatrix(model, "covariance");
        if (covariance == null || covariance.length != dim || covariance[0].length != dim) {
            covariance = empiricalCovariance(eliteVectors, mean, dim);
        }

        double[] eigenvalues = covariance.length <= 30
                ? jacobiEigenvalues(covariance)
                : covarianceDiagonal(covariance);

        double[] logSigma = new double[dim];
        double sigmaMean = 0.0;
        double sigmaMin = Double.POSITIVE_INFINITY;
        double sigmaMax = Double.NEGATIVE_INFINITY;
        int collapsedDims = 0;

        for (int i = 0; i < dim; i++) {
            sigma[i] = Math.max(1.0e-12, sigma[i]);
            logSigma[i] = Math.log(sigma[i]);
            sigmaMean += sigma[i];
            sigmaMin = Math.min(sigmaMin, sigma[i]);
            sigmaMax = Math.max(sigmaMax, sigma[i]);
            if (sigma[i] < sigmaCollapseThreshold) {
                collapsedDims++;
            }
        }
        sigmaMean /= dim;

        double[] previousMean = toDoubleArray(previous.insights().get("meanVector"));
        double[] previousSigma = toDoubleArray(previous.insights().get("sigmaVector"));

        Map<String, Double> drift = new LinkedHashMap<>();
        if (previousMean != null && previousSigma != null
                && previousMean.length == dim && previousSigma.length == dim) {
            drift.put("mean_l2", l2Distance(mean, previousMean));
            drift.put("sigma_l2", l2Distance(sigma, previousSigma));
            drift.put("gaussian_kl_diag", gaussianKlDiag(mean, sigma, previousMean, previousSigma));
        } else {
            drift.put("mean_l2", 0.0);
            drift.put("sigma_l2", 0.0);
            drift.put("gaussian_kl_diag", 0.0);
        }

        Map<String, Double> diversity = new LinkedHashMap<>();
        diversity.put("euclidean_population", averageEuclidean(populationVectors, pairLimit));
        diversity.put("euclidean_elite", averageEuclidean(eliteVectors, pairLimit));
        diversity.put("near_identical_ratio", nearIdenticalRatio(populationVectors, pairLimit, nearIdenticalThreshold));
        diversity.put("covariance_trace", trace(covariance));

        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("real_dim", (double) dim);
        metrics.put("real_sigma_mean", sigmaMean);
        metrics.put("real_sigma_min", sigmaMin);
        metrics.put("real_sigma_max", sigmaMax);
        metrics.put("real_entropy_proxy_mean", Arrays.stream(logSigma).average().orElse(0.0));
        metrics.put("real_differential_entropy", differentialEntropy(covariance, sigma));
        metrics.put("real_collapsed_dim_ratio", collapsedDims / (double) dim);

        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("meanVector", toList(mean));
        insights.put("sigmaVector", toList(sigma));
        insights.put("logSigma", toList(logSigma));
        insights.put("eigenvalues", toList(eigenvalues));
        insights.put("collapsedDimensions", collapsedDimensions(sigma, sigmaCollapseThreshold));
        insights.put("topVaryingDimensions", topVaryingDimensions(sigma, topK));

        return new LatentTelemetry("real", metrics, insights, drift, diversity);
    }

    private static Object firstGenotype(Population<?> population, List<? extends Individual<?>> elite) {
        if (elite != null && !elite.isEmpty()) {
            return elite.getFirst().genotype();
        }
        if (population != null && population.size() > 0) {
            return population.get(0).genotype();
        }
        return null;
    }

    private static List<boolean[]> extractBitstrings(Population<?> population) {
        List<boolean[]> rows = new ArrayList<>();
        if (population == null) {
            return rows;
        }
        for (Individual<?> individual : population) {
            boolean[] genes = extractBooleanArray(individual.genotype(), "genes");
            if (genes != null) {
                rows.add(genes);
            }
        }
        return rows;
    }

    private static List<boolean[]> extractBitstrings(List<? extends Individual<?>> individuals) {
        List<boolean[]> rows = new ArrayList<>();
        if (individuals == null) {
            return rows;
        }
        for (Individual<?> individual : individuals) {
            boolean[] genes = extractBooleanArray(individual.genotype(), "genes");
            if (genes != null) {
                rows.add(genes);
            }
        }
        return rows;
    }

    private static List<int[]> extractPermutations(Population<?> population) {
        List<int[]> rows = new ArrayList<>();
        if (population == null) {
            return rows;
        }
        for (Individual<?> individual : population) {
            int[] order = extractIntArray(individual.genotype(), "order");
            if (order != null) {
                rows.add(order);
            }
        }
        return rows;
    }

    private static List<int[]> extractPermutations(List<? extends Individual<?>> individuals) {
        List<int[]> rows = new ArrayList<>();
        if (individuals == null) {
            return rows;
        }
        for (Individual<?> individual : individuals) {
            int[] order = extractIntArray(individual.genotype(), "order");
            if (order != null) {
                rows.add(order);
            }
        }
        return rows;
    }

    private static List<double[]> extractRealVectors(Population<?> population) {
        List<double[]> rows = new ArrayList<>();
        if (population == null) {
            return rows;
        }
        for (Individual<?> individual : population) {
            double[] values = extractDoubleArray(individual.genotype(), "values");
            if (values != null) {
                rows.add(values);
            }
        }
        return rows;
    }

    private static List<double[]> extractRealVectors(List<? extends Individual<?>> individuals) {
        List<double[]> rows = new ArrayList<>();
        if (individuals == null) {
            return rows;
        }
        for (Individual<?> individual : individuals) {
            double[] values = extractDoubleArray(individual.genotype(), "values");
            if (values != null) {
                rows.add(values);
            }
        }
        return rows;
    }

    private static List<Map<String, Object>> topRankedBitsByScore(double[] probabilities, int topK, boolean decided) {
        Integer[] indices = new Integer[probabilities.length];
        for (int i = 0; i < probabilities.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble((Integer idx) -> Math.abs(probabilities[idx] - 0.5)).reversed());

        List<Map<String, Object>> rows = new ArrayList<>();
        int count = Math.min(topK, indices.length);
        for (int i = 0; i < count; i++) {
            int idx = indices[i];
            double certainty = Math.abs(probabilities[idx] - 0.5) * 2.0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("index", idx);
            row.put(decided ? "certainty" : "score", certainty);
            row.put("probability", probabilities[idx]);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> topRankedBitsByEntropy(double[] entropy, int topK) {
        Integer[] indices = new Integer[entropy.length];
        for (int i = 0; i < entropy.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, Comparator.comparingDouble((Integer idx) -> entropy[idx]).reversed());

        List<Map<String, Object>> rows = new ArrayList<>();
        int count = Math.min(topK, indices.length);
        for (int i = 0; i < count; i++) {
            int idx = indices[i];
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("index", idx);
            row.put("entropy", entropy[idx]);
            rows.add(row);
        }
        return rows;
    }

    private static DependencySummary dependencySummary(List<boolean[]> eliteGenes, int dims, int topK) {
        List<Map<String, Object>> allEdges = new ArrayList<>();
        double maxWeight = 0.0;
        for (int i = 0; i < dims; i++) {
            for (int j = i + 1; j < dims; j++) {
                double[] stats = binaryDependencyStats(eliteGenes, i, j);
                double mi = stats[0];
                double corr = stats[1];
                Map<String, Object> edge = new LinkedHashMap<>();
                edge.put("i", i);
                edge.put("j", j);
                edge.put("weight", mi);
                edge.put("correlation", corr);
                allEdges.add(edge);
                maxWeight = Math.max(maxWeight, mi);
            }
        }

        allEdges.sort(Comparator.comparingDouble((Map<String, Object> e) -> asDouble(e.get("weight"), 0.0)).reversed());
        List<Map<String, Object>> topEdges = allEdges.subList(0, Math.min(topK, allEdges.size()));

        UnionFind unionFind = new UnionFind(dims);
        for (Map<String, Object> edge : topEdges) {
            if (asDouble(edge.get("weight"), 0.0) > 0.0) {
                unionFind.union(asInt(edge.get("i"), 0), asInt(edge.get("j"), 0));
            }
        }

        Map<Integer, List<Integer>> grouped = new LinkedHashMap<>();
        for (int i = 0; i < dims; i++) {
            grouped.computeIfAbsent(unionFind.find(i), key -> new ArrayList<>()).add(i);
        }
        List<List<Integer>> clusters = grouped.values().stream()
                .filter(cluster -> cluster.size() > 1)
                .toList();

        return new DependencySummary(new ArrayList<>(topEdges), clusters, maxWeight);
    }

    private static double[] binaryDependencyStats(List<boolean[]> eliteGenes, int i, int j) {
        double smooth = 1.0;
        double c00 = smooth;
        double c01 = smooth;
        double c10 = smooth;
        double c11 = smooth;
        for (boolean[] genes : eliteGenes) {
            boolean xi = genes[i];
            boolean xj = genes[j];
            if (!xi && !xj) {
                c00++;
            } else if (!xi) {
                c01++;
            } else if (!xj) {
                c10++;
            } else {
                c11++;
            }
        }

        double total = c00 + c01 + c10 + c11;
        double p00 = c00 / total;
        double p01 = c01 / total;
        double p10 = c10 / total;
        double p11 = c11 / total;
        double px0 = p00 + p01;
        double px1 = p10 + p11;
        double py0 = p00 + p10;
        double py1 = p01 + p11;

        double mi = 0.0;
        mi += miTerm(p00, px0 * py0);
        mi += miTerm(p01, px0 * py1);
        mi += miTerm(p10, px1 * py0);
        mi += miTerm(p11, px1 * py1);

        double numerator = (p11 * p00) - (p10 * p01);
        double denominator = Math.sqrt(Math.max(1.0e-20, px0 * px1 * py0 * py1));
        double corr = numerator / denominator;
        return new double[]{Math.max(0.0, mi), corr};
    }

    private static double miTerm(double joint, double independent) {
        if (joint <= 0.0 || independent <= 0.0) {
            return 0.0;
        }
        return joint * (Math.log(joint / independent) / LOG_2);
    }

    private static double averageHamming(List<boolean[]> values, int pairLimit) {
        if (values.size() <= 1) {
            return 0.0;
        }
        int length = values.getFirst().length;
        int pairs = 0;
        double sum = 0.0;
        for (int i = 0; i < values.size() && pairs < pairLimit; i++) {
            for (int j = i + 1; j < values.size() && pairs < pairLimit; j++) {
                int diff = 0;
                boolean[] a = values.get(i);
                boolean[] b = values.get(j);
                for (int d = 0; d < length; d++) {
                    if (a[d] != b[d]) {
                        diff++;
                    }
                }
                sum += diff / (double) length;
                pairs++;
            }
        }
        return pairs == 0 ? 0.0 : sum / pairs;
    }

    private static int[] consensusPermutation(List<int[]> permutations, int n) {
        double[] scores = new double[n];
        for (int[] permutation : permutations) {
            for (int position = 0; position < n; position++) {
                scores[permutation[position]] += position;
            }
        }
        Integer[] items = new Integer[n];
        for (int i = 0; i < n; i++) {
            scores[i] /= permutations.size();
            items[i] = i;
        }
        Arrays.sort(items, Comparator.comparingDouble(i -> scores[i]));

        int[] consensus = new int[n];
        for (int i = 0; i < n; i++) {
            consensus[i] = items[i];
        }
        return consensus;
    }

    private static Map<String, Double> adjacencyFrequencies(List<int[]> permutations) {
        Map<String, Double> frequencies = new LinkedHashMap<>();
        if (permutations.isEmpty()) {
            return frequencies;
        }
        for (int[] permutation : permutations) {
            int n = permutation.length;
            for (int i = 0; i < n; i++) {
                int from = permutation[i];
                int to = permutation[(i + 1) % n];
                String key = edgeKey(from, to);
                frequencies.merge(key, 1.0, Double::sum);
            }
        }
        double divisor = permutations.size();
        for (Map.Entry<String, Double> entry : frequencies.entrySet()) {
            entry.setValue(entry.getValue() / divisor);
        }
        return frequencies;
    }

    private static List<Map<String, Object>> topAdjacencyEdges(Map<String, Double> adjacency,
                                                                Map<String, Double> previousAdjacency,
                                                                int topK) {
        return adjacency.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> {
                    int[] edge = parseEdge(entry.getKey());
                    double previousValue = previousAdjacency.getOrDefault(entry.getKey(), 0.0);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("from", edge[0]);
                    row.put("to", edge[1]);
                    row.put("frequency", entry.getValue());
                    row.put("trend", entry.getValue() - previousValue);
                    return row;
                })
                .toList();
    }

    private static String edgeKey(int from, int to) {
        return from + "->" + to;
    }

    private static int[] parseEdge(String key) {
        String[] parts = key.split("->", 2);
        if (parts.length != 2) {
            return new int[]{0, 0};
        }
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private static double averageKendall(List<int[]> permutations, int pairLimit) {
        if (permutations.size() <= 1) {
            return 0.0;
        }
        int n = permutations.getFirst().length;
        double normalizer = n <= 1 ? 1.0 : (n * (n - 1)) / 2.0;
        int pairs = 0;
        double sum = 0.0;
        for (int i = 0; i < permutations.size() && pairs < pairLimit; i++) {
            for (int j = i + 1; j < permutations.size() && pairs < pairLimit; j++) {
                sum += kendallDistance(permutations.get(i), permutations.get(j)) / normalizer;
                pairs++;
            }
        }
        return pairs == 0 ? 0.0 : sum / pairs;
    }

    private static int kendallDistance(int[] a, int[] b) {
        int n = a.length;
        int[] position = new int[n];
        for (int i = 0; i < n; i++) {
            position[b[i]] = i;
        }
        int inversions = 0;
        int[] mapped = new int[n];
        for (int i = 0; i < n; i++) {
            mapped[i] = position[a[i]];
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (mapped[i] > mapped[j]) {
                    inversions++;
                }
            }
        }
        return inversions;
    }

    private static double kendallDistanceNormalized(int[] a, int[] b) {
        int n = a.length;
        double normalizer = n <= 1 ? 1.0 : (n * (n - 1)) / 2.0;
        return kendallDistance(a, b) / normalizer;
    }

    private static double[] estimateMean(List<double[]> values, int dim) {
        double[] mean = new double[dim];
        if (values.isEmpty()) {
            return mean;
        }
        for (double[] row : values) {
            for (int d = 0; d < dim; d++) {
                mean[d] += row[d];
            }
        }
        for (int d = 0; d < dim; d++) {
            mean[d] /= values.size();
        }
        return mean;
    }

    private static double[] estimateSigma(List<double[]> values, double[] mean, int dim) {
        double[] sigma = new double[dim];
        if (values.size() <= 1) {
            Arrays.fill(sigma, 1.0e-12);
            return sigma;
        }
        for (double[] row : values) {
            for (int d = 0; d < dim; d++) {
                double diff = row[d] - mean[d];
                sigma[d] += diff * diff;
            }
        }
        for (int d = 0; d < dim; d++) {
            sigma[d] = Math.sqrt(Math.max(1.0e-18, sigma[d] / (values.size() - 1.0)));
        }
        return sigma;
    }

    private static double[] clipSigma(double[] sigma) {
        double[] clipped = Arrays.copyOf(sigma, sigma.length);
        for (int i = 0; i < clipped.length; i++) {
            clipped[i] = Math.max(1.0e-12, clipped[i]);
        }
        return clipped;
    }

    private static double[][] empiricalCovariance(List<double[]> values, double[] mean, int dim) {
        double[][] covariance = new double[dim][dim];
        if (values.size() <= 1) {
            for (int i = 0; i < dim; i++) {
                covariance[i][i] = 1.0e-12;
            }
            return covariance;
        }
        for (double[] row : values) {
            for (int i = 0; i < dim; i++) {
                double di = row[i] - mean[i];
                for (int j = i; j < dim; j++) {
                    double dj = row[j] - mean[j];
                    covariance[i][j] += di * dj;
                }
            }
        }
        double scale = 1.0 / Math.max(1.0, values.size() - 1.0);
        for (int i = 0; i < dim; i++) {
            for (int j = i; j < dim; j++) {
                covariance[i][j] *= scale;
                covariance[j][i] = covariance[i][j];
            }
            covariance[i][i] = Math.max(covariance[i][i], 1.0e-12);
        }
        return covariance;
    }

    private static double[] covarianceDiagonal(double[][] covariance) {
        double[] diagonal = new double[covariance.length];
        for (int i = 0; i < covariance.length; i++) {
            diagonal[i] = covariance[i][i];
        }
        Arrays.sort(diagonal);
        for (int i = 0; i < diagonal.length / 2; i++) {
            double temp = diagonal[i];
            diagonal[i] = diagonal[diagonal.length - i - 1];
            diagonal[diagonal.length - i - 1] = temp;
        }
        return diagonal;
    }

    /**
     * Jacobi eigenvalue decomposition for small symmetric matrices.
     */
    private static double[] jacobiEigenvalues(double[][] covariance) {
        int n = covariance.length;
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i] = Arrays.copyOf(covariance[i], n);
        }

        int maxIterations = Math.max(20, n * n * 10);
        for (int iter = 0; iter < maxIterations; iter++) {
            int p = 0;
            int q = 1;
            double maxOffDiagonal = 0.0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double value = Math.abs(matrix[i][j]);
                    if (value > maxOffDiagonal) {
                        maxOffDiagonal = value;
                        p = i;
                        q = j;
                    }
                }
            }
            if (maxOffDiagonal < 1.0e-12) {
                break;
            }

            double app = matrix[p][p];
            double aqq = matrix[q][q];
            double apq = matrix[p][q];
            double phi = 0.5 * Math.atan2(2.0 * apq, aqq - app);
            double c = Math.cos(phi);
            double s = Math.sin(phi);

            for (int k = 0; k < n; k++) {
                if (k == p || k == q) {
                    continue;
                }
                double aik = matrix[p][k];
                double aqk = matrix[q][k];
                matrix[p][k] = c * aik - s * aqk;
                matrix[k][p] = matrix[p][k];
                matrix[q][k] = s * aik + c * aqk;
                matrix[k][q] = matrix[q][k];
            }

            matrix[p][p] = c * c * app - 2.0 * s * c * apq + s * s * aqq;
            matrix[q][q] = s * s * app + 2.0 * s * c * apq + c * c * aqq;
            matrix[p][q] = 0.0;
            matrix[q][p] = 0.0;
        }

        double[] eigenvalues = new double[n];
        for (int i = 0; i < n; i++) {
            eigenvalues[i] = Math.max(1.0e-12, matrix[i][i]);
        }
        Arrays.sort(eigenvalues);
        for (int i = 0; i < eigenvalues.length / 2; i++) {
            double temp = eigenvalues[i];
            eigenvalues[i] = eigenvalues[eigenvalues.length - i - 1];
            eigenvalues[eigenvalues.length - i - 1] = temp;
        }
        return eigenvalues;
    }

    private static double differentialEntropy(double[][] covariance, double[] sigma) {
        if (covariance != null && covariance.length > 0) {
            double[] eigen = covariance.length <= 30 ? jacobiEigenvalues(covariance) : covarianceDiagonal(covariance);
            double logDet = 0.0;
            for (double value : eigen) {
                logDet += Math.log(Math.max(1.0e-12, value));
            }
            return 0.5 * (covariance.length * (1.0 + Math.log(2.0 * Math.PI)) + logDet);
        }

        double logDet = 0.0;
        for (double s : sigma) {
            logDet += 2.0 * Math.log(Math.max(1.0e-12, s));
        }
        return 0.5 * (sigma.length * (1.0 + Math.log(2.0 * Math.PI)) + logDet);
    }

    private static double averageEuclidean(List<double[]> values, int pairLimit) {
        if (values.size() <= 1) {
            return 0.0;
        }
        int pairs = 0;
        double sum = 0.0;
        for (int i = 0; i < values.size() && pairs < pairLimit; i++) {
            for (int j = i + 1; j < values.size() && pairs < pairLimit; j++) {
                sum += euclidean(values.get(i), values.get(j));
                pairs++;
            }
        }
        return pairs == 0 ? 0.0 : sum / pairs;
    }

    private static double nearIdenticalRatio(List<double[]> values, int pairLimit, double threshold) {
        if (values.size() <= 1) {
            return 1.0;
        }
        int pairs = 0;
        int near = 0;
        for (int i = 0; i < values.size() && pairs < pairLimit; i++) {
            for (int j = i + 1; j < values.size() && pairs < pairLimit; j++) {
                if (euclidean(values.get(i), values.get(j)) <= threshold) {
                    near++;
                }
                pairs++;
            }
        }
        return pairs == 0 ? 0.0 : near / (double) pairs;
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private static double trace(double[][] matrix) {
        double trace = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            trace += matrix[i][i];
        }
        return trace;
    }

    private static List<Integer> collapsedDimensions(double[] sigma, double threshold) {
        List<Integer> dims = new ArrayList<>();
        for (int i = 0; i < sigma.length; i++) {
            if (sigma[i] < threshold) {
                dims.add(i);
            }
        }
        return dims;
    }

    private static List<Map<String, Object>> topVaryingDimensions(double[] sigma, int topK) {
        Integer[] dims = new Integer[sigma.length];
        for (int i = 0; i < sigma.length; i++) {
            dims[i] = i;
        }
        Arrays.sort(dims, Comparator.comparingDouble((Integer idx) -> sigma[idx]).reversed());

        List<Map<String, Object>> rows = new ArrayList<>();
        int count = Math.min(topK, dims.length);
        for (int i = 0; i < count; i++) {
            int idx = dims[i];
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("dimension", idx);
            row.put("sigma", sigma[idx]);
            rows.add(row);
        }
        return rows;
    }

    private static double gaussianKlDiag(double[] currentMean,
                                         double[] currentSigma,
                                         double[] previousMean,
                                         double[] previousSigma) {
        double kl = 0.0;
        for (int i = 0; i < currentMean.length; i++) {
            double s0 = Math.max(1.0e-12, currentSigma[i]);
            double s1 = Math.max(1.0e-12, previousSigma[i]);
            double m0 = currentMean[i];
            double m1 = previousMean[i];
            kl += Math.log(s1 / s0)
                    + ((s0 * s0) + ((m0 - m1) * (m0 - m1))) / (2.0 * s1 * s1)
                    - 0.5;
        }
        return Math.max(0.0, kl);
    }

    private static double binaryEntropy(double p) {
        return entropyTerm(p) + entropyTerm(1.0 - p);
    }

    private static double entropyTerm(double p) {
        if (p <= 0.0 || p >= 1.0) {
            return 0.0;
        }
        return -p * (Math.log(p) / LOG_2);
    }

    private static double binaryKl(double[] current, double[] previous) {
        double kl = 0.0;
        for (int i = 0; i < current.length; i++) {
            double p = clamp(current[i], 1.0e-12, 1.0 - 1.0e-12);
            double q = clamp(previous[i], 1.0e-12, 1.0 - 1.0e-12);
            kl += p * Math.log(p / q) + (1.0 - p) * Math.log((1.0 - p) / (1.0 - q));
        }
        return Math.max(0.0, kl);
    }

    private static double l1Distance(double[] left, double[] right) {
        double sum = 0.0;
        for (int i = 0; i < left.length; i++) {
            sum += Math.abs(left[i] - right[i]);
        }
        return sum;
    }

    private static double l2Distance(double[] left, double[] right) {
        double sum = 0.0;
        for (int i = 0; i < left.length; i++) {
            double diff = left[i] - right[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private static List<Double> toList(double[] values) {
        List<Double> list = new ArrayList<>(values.length);
        for (double value : values) {
            list.add(value);
        }
        return list;
    }

    private static List<Integer> toIntList(int[] values) {
        List<Integer> list = new ArrayList<>(values.length);
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    private static List<List<Double>> toNestedList(double[][] matrix) {
        List<List<Double>> rows = new ArrayList<>(matrix.length);
        for (double[] row : matrix) {
            rows.add(toList(row));
        }
        return rows;
    }

    private static double asDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double[] toDoubleArray(Object value) {
        if (!(value instanceof List<?> list)) {
            return null;
        }
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (!(element instanceof Number number)) {
                return null;
            }
            result[i] = number.doubleValue();
        }
        return result;
    }

    private static int[] toIntArray(Object value) {
        if (!(value instanceof List<?> list)) {
            return null;
        }
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (!(element instanceof Number number)) {
                return null;
            }
            result[i] = number.intValue();
        }
        return result;
    }

    private static Map<String, Double> toDoubleMap(Object value) {
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> converted = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || !(entry.getValue() instanceof Number number)) {
                continue;
            }
            converted.put(String.valueOf(entry.getKey()), number.doubleValue());
        }
        return converted;
    }

    private static boolean[] extractBooleanArray(Object source, String methodName) {
        Object value = invokeNoArg(source, methodName);
        if (!(value instanceof boolean[] raw)) {
            return null;
        }
        return Arrays.copyOf(raw, raw.length);
    }

    private static int[] extractIntArray(Object source, String methodName) {
        Object value = invokeNoArg(source, methodName);
        if (!(value instanceof int[] raw)) {
            return null;
        }
        return Arrays.copyOf(raw, raw.length);
    }

    private static double[] extractDoubleArray(Object source, String methodName) {
        Object value = invokeNoArg(source, methodName);
        if (!(value instanceof double[] raw)) {
            return null;
        }
        return Arrays.copyOf(raw, raw.length);
    }

    private static double[][] extractDoubleMatrix(Object source, String methodName) {
        Object value = invokeNoArg(source, methodName);
        if (!(value instanceof double[][] matrix)) {
            return null;
        }
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private static Object invokeNoArg(Object source, String methodName) {
        if (source == null) {
            return null;
        }
        try {
            Method method = source.getClass().getMethod(methodName);
            return method.invoke(source);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Summary tuple for dependency analysis.
     */
    private record DependencySummary(List<Map<String, Object>> edges,
                                     List<List<Integer>> clusters,
                                     double maxWeight) {
    }

    /**
     * Small union-find helper for dependency cluster extraction.
     */
    private static final class UnionFind {
        private final int[] parent;
        private final int[] rank;

        private UnionFind(int size) {
            this.parent = new int[size];
            this.rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        private int find(int node) {
            if (parent[node] != node) {
                parent[node] = find(parent[node]);
            }
            return parent[node];
        }

        private void union(int left, int right) {
            int rootLeft = find(left);
            int rootRight = find(right);
            if (rootLeft == rootRight) {
                return;
            }
            if (rank[rootLeft] < rank[rootRight]) {
                parent[rootLeft] = rootRight;
            } else if (rank[rootLeft] > rank[rootRight]) {
                parent[rootRight] = rootLeft;
            } else {
                parent[rootRight] = rootLeft;
                rank[rootLeft]++;
            }
        }
    }
}
