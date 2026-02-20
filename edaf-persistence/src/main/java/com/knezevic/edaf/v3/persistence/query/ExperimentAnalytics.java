package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Aggregate metrics and profile curves computed over all runs of one experiment.
 */
public record ExperimentAnalytics(
        String experimentId,
        String objectiveDirection,
        Double targetFitness,
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
        List<ProfilePoint> dataProfile,
        List<ProfilePoint> performanceProfile
) {
}
