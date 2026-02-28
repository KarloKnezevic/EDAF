/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.coco.config;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when COCO campaign config fails schema or semantic validation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CocoConfigValidationException extends RuntimeException {

    private final Path path;
    private final List<String> issues;

    /**
     * Creates a new CocoConfigValidationException instance.
     *
     * @param path filesystem path
     * @param issues the issues argument
     */
    public CocoConfigValidationException(Path path, List<String> issues) {
        super("Invalid COCO config '" + path + "':\n" + issues.stream().map(s -> " - " + s).collect(Collectors.joining("\n")));
        this.path = path;
        this.issues = List.copyOf(issues);
    }

    /**
     * Executes path.
     *
     * @return the path
     */
    public Path path() {
        return path;
    }

    /**
     * Checks whether sues.
     *
     * @return true if sues; otherwise false
     */
    public List<String> issues() {
        return issues;
    }
}
