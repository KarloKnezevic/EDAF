/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Aggregate metrics and profile curves computed over all runs of one experiment.
 */
public record ExperimentAnalytics(
        String experimentId,
        String objectiveDirection,
        Double targetFitness,
        String targetSource,
        long totalRuns,
        long completedRuns,
        long successfulRuns,
        double successRate,
        Double ert,
        Double sp1,
        BoxPlotStats bestFitnessBox,
        List<Double> bestFitnessValues,
        BoxPlotStats runtimeMillisBox,
        BoxPlotStats evaluationsBox,
        List<ConfidenceBandPoint> convergence95Ci,
        List<ProfilePoint> successVsBudget,
        List<HistogramBin> timeToTargetHistogram,
        List<ProfilePoint> ecdfTotalRuns,
        List<ProfilePoint> ecdfSuccessfulRuns,
        List<ProfilePoint> dataProfile,
        List<ProfilePoint> performanceProfile
) {
}
