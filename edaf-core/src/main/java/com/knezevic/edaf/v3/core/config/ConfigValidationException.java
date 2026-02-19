package com.knezevic.edaf.v3.core.config;

import com.knezevic.edaf.v3.core.errors.ConfigurationException;

import java.util.List;

/**
 * Structured validation exception for user-facing config errors.
 */
public final class ConfigValidationException extends ConfigurationException {

    private final List<ConfigIssue> issues;

    public ConfigValidationException(String file, List<ConfigIssue> issues) {
        super(buildMessage(file, issues));
        this.issues = List.copyOf(issues);
    }

    public List<ConfigIssue> issues() {
        return issues;
    }

    private static String buildMessage(String file, List<ConfigIssue> issues) {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration validation failed for '").append(file).append("':\n");
        for (ConfigIssue issue : issues) {
            sb.append("  - ").append(issue.path()).append(": ").append(issue.message());
            if (issue.hint() != null && !issue.hint().isBlank()) {
                sb.append(" (hint: ").append(issue.hint()).append(')');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
