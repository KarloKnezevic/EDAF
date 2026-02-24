/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.config;

import java.util.List;

/**
 * Result of loading config including validation warnings.
 */
public record ConfigLoadResult(ExperimentConfig config, List<String> warnings) {
}
