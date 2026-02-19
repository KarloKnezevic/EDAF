package com.knezevic.edaf.v3.experiments.runner;

import com.knezevic.edaf.v3.core.api.RunResult;

import java.util.List;
import java.util.Map;

/**
 * Execution result envelope for CLI and automation consumers.
 */
public record RunExecution(RunResult<?> result, Map<String, String> artifacts, List<String> warnings) {
}
