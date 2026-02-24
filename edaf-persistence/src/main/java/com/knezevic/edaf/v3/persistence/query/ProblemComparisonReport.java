/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
