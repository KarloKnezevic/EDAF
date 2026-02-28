/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Variable reference terminal.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class VariableTerminal extends Terminal {

    private final String variableName;

    /**
     * Creates a variable terminal.
     * @param variableName the variableName argument
     * @param type value type
     */
    public VariableTerminal(String variableName, ValueType type) {
        super(variableName, TypeSignature.leaf(type == null ? ValueType.ANY : type));
        this.variableName = variableName;
    }

    /**
     * Variable identifier expected in evaluation contexts.
     * @return the variable name
     */
    public String variableName() {
        return variableName;
    }
}
