package com.knezevic.edaf.v3.coco.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Loads and validates COCO campaign configuration documents.
 */
public final class CocoConfigLoader {

    private final ObjectMapper mapper;
    private final Validator validator;

    public CocoConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            this.validator = validatorFactory.getValidator();
        }
    }

    /**
     * Loads one COCO campaign YAML file and validates structure and semantics.
     */
    public CocoCampaignConfig load(Path path) {
        CocoCampaignConfig config;
        try {
            config = mapper.readValue(Files.newBufferedReader(path), CocoCampaignConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read COCO config '" + path + "'", e);
        }

        List<String> issues = new ArrayList<>();
        for (ConstraintViolation<CocoCampaignConfig> violation : validator.validate(config)) {
            issues.add(violation.getPropertyPath() + " " + violation.getMessage());
        }
        issues.addAll(semanticIssues(config));

        if (!issues.isEmpty()) {
            throw new CocoConfigValidationException(path, issues);
        }
        return config;
    }

    private static List<String> semanticIssues(CocoCampaignConfig config) {
        List<String> issues = new ArrayList<>();

        String schema = normalize(config.getSchema());
        if (!schema.startsWith("3.")) {
            issues.add("schema must start with '3.' (use '3.0-coco')");
        }

        String suite = normalize(config.getCampaign().getSuite());
        if (!"bbob".equals(suite)) {
            issues.add("campaign.suite must be 'bbob' in current implementation");
        }

        for (Integer functionId : config.getCampaign().getFunctions()) {
            if (functionId == null || functionId < 1 || functionId > 24) {
                issues.add("campaign.functions must contain IDs in [1..24]; found: " + functionId);
            }
        }
        for (Integer dimension : config.getCampaign().getDimensions()) {
            if (dimension == null || dimension < 2) {
                issues.add("campaign.dimensions values must be >= 2; found: " + dimension);
            }
        }
        for (Integer instance : config.getCampaign().getInstances()) {
            if (instance == null || instance < 1) {
                issues.add("campaign.instances values must be >= 1; found: " + instance);
            }
        }

        Set<String> seenOptimizerIds = new HashSet<>();
        for (CocoCampaignConfig.OptimizerSection optimizer : config.getOptimizers()) {
            String id = normalize(optimizer.getId());
            if (!seenOptimizerIds.add(id)) {
                issues.add("Duplicate optimizers[].id: " + optimizer.getId());
            }
        }

        String referenceMode = normalize(config.getCampaign().getReferenceMode());
        if (!referenceMode.equals("best-online") && !referenceMode.startsWith("optimizer:")) {
            issues.add("campaign.referenceMode must be 'best-online' or 'optimizer:<name>'");
        }

        return issues;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
