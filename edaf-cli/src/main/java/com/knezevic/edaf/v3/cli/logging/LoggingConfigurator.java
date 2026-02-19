package com.knezevic.edaf.v3.cli.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies runtime log levels based on CLI verbosity.
 */
public final class LoggingConfigurator {

    private LoggingConfigurator() {
    }

    /**
     * Configures root and selected package log levels for the current process.
     *
     * @param verbosity user-selected verbosity
     */
    public static void apply(Verbosity verbosity) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }

        switch (verbosity) {
            case QUIET -> {
                set(context, Logger.ROOT_LOGGER_NAME, Level.ERROR);
                set(context, "com.knezevic.edaf.v3", Level.WARN);
                set(context, "com.zaxxer.hikari", Level.ERROR);
                set(context, "org.hibernate.validator", Level.ERROR);
                set(context, "org.jboss.logging", Level.ERROR);
            }
            case VERBOSE -> {
                set(context, Logger.ROOT_LOGGER_NAME, Level.INFO);
                set(context, "com.knezevic.edaf.v3", Level.INFO);
                set(context, "com.zaxxer.hikari", Level.WARN);
                set(context, "org.hibernate.validator", Level.WARN);
                set(context, "org.jboss.logging", Level.WARN);
            }
            case DEBUG -> {
                set(context, Logger.ROOT_LOGGER_NAME, Level.DEBUG);
                set(context, "com.knezevic.edaf.v3", Level.DEBUG);
                set(context, "com.zaxxer.hikari", Level.INFO);
                set(context, "org.hibernate.validator", Level.INFO);
                set(context, "org.jboss.logging", Level.INFO);
            }
            case NORMAL -> {
                set(context, Logger.ROOT_LOGGER_NAME, Level.WARN);
                set(context, "com.knezevic.edaf.v3", Level.INFO);
                set(context, "com.zaxxer.hikari", Level.WARN);
                set(context, "org.hibernate.validator", Level.WARN);
                set(context, "org.jboss.logging", Level.WARN);
            }
            default -> {
                set(context, Logger.ROOT_LOGGER_NAME, Level.WARN);
                set(context, "com.knezevic.edaf.v3", Level.INFO);
            }
        }
    }

    private static void set(LoggerContext context, String loggerName, Level level) {
        context.getLogger(loggerName).setLevel(level);
    }
}
