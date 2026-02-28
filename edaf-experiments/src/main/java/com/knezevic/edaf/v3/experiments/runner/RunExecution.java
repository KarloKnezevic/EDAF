/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.experiments.runner;

import com.knezevic.edaf.v3.core.api.RunResult;

import java.util.List;
import java.util.Map;

/**
 * Execution result envelope for CLI and automation consumers.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public record RunExecution(RunResult<?> result, Map<String, String> artifacts, List<String> warnings) {
}
