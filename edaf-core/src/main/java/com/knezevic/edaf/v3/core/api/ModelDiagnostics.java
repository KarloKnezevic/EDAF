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
 */
public record ModelDiagnostics(Map<String, Double> numeric) {

    public ModelDiagnostics {
        numeric = Collections.unmodifiableMap(new LinkedHashMap<>(numeric));
    }

    public static ModelDiagnostics empty() {
        return new ModelDiagnostics(Map.of());
    }
}
