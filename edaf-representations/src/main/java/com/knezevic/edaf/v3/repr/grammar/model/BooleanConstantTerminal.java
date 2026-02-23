package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Boolean literal terminal.
 */
public final class BooleanConstantTerminal extends Terminal {

    private final boolean value;

    /**
     * Creates a boolean constant terminal.
     */
    public BooleanConstantTerminal(boolean value) {
        super(Boolean.toString(value), TypeSignature.leaf(ValueType.BOOL));
        this.value = value;
    }

    /**
     * Boolean literal value.
     */
    public boolean value() {
        return value;
    }
}
