/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.plugins;

/**
 * Base plugin metadata contract.
 */
public interface Plugin {

    /**
     * Stable type id used in config (e.g. umda, bitstring, gaussian-diag).
     */
    String type();

    /**
     * Human-readable plugin description shown in `edaf list`.
     */
    String description();
}
