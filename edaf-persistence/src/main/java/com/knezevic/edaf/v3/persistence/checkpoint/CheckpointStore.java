package com.knezevic.edaf.v3.persistence.checkpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Checkpoint storage utility using YAML payload files.
 */
public final class CheckpointStore {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Saves checkpoint payload as YAML file.
     */
    public void save(Path path, JsonNode payload) {
        try {
            Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), payload);
        } catch (IOException e) {
            throw new RuntimeException("Failed saving checkpoint to " + path, e);
        }
    }

    /**
     * Loads checkpoint payload from YAML file.
     */
    public JsonNode load(Path path) {
        try {
            return mapper.readTree(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed loading checkpoint from " + path, e);
        }
    }
}
