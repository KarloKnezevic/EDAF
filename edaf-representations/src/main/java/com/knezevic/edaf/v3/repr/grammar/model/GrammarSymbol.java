/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Common marker for non-terminals and terminals used inside grammar productions.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public interface GrammarSymbol {

    /**
     * Symbol identifier used in rules and serialized AST payloads.
     */
    String symbol();

    /**
     * Optional output type annotation.
     */
    TypeSignature typeSignature();
}
