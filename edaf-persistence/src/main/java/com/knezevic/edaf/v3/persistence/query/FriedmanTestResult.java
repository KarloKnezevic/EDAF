/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Friedman omnibus test output for multiple algorithms.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record FriedmanTestResult(
        int blocks,
        int algorithms,
        Double statistic,
        Double pValue,
        List<FriedmanRank> ranks
) {
}
