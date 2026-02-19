package com.knezevic.edaf.persistence.api;

import java.time.Instant;

/**
 * A snapshot of statistics for a single generation.
 *
 * @param generation          generation number (1-based)
 * @param bestFitness         best fitness in the generation
 * @param worstFitness        worst fitness in the generation
 * @param avgFitness          average fitness in the generation
 * @param stdFitness          standard deviation of fitness in the generation
 * @param bestIndividualJson  JSON representation of the best individual's genotype
 * @param evalDurationNanos   time spent on fitness evaluation in this generation (nanoseconds)
 * @param recordedAt          timestamp when this record was captured
 */
public record GenerationRecord(
    int generation,
    double bestFitness,
    double worstFitness,
    double avgFitness,
    double stdFitness,
    String bestIndividualJson,
    long evalDurationNanos,
    Instant recordedAt
) {}
