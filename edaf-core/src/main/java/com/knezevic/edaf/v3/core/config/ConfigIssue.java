package com.knezevic.edaf.v3.core.config;

/**
 * One validation issue with path, message, and optional remediation hint.
 */
public record ConfigIssue(String path, String message, String hint) {
}
