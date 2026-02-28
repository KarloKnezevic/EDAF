/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.ops;

import com.knezevic.edaf.v3.repr.grammar.model.TypeSignature;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;

import java.util.List;
import java.util.function.Function;

/**
 * One registered grammar operator with arity, type signature, and execution lambda.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class OperatorDefinition {

    private final String name;
    private final int arity;
    private final OperatorKind kind;
    private final TypeSignature typeSignature;
    private final Function<List<Double>, Double> evaluator;

    /**
     * Creates one operator definition.
     */
    public OperatorDefinition(String name,
                              int arity,
                              OperatorKind kind,
                              TypeSignature typeSignature,
                              Function<List<Double>, Double> evaluator) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Operator name must not be blank");
        }
        if (arity < 1) {
            throw new IllegalArgumentException("Operator arity must be >= 1");
        }
        this.name = name.trim();
        this.arity = arity;
        this.kind = kind;
        this.typeSignature = typeSignature == null
                ? TypeSignature.leaf(ValueType.ANY)
                : typeSignature;
        this.evaluator = evaluator;
        if (this.evaluator == null) {
            throw new IllegalArgumentException("Operator evaluator must not be null");
        }
    }

    /**
     * Canonical operator name used in grammar terminals.
     * @return component name identifier
     */
    public String name() {
        return name;
    }

    /**
     * Required argument count.
     * @return the computed arity
     */
    public int arity() {
        return arity;
    }

    /**
     * Operator family.
     * @return the kind
     */
    public OperatorKind kind() {
        return kind;
    }

    /**
     * Output/input type signature.
     * @return the type signature
     */
    public TypeSignature typeSignature() {
        return typeSignature;
    }

    /**
     * Evaluates operator for provided argument list.
     * @param args operator arguments
     * @return evaluation result
     */
    public double evaluate(List<Double> args) {
        if (args == null || args.size() != arity) {
            throw new IllegalArgumentException("Operator '" + name + "' expects " + arity + " args");
        }
        return evaluator.apply(args);
    }
}
