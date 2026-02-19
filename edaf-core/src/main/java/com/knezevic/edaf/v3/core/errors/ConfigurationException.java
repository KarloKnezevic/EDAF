package com.knezevic.edaf.v3.core.errors;

/**
 * Thrown when configuration parsing or validation fails.
 */
public class ConfigurationException extends EdafException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
