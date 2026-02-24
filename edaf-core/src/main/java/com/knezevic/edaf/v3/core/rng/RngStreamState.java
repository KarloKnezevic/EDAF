/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.rng;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable state snapshot for a single named RNG stream.
 */
public record RngStreamState(long state, boolean hasGaussian, double gaussian) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
