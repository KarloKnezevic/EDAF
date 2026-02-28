/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import com.knezevic.edaf.v3.repr.grammar.ops.OperatorDefinition;

/**
 * Terminal that references one executable operator definition.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class OperatorTerminal extends Terminal {

    private final OperatorDefinition operator;

    /**
     * Creates operator terminal.
     * @param operator operator definition
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
     * @return the operator
     */
    public OperatorDefinition operator() {
        return operator;
    }
}
