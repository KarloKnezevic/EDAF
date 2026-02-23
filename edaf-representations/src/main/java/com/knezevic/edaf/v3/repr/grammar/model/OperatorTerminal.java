package com.knezevic.edaf.v3.repr.grammar.model;

import com.knezevic.edaf.v3.repr.grammar.ops.OperatorDefinition;

/**
 * Terminal that references one executable operator definition.
 */
public final class OperatorTerminal extends Terminal {

    private final OperatorDefinition operator;

    /**
     * Creates operator terminal.
     */
    public OperatorTerminal(OperatorDefinition operator) {
        super(operator == null ? "" : operator.name(), operator == null ? null : operator.typeSignature());
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null");
        }
        this.operator = operator;
    }

    /**
     * Bound operator implementation.
     */
    public OperatorDefinition operator() {
        return operator;
    }
}
