/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured model diagnostics captured per iteration and persisted by sinks.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record ModelDiagnostics(Map<String, Double> numeric) {

    /**
     * Creates immutable diagnostics record from numeric metric map.
     *
     * @param numeric numeric diagnostics map
     */
    public ModelDiagnostics {
        numeric = Collections.unmodifiableMap(new LinkedHashMap<>(numeric));
    }

    /**
     * Returns an empty diagnostics instance.
     *
     * @return empty diagnostics
     */
    public static ModelDiagnostics empty() {
        return new ModelDiagnostics(Map.of());
    }
}
