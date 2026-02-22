package com.knezevic.edaf.v3.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.persistence.query.CheckpointRow;
import com.knezevic.edaf.v3.persistence.query.EventRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentParamRow;
import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.PageResult;
import com.knezevic.edaf.v3.persistence.query.RunDetail;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Reads completed-run artifacts from filesystem for UI/API fallback when DB entries are unavailable.
 */
@Service
public final class RunArtifactService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<RunDetail> loadRunDetail(String runId) {
        return locateRunDirectory(runId).flatMap(dir -> {
            try {
                Map<String, Object> summary = readSummary(dir);
                String configJson = readIfExists(dir.resolve("config-resolved.json"));
                String configYaml = readIfExists(dir.resolve("config-resolved.yaml"));
                Map<String, Object> config = parseMap(configJson);

                String algorithm = asString(summary.get("algorithm"), asString(path(config, "algorithm.type"), "unknown"));
                String model = asString(summary.get("model"), asString(path(config, "model.type"), "unknown"));
                String problem = asString(summary.get("problem"), asString(path(config, "problem.type"), "unknown"));
                String representation = asString(path(config, "representation.type"), "unknown");
                String selection = asString(path(config, "selection.type"), "unknown");
                String replacement = asString(path(config, "replacement.type"), "unknown");
                String stopping = asString(path(config, "stopping.type"), "unknown");

                String startedAt = asString(summary.get("startedAt"), null);
                String endedAt = asString(summary.get("endedAt"), null);
                Long runtimeMillis = asLong(summary.get("runtimeMillis"));
                Integer iterations = asInteger(summary.get("iterations"));
                Long evaluations = asLong(summary.get("evaluations"));
                Double bestFitness = asDouble(summary.get("bestFitness"));
                String status = asString(summary.get("status"), "UNKNOWN");
                long seed = asLong(path(config, "run.masterSeed"), 0L);
                String schema = asString(path(config, "schema"), "3.0");
                String runName = asString(path(config, "run.name"), "Artifact run");
                Integer maxIterations = asInteger(path(config, "stopping.maxIterations"));

                String runDetailId = asString(summary.get("runId"), runId);
                String configHash = sha256(configJson);
                String artifactsJson = mapper.writeValueAsString(summary.getOrDefault("artifactPaths", Map.of()));

                return Optional.of(new RunDetail(
                        runDetailId,
                        "artifact-" + runDetailId,
                        configHash,
                        schema,
                        runName,
                        algorithm,
                        model,
                        problem,
                        representation,
                        selection,
                        replacement,
                        stopping,
                        maxIterations,
                        status,
                        seed,
                        startedAt,
                        endedAt,
                        iterations,
                        evaluations,
                        bestFitness,
                        asString(summary.get("bestSummary"), null),
                        runtimeMillis,
                        artifactsJson,
                        null,
                        asString(summary.get("errorMessage"), null),
                        configYaml,
                        configJson,
                        startedAt == null ? Instant.now().toString() : startedAt
                ));
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    public List<IterationMetric> loadIterations(String runId) {
        return locateRunDirectory(runId)
                .map(dir -> readJsonLines(dir.resolve("telemetry.jsonl")))
                .orElseGet(List::of)
                .stream()
                .map(row -> {
                    String createdAt = asString(row.get("timestamp"), Instant.now().toString());
                    int iteration = asInteger(row.get("generation"), 0);
                    long evaluations = asLong(row.get("evaluations"), 0L);
                    double best = asDouble(row.get("bestFitness"), 0.0);
                    double mean = asDouble(row.get("meanFitness"), 0.0);
                    double std = asDouble(row.get("stdFitness"), 0.0);

                    Map<String, Object> diagnostics = new LinkedHashMap<>();
                    diagnostics.put("populationSize", asInteger(row.get("populationSize"), 0));
                    diagnostics.put("eliteSize", asInteger(row.get("eliteSize"), 0));
                    diagnostics.put("latentTelemetry", row.getOrDefault("latentTelemetry", Map.of()));
                    diagnostics.put("adaptiveActions", row.getOrDefault("adaptiveActions", List.of()));

                    try {
                        String metricsJson = mapper.writeValueAsString(row.getOrDefault("metrics", Map.of()));
                        String diagnosticsJson = mapper.writeValueAsString(diagnostics);
                        return new IterationMetric(iteration, evaluations, best, mean, std, metricsJson, diagnosticsJson, createdAt);
                    } catch (Exception e) {
                        return new IterationMetric(iteration, evaluations, best, mean, std, "{}", "{}", createdAt);
                    }
                })
                .sorted(Comparator.comparingInt(IterationMetric::iteration))
                .toList();
    }

    public PageResult<EventRow> loadEvents(String runId, String eventType, String q, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(200, size));

        List<EventRow> all = locateRunDirectory(runId)
                .map(dir -> readJsonLines(dir.resolve("events.jsonl")))
                .orElseGet(List::of)
                .stream()
                .map(row -> {
                    String type = asString(row.get("type"), "unknown");
                    String createdAt = asString(row.get("timestamp"), Instant.now().toString());
                    Object payload = row.get("payload");
                    String payloadJson;
                    try {
                        payloadJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
                    } catch (Exception e) {
                        payloadJson = String.valueOf(payload);
                    }
                    long id = Math.abs((createdAt + type).hashCode());
                    return new EventRow(id, runId, type, payloadJson, createdAt);
                })
                .filter(row -> eventType == null || eventType.isBlank()
                        || row.eventType().equalsIgnoreCase(eventType))
                .filter(row -> {
                    if (q == null || q.isBlank()) {
                        return true;
                    }
                    String needle = q.toLowerCase(Locale.ROOT);
                    return row.eventType().toLowerCase(Locale.ROOT).contains(needle)
                            || row.payloadJson().toLowerCase(Locale.ROOT).contains(needle);
                })
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();

        int from = Math.min(all.size(), safePage * safeSize);
        int to = Math.min(all.size(), from + safeSize);
        List<EventRow> items = all.subList(from, to);
        long total = all.size();
        long totalPages = total == 0 ? 0 : ((total + safeSize - 1) / safeSize);
        return new PageResult<>(items, safePage, safeSize, total, totalPages);
    }

    public List<CheckpointRow> loadCheckpoints(String runId) {
        return List.of();
    }

    public List<ExperimentParamRow> loadParams(String runId) {
        return locateRunDirectory(runId)
                .map(dir -> parseMap(readIfExists(dir.resolve("config-resolved.json"))))
                .map(this::flattenConfig)
                .orElseGet(List::of);
    }

    private List<ExperimentParamRow> flattenConfig(Map<String, Object> config) {
        List<ExperimentParamRow> rows = new ArrayList<>();
        if (config == null || config.isEmpty()) {
            return rows;
        }
        long[] id = {1};
        config.forEach((section, value) -> flattenRecursive("artifact", section, section, value, rows, id));
        rows.sort(Comparator.comparing(ExperimentParamRow::section).thenComparing(ExperimentParamRow::paramPath));
        return rows;
    }

    @SuppressWarnings("unchecked")
    private void flattenRecursive(String experimentId,
                                  String section,
                                  String path,
                                  Object value,
                                  List<ExperimentParamRow> out,
                                  long[] id) {
        String leaf = leaf(path);
        if (value == null) {
            out.add(new ExperimentParamRow(id[0]++, experimentId, section, path, leaf, "null", null, null, null, null));
            return;
        }

        if (value instanceof Map<?, ?> map) {
            String json = toJson(map);
            out.add(new ExperimentParamRow(id[0]++, experimentId, section, path, leaf, "json", null, null, null, json));
            map.forEach((key, nested) -> {
                if (key != null) {
                    flattenRecursive(experimentId, section, path + "." + key, nested, out, id);
                }
            });
            return;
        }

        if (value instanceof List<?> list) {
            out.add(new ExperimentParamRow(id[0]++, experimentId, section, path, leaf, "json", null, null, null, toJson(list)));
            for (int i = 0; i < list.size(); i++) {
                flattenRecursive(experimentId, section, path + "[" + i + "]", list.get(i), out, id);
            }
            return;
        }

        if (value instanceof Number number) {
            out.add(new ExperimentParamRow(
                    id[0]++, experimentId, section, path, leaf, "number",
                    number.toString(), number.doubleValue(), null, null
            ));
            return;
        }

        if (value instanceof Boolean bool) {
            out.add(new ExperimentParamRow(
                    id[0]++, experimentId, section, path, leaf, "boolean",
                    bool.toString(), null, bool ? 1 : 0, null
            ));
            return;
        }

        out.add(new ExperimentParamRow(id[0]++, experimentId, section, path, leaf, "string", String.valueOf(value), null, null, null));
    }

    private String leaf(String path) {
        int dot = path.lastIndexOf('.');
        int bracket = path.lastIndexOf('[');
        int index = Math.max(dot, bracket);
        return index < 0 ? path : path.substring(index + 1).replace("]", "");
    }

    private Map<String, Object> readSummary(Path runDir) {
        return parseMap(readIfExists(runDir.resolve("summary.json")));
    }

    private List<Map<String, Object>> readJsonLines(Path path) {
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                rows.add(mapper.readValue(line, MAP_TYPE));
            }
            return rows;
        } catch (IOException e) {
            return List.of();
        }
    }

    private Optional<Path> locateRunDirectory(String runId) {
        Path direct = Path.of("results", "runs", runId);
        if (Files.isDirectory(direct)) {
            return Optional.of(direct);
        }

        Path root = Path.of("results");
        if (!Files.exists(root)) {
            return Optional.empty();
        }

        try (var paths = Files.find(root, 5, (path, attrs) -> attrs.isDirectory()
                && path.getFileName().toString().equals(runId)
                && path.getParent() != null
                && path.getParent().getFileName() != null
                && path.getParent().getFileName().toString().equals("runs"))) {
            return paths.findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Map<String, Object> parseMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return mapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static Object path(Map<String, Object> root, String dotted) {
        if (root == null) {
            return null;
        }
        Object current = root;
        for (String part : dotted.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    private String readIfExists(Path path) {
        if (!Files.exists(path)) {
            return "";
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private static String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static Integer asInteger(Object value) {
        return asInteger(value, null);
    }

    private static Integer asInteger(Object value, Integer fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Long asLong(Object value) {
        return asLong(value, null);
    }

    private static Long asLong(Object value, Long fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Double asDouble(Object value) {
        return asDouble(value, null);
    }

    private static Double asDouble(Object value, Double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String sha256(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
