package com.knezevic.edaf.v3.persistence.query;

/**
 * One pairwise statistical comparison row.
 */
public record PairwiseTestResult(
        String algorithmA,
        String algorithmB,
        long sampleSizeA,
        long sampleSizeB,
        double pValue,
        double holmAdjustedPValue,
        String betterAlgorithm,
        boolean significantAt05
) {
}
