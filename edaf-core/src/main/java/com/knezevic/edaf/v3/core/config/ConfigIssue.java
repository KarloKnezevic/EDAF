/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.config;

/**
 * One validation issue with path, message, and optional remediation hint.
 */
public record ConfigIssue(String path, String message, String hint) {
}
