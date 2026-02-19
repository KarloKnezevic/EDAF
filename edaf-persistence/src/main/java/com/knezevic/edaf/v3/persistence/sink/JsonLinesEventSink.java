package com.knezevic.edaf.v3.persistence.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.RunEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * JSON Lines sink for machine-ingestible event streams.
 */
public final class JsonLinesEventSink implements EventSink {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public JsonLinesEventSink(Path file) {
        this.file = file;
    }

    @Override
    public synchronized void onEvent(RunEvent event) {
        try {
            Files.createDirectories(file.getParent() == null ? Path.of(".") : file.getParent());
            String json = mapper.writeValueAsString(event);
            Files.writeString(file, json + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing JSON lines sink", e);
        }
    }
}
