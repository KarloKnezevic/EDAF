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
