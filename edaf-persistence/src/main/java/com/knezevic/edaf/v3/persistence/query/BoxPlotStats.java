/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * Five-number summary and moments used for box-plot and distribution cards.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
