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
