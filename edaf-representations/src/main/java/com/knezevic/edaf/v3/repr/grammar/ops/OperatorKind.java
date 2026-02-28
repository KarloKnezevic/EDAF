/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.ops;

/**
 * Operator family used by printers and evaluator safety rules.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public enum OperatorKind {
    REAL_UNARY,
    REAL_BINARY,
    REAL_TERNARY,
    BOOLEAN_UNARY,
    BOOLEAN_BINARY,
    BOOLEAN_TERNARY
}
