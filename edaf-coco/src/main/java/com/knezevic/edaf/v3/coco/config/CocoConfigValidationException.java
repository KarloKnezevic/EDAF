package com.knezevic.edaf.v3.coco.config;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when COCO campaign config fails schema or semantic validation.
 */
public final class CocoConfigValidationException extends RuntimeException {

    private final Path path;
    private final List<String> issues;

    public CocoConfigValidationException(Path path, List<String> issues) {
        super("Invalid COCO config '" + path + "':\n" + issues.stream().map(s -> " - " + s).collect(Collectors.joining("\n")));
        this.path = path;
        this.issues = List.copyOf(issues);
    }

    public Path path() {
        return path;
    }

    public List<String> issues() {
        return issues;
    }
}
