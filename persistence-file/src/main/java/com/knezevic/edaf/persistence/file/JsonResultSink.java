package com.knezevic.edaf.persistence.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.knezevic.edaf.persistence.api.GenerationRecord;
import com.knezevic.edaf.persistence.api.ResultSink;
import com.knezevic.edaf.persistence.api.RunMetadata;
import com.knezevic.edaf.persistence.api.RunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes algorithm run results as a single JSON file per run.
 * <p>
 * Output file: {@code <directory>/run-<runId>.json}
 * </p>
 */
public class JsonResultSink implements ResultSink {

    private static final Logger log = LoggerFactory.getLogger(JsonResultSink.class);

    private final Path directory;
    private final ObjectMapper mapper;

    private RunMetadata metadata;
    private final List<Map<String, Object>> generations = new ArrayList<>();

    public JsonResultSink(Path directory) {
        this.directory = directory;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void onRunStarted(RunMetadata metadata) {
        this.metadata = metadata;
        this.generations.clear();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            log.error("Failed to create output directory: {}", directory, e);
        }
    }

    @Override
    public void onGenerationCompleted(String runId, GenerationRecord record) {
        Map<String, Object> genMap = new LinkedHashMap<>();
        genMap.put("generation", record.generation());
        genMap.put("bestFitness", record.bestFitness());
        genMap.put("worstFitness", record.worstFitness());
        genMap.put("avgFitness", record.avgFitness());
        genMap.put("stdFitness", record.stdFitness());
        genMap.put("bestIndividual", record.bestIndividualJson());
        genMap.put("evalDurationNanos", record.evalDurationNanos());
        genMap.put("recordedAt", record.recordedAt().toString());
        generations.add(genMap);
    }

    @Override
    public void onRunCompleted(String runId, RunResult result) {
        Map<String, Object> root = new LinkedHashMap<>();

        // Metadata section
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("runId", metadata.runId());
        meta.put("algorithmId", metadata.algorithmId());
        meta.put("problemClass", metadata.problemClass());
        meta.put("genotypeType", metadata.genotypeType());
        meta.put("populationSize", metadata.populationSize());
        meta.put("maxGenerations", metadata.maxGenerations());
        meta.put("seed", metadata.seed());
        meta.put("startedAt", metadata.startedAt().toString());
        root.put("metadata", meta);

        // Result section
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalGenerations", result.totalGenerations());
        res.put("bestFitness", result.bestFitness());
        res.put("bestIndividual", result.bestIndividualJson());
        res.put("totalDurationMillis", result.totalDurationMillis());
        res.put("completedAt", result.completedAt().toString());
        root.put("result", res);

        // Generations
        root.put("generations", generations);

        Path file = directory.resolve("run-" + runId + ".json");
        try {
            mapper.writeValue(file.toFile(), root);
            log.info("Run results written to {}", file);
        } catch (IOException e) {
            log.error("Failed to write JSON results to {}", file, e);
        }
    }
}
