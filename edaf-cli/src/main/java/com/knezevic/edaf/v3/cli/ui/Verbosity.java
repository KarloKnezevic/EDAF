/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.cli.ui;

/**
 * Console verbosity levels for CLI output.
 */
public enum Verbosity {
    QUIET,
    NORMAL,
    VERBOSE,
    DEBUG;

    public static Verbosity from(String value) {
        if (value == null) {
            return NORMAL;
        }
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "quiet" -> QUIET;
            case "verbose" -> VERBOSE;
            case "debug" -> DEBUG;
            default -> NORMAL;
        };
    }
}
