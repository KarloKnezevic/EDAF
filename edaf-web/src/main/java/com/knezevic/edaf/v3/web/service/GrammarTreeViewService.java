/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.persistence.query.RunDetail;
import com.knezevic.edaf.v3.repr.grammar.GrammarTreeEngine;
import com.knezevic.edaf.v3.repr.types.BitString;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds tree-visualization payload for grammar-based runs.
 */
@Service
public final class GrammarTreeViewService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns tree payload when run uses grammar representation and best genotype is available.
     */
    @SuppressWarnings("unchecked")
    public Optional<GrammarTreeView> view(RunDetail detail) {
        if (detail == null) {
            return Optional.empty();
        }

        Map<String, Object> config = parseMap(detail.configJson());
        String representationType = asString(path(config, "representation.type"), detail.representationType());
        Map<String, Object> artifacts = parseMap(detail.artifactsJson());

        if (!"grammar-bitstring".equalsIgnoreCase(representationType)
                && !config.containsKey("grammar")
                && !artifacts.containsKey("bestAstJson")) {
            return Optional.empty();
        }

        // Fast-path: already persisted in DB artifacts.
        String infix = asString(artifacts.get("bestExpressionInfix"), detail.bestSummary());
        String prefix = asString(artifacts.get("bestExpressionPrefix"), "");
        String latex = asString(artifacts.get("bestExpressionLatex"), "");
        String dot = asString(artifacts.get("bestExpressionDot"), "");
        String astJson = asString(artifacts.get("bestAstJson"), "");

        Map<String, Object> ast = parseMap(astJson);
        if (ast.isEmpty()) {
            ast = toMap(artifacts.get("bestAstJson"));
        }
        if (!ast.isEmpty() && !infix.isBlank()) {
            return Optional.of(new GrammarTreeView(
                    detail.runId(),
                    representationType,
                    infix,
                    prefix,
                    latex,
                    dot,
                    ast,
                    toMap(artifacts.get("bestTreeMetrics")),
                    toIntList(artifacts.get("decisionVector")),
                    toDoubleList(artifacts.get("ercValues")),
                    "artifacts"
            ));
        }

        String genotype = asString(artifacts.get("bestGenotype"), null);
        if (genotype == null || genotype.isBlank() || !genotype.chars().allMatch(ch -> ch == '0' || ch == '1')) {
            return Optional.empty();
        }

        try {
            Map<String, Object> params = new LinkedHashMap<>(pathAsMap(config, "problem.params"));
            Map<String, Object> grammar = pathAsMap(config, "grammar");
            if (!grammar.isEmpty()) {
                params.put("grammar", grammar);
            }

            GrammarTreeEngine engine = new GrammarTreeEngine(params);
            boolean[] genes = new boolean[genotype.length()];
            for (int i = 0; i < genotype.length(); i++) {
                genes[i] = genotype.charAt(i) == '1';
            }
            GrammarTreeEngine.TreeInspection inspection = engine.inspect(new BitString(genes));
            return Optional.of(new GrammarTreeView(
                    detail.runId(),
                    representationType,
                    inspection.infix(),
                    inspection.prefix(),
                    inspection.latex(),
                    inspection.dot(),
                    inspection.ast(),
                    mapper.convertValue(inspection.metrics(), MAP_TYPE),
                    inspection.decisionVector(),
                    inspection.ercValues(),
                    "decoded"
            ));
        } catch (Exception e) {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof String text) {
            return parseMap(text);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, nested) -> {
                if (key != null) {
                    copy.put(String.valueOf(key), nested);
                }
            });
            return copy;
        }
        return Map.of();
    }

    private static String asString(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pathAsMap(Map<String, Object> root, String dotted) {
        Object value = path(root, dotted);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((k, v) -> {
                if (k != null) {
                    copy.put(String.valueOf(k), v);
                }
            });
            return copy;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
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

    private static List<Integer> toIntList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Integer> result = new java.util.ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Number number) {
                result.add(number.intValue());
            } else {
                try {
                    result.add(Integer.parseInt(String.valueOf(item)));
                } catch (Exception ignored) {
                    // skip malformed entry
                }
            }
        }
        return result;
    }

    private static List<Double> toDoubleList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Double> result = new java.util.ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Number number) {
                result.add(number.doubleValue());
            } else {
                try {
                    result.add(Double.parseDouble(String.valueOf(item)));
                } catch (Exception ignored) {
                    // skip malformed entry
                }
            }
        }
        return result;
    }

    /**
     * Tree visualization payload returned by API.
     */
    public record GrammarTreeView(
            String runId,
            String representationType,
            String infix,
            String prefix,
            String latex,
            String dot,
            Map<String, Object> ast,
            Map<String, Object> metrics,
            List<Integer> decisionVector,
            List<Double> ercValues,
            String source
    ) {
    }
}
