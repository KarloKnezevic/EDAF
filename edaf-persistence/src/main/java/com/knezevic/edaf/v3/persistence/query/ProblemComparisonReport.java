package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Cross-algorithm comparison payload for one problem family.
 */
public record ProblemComparisonReport(
        String problemType,
        String objectiveDirection,
        Double targetFitness,
        List<AlgorithmComparisonRow> algorithms,
        List<PairwiseTestResult> pairwiseWilcoxon,
        FriedmanTestResult friedman,
        List<ProfileSeries> dataProfiles,
        List<ProfileSeries> performanceProfiles
) {
}
