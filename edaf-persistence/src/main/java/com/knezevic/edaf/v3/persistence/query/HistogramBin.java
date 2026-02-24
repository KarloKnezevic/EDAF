/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Histogram bin used for time-to-target distributions.
 */
public record HistogramBin(
        double startInclusive,
        double endExclusive,
        long count
) {
}
