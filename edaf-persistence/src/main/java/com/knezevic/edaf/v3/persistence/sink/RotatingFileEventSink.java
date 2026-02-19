package com.knezevic.edaf.v3.persistence.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.RunEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Plain file sink with size-based rotation for structured event logs.
 */
public final class RotatingFileEventSink implements EventSink {

    private final Path file;
    private final long maxBytes;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public RotatingFileEventSink(Path file, long maxBytes) {
        this.file = file;
        this.maxBytes = Math.max(1024, maxBytes);
    }

    @Override
    public synchronized void onEvent(RunEvent event) {
        try {
            rotateIfNeeded();
            Files.createDirectories(file.getParent() == null ? Path.of(".") : file.getParent());
            String line = Instant.now() + " " + event.type() + " " + mapper.writeValueAsString(event) + "\n";
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing rotating file sink", e);
        }
    }

    private void rotateIfNeeded() throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        long size = Files.size(file);
        if (size < maxBytes) {
            return;
        }
        Path rotated = file.resolveSibling(file.getFileName() + "." + System.currentTimeMillis());
        Files.move(file, rotated);
    }
}
