/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.cli.ui;

/**
 * Console verbosity levels for CLI output.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public enum Verbosity {
    QUIET,
    NORMAL,
    VERBOSE,
    DEBUG;

    /**
     * Executes from.
     *
     * @param value the value argument
     * @return the from
     */
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
