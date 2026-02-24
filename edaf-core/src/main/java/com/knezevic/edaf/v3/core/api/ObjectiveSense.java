/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

/**
 * Defines whether the optimization objective is a minimization or maximization problem.
 */
public enum ObjectiveSense {
    /** Smaller fitness values are better. */
    MINIMIZE,
    /** Larger fitness values are better. */
    MAXIMIZE
}
