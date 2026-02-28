/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Numeric literal terminal.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ConstantTerminal extends Terminal {

    private final double value;

    /**
     * Creates an immutable numeric terminal.
     * @param value the numeric constant value
     */
    public ConstantTerminal(double value) {
        super(Double.toString(value), TypeSignature.leaf(ValueType.REAL));
        this.value = value;
    }

    /**
     * Numeric literal value.
     * @return the numeric literal stored by this terminal
     */
    public double value() {
        return value;
    }
}
