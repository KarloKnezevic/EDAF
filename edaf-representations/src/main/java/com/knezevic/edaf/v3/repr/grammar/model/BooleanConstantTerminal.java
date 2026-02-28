/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Boolean literal terminal.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanConstantTerminal extends Terminal {

    private final boolean value;

    /**
     * Creates a boolean constant terminal.
     * @param value the value argument
     */
    public BooleanConstantTerminal(boolean value) {
        super(Boolean.toString(value), TypeSignature.leaf(ValueType.BOOL));
        this.value = value;
    }

    /**
     * Boolean literal value.
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean value() {
        return value;
    }
}
