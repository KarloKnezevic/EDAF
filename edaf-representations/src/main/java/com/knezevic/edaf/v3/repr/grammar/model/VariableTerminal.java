/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Variable reference terminal.
 */
public final class VariableTerminal extends Terminal {

    private final String variableName;

    /**
     * Creates a variable terminal.
     */
    public VariableTerminal(String variableName, ValueType type) {
        super(variableName, TypeSignature.leaf(type == null ? ValueType.ANY : type));
        this.variableName = variableName;
    }

    /**
     * Variable identifier expected in evaluation contexts.
     */
    public String variableName() {
        return variableName;
    }
}
