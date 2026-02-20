package com.knezevic.edaf.v3.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.knezevic.edaf.v3.core.errors.ConfigurationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Loads and validates EDAF v3 YAML configurations.
 */
public final class ConfigLoader {

    private final ObjectMapper mapper;
    private final Validator validator;
    private final ExperimentConfigValidator semanticValidator;

    public ConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            this.validator = validatorFactory.getValidator();
        }
        this.semanticValidator = new ExperimentConfigValidator();
    }

    /**
     * Detects whether the YAML document represents a single experiment config or a batch config.
     */
    public ConfigDocumentType detectType(Path path) {
        try {
            JsonNode root = mapper.readTree(Files.newBufferedReader(path));
            if (root != null && root.isObject() && root.has("experiments") && !root.has("schema")) {
                return ConfigDocumentType.BATCH;
            }
            return ConfigDocumentType.EXPERIMENT;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read config file '" + path + "'", e);
        }
    }

    /**
     * Loads and validates one experiment configuration.
     */
    public ConfigLoadResult load(Path path) {
        ExperimentConfig config = decode(path);
        validate(path, config);
        return new ConfigLoadResult(config, List.of());
    }

    /**
     * Loads batch configuration for `edaf batch` command.
     */
    public BatchConfig loadBatch(Path path) {
        try {
            JsonNode root = mapper.readTree(Files.newBufferedReader(path));
            BatchConfig config = parseBatch(root);
            Set<ConstraintViolation<BatchConfig>> violations = validator.validate(config);
            if (!violations.isEmpty()) {
                List<ConfigIssue> issues = new ArrayList<>();
                for (ConstraintViolation<BatchConfig> violation : violations) {
                    issues.add(new ConfigIssue("batch." + violation.getPropertyPath(), violation.getMessage(), null));
                }
                throw new ConfigValidationException(path.toString(), issues);
            }
            return config;
        } catch (ConfigValidationException e) {
            throw e;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read batch config '" + path + "'", e);
        }
    }

    private BatchConfig parseBatch(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new ConfigurationException("Batch config must be a YAML object");
        }
        enforceAllowedBatchRootFields(root);
        JsonNode experimentsNode = root.path("experiments");
        if (!experimentsNode.isArray()) {
            throw new ConfigurationException("batch.experiments must be a YAML array");
        }

        int defaultRepetitions = root.path("defaultRepetitions").asInt(1);
        if (defaultRepetitions < 1) {
            throw new ConfigurationException("batch.defaultRepetitions must be >= 1");
        }
        Long defaultSeedStart = root.has("defaultSeedStart")
                ? root.path("defaultSeedStart").asLong()
                : null;

        List<BatchConfig.BatchExperimentEntry> entries = new ArrayList<>();
        for (int index = 0; index < experimentsNode.size(); index++) {
            JsonNode node = experimentsNode.get(index);
            if (node.isTextual()) {
                entries.add(new BatchConfig.BatchExperimentEntry(
                        node.asText(),
                        defaultRepetitions,
                        defaultSeedStart,
                        null
                ));
                continue;
            }

            if (!node.isObject()) {
                throw new ConfigurationException("batch.experiments[" + index + "] must be string or object");
            }
            enforceAllowedBatchEntryFields(node, index);

            String configPath = textOrNull(node, "config");
            if (configPath == null) {
                configPath = textOrNull(node, "path");
            }
            if (configPath == null) {
                throw new ConfigurationException("batch.experiments[" + index + "].config is required");
            }

            int repetitions = node.has("repetitions")
                    ? node.path("repetitions").asInt(defaultRepetitions)
                    : defaultRepetitions;
            if (repetitions < 1) {
                throw new ConfigurationException("batch.experiments[" + index + "].repetitions must be >= 1");
            }
            Long seedStart = node.has("seedStart")
                    ? node.path("seedStart").asLong()
                    : defaultSeedStart;
            String runIdPrefix = textOrNull(node, "runIdPrefix");

            entries.add(new BatchConfig.BatchExperimentEntry(
                    configPath,
                    repetitions,
                    seedStart,
                    runIdPrefix
            ));
        }

        BatchConfig config = new BatchConfig();
        config.setDefaultRepetitions(defaultRepetitions);
        config.setDefaultSeedStart(defaultSeedStart);
        config.setExperiments(entries);
        return config;
    }

    private static void enforceAllowedBatchRootFields(JsonNode root) {
        List<String> allowed = List.of("experiments", "defaultRepetitions", "defaultSeedStart");
        root.fieldNames().forEachRemaining(field -> {
            if (!allowed.contains(field)) {
                throw new ConfigurationException(
                        "Unknown batch property '" + field + "'. Allowed: " + allowed
                );
            }
        });
    }

    private static void enforceAllowedBatchEntryFields(JsonNode node, int index) {
        List<String> allowed = List.of("config", "path", "repetitions", "seedStart", "runIdPrefix");
        node.fieldNames().forEachRemaining(field -> {
            if (!allowed.contains(field)) {
                throw new ConfigurationException(
                        "Unknown batch.experiments[" + index + "] property '" + field + "'. Allowed: " + allowed
                );
            }
        });
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private ExperimentConfig decode(Path path) {
        try {
            return mapper.readValue(Files.newBufferedReader(path), ExperimentConfig.class);
        } catch (UnrecognizedPropertyException e) {
            List<ConfigIssue> issues = List.of(new ConfigIssue(
                    e.getPathReference(),
                    "Unknown property '" + e.getPropertyName() + "'",
                    "Check spelling or remove unsupported field"
            ));
            throw new ConfigValidationException(path.toString(), issues);
        } catch (ConfigValidationException e) {
            throw e;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read or parse config file '" + path + "'", e);
        }
    }

    private void validate(Path path, ExperimentConfig config) {
        List<ConfigIssue> issues = new ArrayList<>();

        Set<ConstraintViolation<ExperimentConfig>> violations = validator.validate(config);
        for (ConstraintViolation<ExperimentConfig> violation : violations) {
            issues.add(new ConfigIssue(
                    violation.getPropertyPath().toString(),
                    violation.getMessage(),
                    null
            ));
        }

        issues.addAll(semanticValidator.validate(config));

        if (!issues.isEmpty()) {
            throw new ConfigValidationException(path.toString(), issues);
        }
    }
}
