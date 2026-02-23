package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Numeric literal terminal.
 */
public final class ConstantTerminal extends Terminal {

    private final double value;

    /**
     * Creates an immutable numeric terminal.
     */
    public ConstantTerminal(double value) {
        super(Double.toString(value), TypeSignature.leaf(ValueType.REAL));
        this.value = value;
    }

    /**
     * Numeric literal value.
     */
    public double value() {
        return value;
    }
}
