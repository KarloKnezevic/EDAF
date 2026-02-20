package com.knezevic.edaf.v3.persistence.query;

/**
 * Five-number summary and moments used for box-plot and distribution cards.
 */
public record BoxPlotStats(
        Double min,
        Double q1,
        Double median,
        Double q3,
        Double max,
        Double mean,
        Double stdDev
) {
}
