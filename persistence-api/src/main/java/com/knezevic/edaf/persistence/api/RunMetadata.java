package com.knezevic.edaf.persistence.api;

import java.time.Instant;

/**
 * Immutable metadata captured when an algorithm run starts.
 *
 * @param runId          unique identifier for this run (UUID)
 * @param algorithmId    algorithm name (e.g. "nes", "cem", "umda")
 * @param problemClass   fully-qualified problem class name
 * @param genotypeType   genotype type identifier (e.g. "binary", "fp")
 * @param populationSize configured population size
 * @param maxGenerations configured maximum generations
 * @param configHash     SHA-256 hash of the configuration for deduplication
 * @param seed           random seed used (null if not set)
 * @param startedAt      timestamp when the run started
 */
public record RunMetadata(
    String runId,
    String algorithmId,
    String problemClass,
    String genotypeType,
    int populationSize,
    int maxGenerations,
    String configHash,
    Long seed,
    Instant startedAt
) {}
