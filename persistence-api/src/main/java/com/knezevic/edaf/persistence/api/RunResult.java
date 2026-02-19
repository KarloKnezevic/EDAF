package com.knezevic.edaf.persistence.api;

import java.time.Instant;

/**
 * Summary captured when an algorithm run completes.
 *
 * @param totalGenerations    number of generations executed
 * @param bestFitness         best fitness achieved across all generations
 * @param bestIndividualJson  JSON representation of the overall best individual's genotype
 * @param totalDurationMillis total wall-clock duration of the run in milliseconds
 * @param completedAt         timestamp when the run completed
 */
public record RunResult(
    int totalGenerations,
    double bestFitness,
    String bestIndividualJson,
    long totalDurationMillis,
    Instant completedAt
) {}
