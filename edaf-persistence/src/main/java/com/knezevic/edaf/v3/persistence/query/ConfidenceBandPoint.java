/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

/**
 * One convergence point with mean and 95% confidence interval bounds.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ConfidenceBandPoint(
        double x,
        double mean,
        double ciLower,
        double ciUpper,
        Double median,
        long samples
) {
}
