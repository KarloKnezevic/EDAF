/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.ops;

import com.knezevic.edaf.v3.repr.grammar.model.TypeSignature;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Central operator catalog for grammar-based GP.
 *
 * <p>The registry intentionally uses protected variants for numerically unsafe
 * operators (division, log, sqrt) to keep search stable.</p>
 */
public final class OperatorRegistry {

    private final Map<String, OperatorDefinition> operators;

    /**
     * Creates default EDAF operator registry with ECF-compatible core set.
     */
    public OperatorRegistry() {
        this.operators = defaultOperators();
    }

    /**
     * Resolves operator by name (case-insensitive).
     */
    public Optional<OperatorDefinition> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(operators.get(normalize(name)));
    }

    /**
     * Returns all known operators.
     */
    public List<OperatorDefinition> all() {
        return List.copyOf(operators.values());
    }

    /**
     * Returns subset by operator kind.
     */
    public List<OperatorDefinition> byKind(OperatorKind kind) {
        List<OperatorDefinition> result = new ArrayList<>();
        for (OperatorDefinition operator : operators.values()) {
            if (operator.kind() == kind) {
                result.add(operator);
            }
        }
        return result;
    }

    private static Map<String, OperatorDefinition> defaultOperators() {
        Map<String, OperatorDefinition> map = new LinkedHashMap<>();

        // Real-valued binary operators.
        register(map, op("+", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> args.get(0) + args.get(1)));
        register(map, op("-", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> args.get(0) - args.get(1)));
        register(map, op("*", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> args.get(0) * args.get(1)));
        register(map, op("/", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> protectedDivision(args.get(0), args.get(1))));
        register(map, op("pow", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> protectedPow(args.get(0), args.get(1))));
        register(map, op("min", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> Math.min(args.get(0), args.get(1))));
        register(map, op("max", 2, OperatorKind.REAL_BINARY, ValueType.REAL,
                args -> Math.max(args.get(0), args.get(1))));

        // Real-valued unary operators.
        register(map, op("sin", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> Math.sin(args.get(0))));
        register(map, op("cos", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> Math.cos(args.get(0))));
        register(map, op("tan", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> Math.tan(args.get(0))));
        register(map, op("exp", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> clamp(Math.exp(clamp(args.get(0), -40.0, 40.0)), -1.0e12, 1.0e12)));
        register(map, op("log", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> protectedLog(args.get(0))));
        register(map, op("sqrt", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> protectedSqrt(args.get(0))));
        register(map, op("abs", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> Math.abs(args.get(0))));
        register(map, op("neg", 1, OperatorKind.REAL_UNARY, ValueType.REAL,
                args -> -args.get(0)));

        // Ternary real operator.
        register(map, op("if_then_else", 3, OperatorKind.REAL_TERNARY, ValueType.REAL,
                args -> truthy(args.get(0)) ? args.get(1) : args.get(2)));

        // Boolean operators (encoded as 1.0 true / 0.0 false for evaluator interoperability).
        register(map, op("and", 2, OperatorKind.BOOLEAN_BINARY, ValueType.BOOL,
                args -> bool(truthy(args.get(0)) && truthy(args.get(1)))));
        register(map, op("or", 2, OperatorKind.BOOLEAN_BINARY, ValueType.BOOL,
                args -> bool(truthy(args.get(0)) || truthy(args.get(1)))));
        register(map, op("xor", 2, OperatorKind.BOOLEAN_BINARY, ValueType.BOOL,
                args -> bool(truthy(args.get(0)) ^ truthy(args.get(1)))));
        register(map, op("not", 1, OperatorKind.BOOLEAN_UNARY, ValueType.BOOL,
                args -> bool(!truthy(args.get(0)))));
        register(map, op("nand", 2, OperatorKind.BOOLEAN_BINARY, ValueType.BOOL,
                args -> bool(!(truthy(args.get(0)) && truthy(args.get(1))))));
        register(map, op("nor", 2, OperatorKind.BOOLEAN_BINARY, ValueType.BOOL,
                args -> bool(!(truthy(args.get(0)) || truthy(args.get(1))))));
        register(map, op("if", 3, OperatorKind.BOOLEAN_TERNARY, ValueType.BOOL,
                args -> bool(truthy(args.get(0)) ? truthy(args.get(1)) : truthy(args.get(2)))));

        return map;
    }

    private static OperatorDefinition op(String name,
                                         int arity,
                                         OperatorKind kind,
                                         ValueType output,
                                         java.util.function.Function<List<Double>, Double> evaluator) {
        List<ValueType> input = new ArrayList<>(arity);
        for (int i = 0; i < arity; i++) {
            input.add(ValueType.ANY);
        }
        return new OperatorDefinition(name, arity, kind, new TypeSignature(output, input), evaluator);
    }

    private static void register(Map<String, OperatorDefinition> map, OperatorDefinition definition) {
        map.put(normalize(definition.name()), definition);
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static double protectedDivision(double numerator, double denominator) {
        return Math.abs(denominator) < 1.0e-12 ? numerator : numerator / denominator;
    }

    private static double protectedPow(double base, double exponent) {
        double safeExponent = clamp(exponent, -8.0, 8.0);
        double value = Math.pow(base, safeExponent);
        if (!Double.isFinite(value)) {
            return clamp(base, -1.0e6, 1.0e6);
        }
        return clamp(value, -1.0e12, 1.0e12);
    }

    private static double protectedLog(double value) {
        return Math.log(Math.abs(value) + 1.0e-12);
    }

    private static double protectedSqrt(double value) {
        return Math.sqrt(Math.abs(value));
    }

    private static boolean truthy(double value) {
        return value > 0.5;
    }

    private static double bool(boolean value) {
        return value ? 1.0 : 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
