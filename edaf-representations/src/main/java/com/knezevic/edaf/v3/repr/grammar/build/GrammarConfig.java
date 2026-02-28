/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.build;

import com.knezevic.edaf.v3.core.util.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Canonical grammar configuration used by both auto and custom modes.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarConfig {

    private final String mode;
    private final String file;
    private final List<String> variables;
    private final List<String> binaryOps;
    private final List<String> unaryOps;
    private final List<String> ternaryOps;
    private final boolean allowConstants;
    private final List<Double> constants;
    private final boolean ephemeralConstants;
    private final String ephemeralDistribution;
    private final double ephemeralMin;
    private final double ephemeralMax;
    private final int maxDepth;
    private final boolean typed;
    private final boolean booleanMode;
    private final int bitsPerDecision;
    private final int bitsPerErc;
    private final int maxNodes;

    private GrammarConfig(String mode,
                          String file,
                          List<String> variables,
                          List<String> binaryOps,
                          List<String> unaryOps,
                          List<String> ternaryOps,
                          boolean allowConstants,
                          List<Double> constants,
                          boolean ephemeralConstants,
                          String ephemeralDistribution,
                          double ephemeralMin,
                          double ephemeralMax,
                          int maxDepth,
                          boolean typed,
                          boolean booleanMode,
                          int bitsPerDecision,
                          int bitsPerErc,
                          int maxNodes) {
        this.mode = mode;
        this.file = file;
        this.variables = List.copyOf(variables);
        this.binaryOps = List.copyOf(binaryOps);
        this.unaryOps = List.copyOf(unaryOps);
        this.ternaryOps = List.copyOf(ternaryOps);
        this.allowConstants = allowConstants;
        this.constants = List.copyOf(constants);
        this.ephemeralConstants = ephemeralConstants;
        this.ephemeralDistribution = ephemeralDistribution;
        this.ephemeralMin = Math.min(ephemeralMin, ephemeralMax);
        this.ephemeralMax = Math.max(ephemeralMin, ephemeralMax);
        this.maxDepth = maxDepth;
        this.typed = typed;
        this.booleanMode = booleanMode;
        this.bitsPerDecision = bitsPerDecision;
        this.bitsPerErc = bitsPerErc;
        this.maxNodes = maxNodes;
    }

    /**
     * Parses canonical grammar config from section params.
     *
     * <p>Accepts either direct keys or nested map under {@code grammar}.</p>
     * @param params configuration the input value map
     * @return parsed configuration
     */
    @SuppressWarnings("unchecked")
    public static GrammarConfig fromParams(Map<String, Object> params) {
        Map<String, Object> source = params == null ? Map.of() : params;
        if (source.containsKey("grammar") && source.get("grammar") instanceof Map<?, ?> nested) {
            source = (Map<String, Object>) nested;
        }

        String mode = normalizeMode(Params.str(source, "mode", "auto"));
        String file = Params.str(source, "file", null);

        List<String> variables = toStringList(source.get("variables"));
        if (variables.isEmpty()) {
            variables = List.of("x");
        }

        List<String> binaryOps = normalizeOps(toStringList(source.get("binary_ops")));
        if (binaryOps.isEmpty()) {
            binaryOps = List.of("+", "-", "*", "/", "pow", "min", "max");
        }

        List<String> unaryOps = normalizeOps(toStringList(source.get("unary_ops")));
        if (unaryOps.isEmpty()) {
            unaryOps = List.of("sin", "cos", "tan", "exp", "log", "sqrt", "abs", "neg");
        }

        List<String> ternaryOps = normalizeOps(toStringList(source.get("ternary_ops")));
        if (ternaryOps.isEmpty()) {
            ternaryOps = List.of("if_then_else");
        }

        boolean allowConstants = Params.bool(source, "allow_constants", true);
        List<Double> constants = toDoubleList(source.get("constants"));
        if (constants.isEmpty()) {
            constants = List.of(-1.0, 0.0, 1.0);
        }

        boolean ephemeralConstants = Params.bool(source, "ephemeral_constants", true);
        List<Double> range = toDoubleList(source.get("ephemeral_range"));
        double rangeMin = range.size() >= 1 ? range.get(0) : -5.0;
        double rangeMax = range.size() >= 2 ? range.get(1) : 5.0;

        int maxDepth = Math.max(2, Params.integer(source, "max_depth", 7));
        boolean typed = Params.bool(source, "typed", false);
        boolean booleanMode = Params.bool(source, "boolean_mode", false);
        int bitsPerDecision = Math.max(1, Params.integer(source, "bits_per_decision", 6));
        int bitsPerErc = Math.max(4, Params.integer(source, "bits_per_erc", 16));
        int maxNodes = Math.max(4, Params.integer(source, "max_nodes", 2048));
        String ercDistribution = Params.str(source, "ephemeral_distribution", "uniform");

        return new GrammarConfig(
                mode,
                file,
                variables,
                binaryOps,
                unaryOps,
                ternaryOps,
                allowConstants,
                constants,
                ephemeralConstants,
                ercDistribution,
                rangeMin,
                rangeMax,
                maxDepth,
                typed,
                booleanMode,
                bitsPerDecision,
                bitsPerErc,
                maxNodes
        );
    }

    private static String normalizeMode(String mode) {
        String normalized = mode == null ? "auto" : mode.trim().toLowerCase(Locale.ROOT);
        if (!"auto".equals(normalized) && !"custom".equals(normalized)) {
            throw new IllegalArgumentException("grammar.mode must be 'auto' or 'custom'");
        }
        return normalized;
    }

    private static List<String> normalizeOps(List<String> values) {
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalized.add(value.trim().toLowerCase(Locale.ROOT));
        }
        return normalized;
    }

    private static List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null && !String.valueOf(item).isBlank()) {
                result.add(String.valueOf(item).trim());
            }
        }
        return result;
    }

    private static List<Double> toDoubleList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Double> result = new ArrayList<>();
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            if (item instanceof Number number) {
                result.add(number.doubleValue());
            } else {
                try {
                    result.add(Double.parseDouble(String.valueOf(item)));
                } catch (NumberFormatException ignored) {
                    // ignore malformed list entries to preserve human-friendly config handling.
                }
            }
        }
        return result;
    }

    /**
     * Executes mode.
     *
     * @return the mode
     */
    public String mode() {
        return mode;
    }

    /**
     * Executes file.
     *
     * @return the file
     */
    public String file() {
        return file;
    }

    /**
     * Executes variables.
     *
     * @return the variables
     */
    public List<String> variables() {
        return variables;
    }

    /**
     * Executes binary ops.
     *
     * @return the binary ops
     */
    public List<String> binaryOps() {
        return binaryOps;
    }

    /**
     * Executes unary ops.
     *
     * @return the unary ops
     */
    public List<String> unaryOps() {
        return unaryOps;
    }

    /**
     * Executes ternary ops.
     *
     * @return the ternary ops
     */
    public List<String> ternaryOps() {
        return ternaryOps;
    }

    /**
     * Executes allow constants.
     *
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean allowConstants() {
        return allowConstants;
    }

    /**
     * Executes constants.
     *
     * @return the constants
     */
    public List<Double> constants() {
        return constants;
    }

    /**
     * Executes ephemeral constants.
     *
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean ephemeralConstants() {
        return ephemeralConstants;
    }

    /**
     * Executes ephemeral distribution.
     *
     * @return the ephemeral distribution
     */
    public String ephemeralDistribution() {
        return ephemeralDistribution;
    }

    /**
     * Executes ephemeral min.
     *
     * @return the computed ephemeral min
     */
    public double ephemeralMin() {
        return ephemeralMin;
    }

    /**
     * Executes ephemeral max.
     *
     * @return the computed ephemeral max
     */
    public double ephemeralMax() {
        return ephemeralMax;
    }

    /**
     * Executes max depth.
     *
     * @return the computed max depth
     */
    public int maxDepth() {
        return maxDepth;
    }

    /**
     * Executes typed.
     *
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean typed() {
        return typed;
    }

    /**
     * Executes boolean mode.
     *
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean booleanMode() {
        return booleanMode;
    }

    /**
     * Executes bits per decision.
     *
     * @return the computed bits per decision
     */
    public int bitsPerDecision() {
        return bitsPerDecision;
    }

    /**
     * Executes bits per erc.
     *
     * @return the computed bits per erc
     */
    public int bitsPerErc() {
        return bitsPerErc;
    }

    /**
     * Executes max nodes.
     *
     * @return the computed max nodes
     */
    public int maxNodes() {
        return maxNodes;
    }
}
