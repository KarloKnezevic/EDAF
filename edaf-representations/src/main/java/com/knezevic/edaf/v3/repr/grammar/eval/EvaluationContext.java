/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.eval;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Variable bindings used for tree evaluation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EvaluationContext {

    private final Map<String, Double> realVariables;
    private final Map<String, Boolean> booleanVariables;

    /**
     * Creates context from numeric and boolean variable maps.
     * @param realVariables real-valued variable map
     * @param booleanVariables boolean variable map
     */
    public EvaluationContext(Map<String, Double> realVariables, Map<String, Boolean> booleanVariables) {
        this.realVariables = immutableCopy(realVariables);
        this.booleanVariables = immutableCopyBoolean(booleanVariables);
    }

    /**
     * Creates context from numeric variables only.
     * @param variables input variable map
     * @return the real
     */
    public static EvaluationContext real(Map<String, Double> variables) {
        return new EvaluationContext(variables, Map.of());
    }

    /**
     * Creates context from boolean variables only.
     * @param variables input variable map
     * @return the bool
     */
    public static EvaluationContext bool(Map<String, Boolean> variables) {
        return new EvaluationContext(Map.of(), variables);
    }

    /**
     * Resolves numeric variable (missing values default to zero).
     * @param name symbol name
     * @return the computed real
     */
    public double real(String name) {
        if (name == null) {
            return 0.0;
        }
        if (realVariables.containsKey(name)) {
            return realVariables.get(name);
        }
        Boolean boolValue = booleanVariables.get(name);
        return boolValue == null ? 0.0 : (boolValue ? 1.0 : 0.0);
    }

    /**
     * Resolves boolean variable (missing values default to false).
     * @param name symbol name
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean bool(String name) {
        if (name == null) {
            return false;
        }
        if (booleanVariables.containsKey(name)) {
            return Boolean.TRUE.equals(booleanVariables.get(name));
        }
        Double realValue = realVariables.get(name);
        return realValue != null && realValue > 0.5;
    }

    private static Map<String, Double> immutableCopy(Map<String, Double> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(input));
    }

    private static Map<String, Boolean> immutableCopyBoolean(Map<String, Boolean> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(input));
    }
}
