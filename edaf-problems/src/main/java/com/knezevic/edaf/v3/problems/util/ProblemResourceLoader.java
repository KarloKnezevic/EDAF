package com.knezevic.edaf.v3.problems.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility loader resolving benchmark instances from classpath or filesystem.
 */
public final class ProblemResourceLoader {

    private ProblemResourceLoader() {
        // utility class
    }

    /**
     * Reads UTF-8 text content from either classpath resource (prefix {@code classpath:})
     * or local filesystem path.
     */
    public static String readText(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Resource location must not be blank");
        }

        if (location.startsWith("classpath:")) {
            String resourcePath = location.substring("classpath:".length());
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            try (InputStream stream = ProblemResourceLoader.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    throw new IllegalArgumentException("Classpath resource not found: " + location);
                }
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed reading classpath resource: " + location, e);
            }
        }

        try {
            return Files.readString(Path.of(location), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading file resource: " + location, e);
        }
    }

    /**
     * Reads UTF-8 lines from either classpath or filesystem location.
     */
    public static List<String> readLines(String location) {
        return readText(location).lines().toList();
    }
}
