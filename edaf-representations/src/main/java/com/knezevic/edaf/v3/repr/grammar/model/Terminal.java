/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Base terminal symbol class.
 */
public abstract sealed class Terminal implements GrammarSymbol
        permits VariableTerminal, ConstantTerminal, EphemeralConstantTerminal, BooleanConstantTerminal, OperatorTerminal {

    private final String symbol;
    private final TypeSignature typeSignature;

    /**
     * Creates a new Terminal instance.
     *
     * @param symbol grammar symbol
     * @param typeSignature type signature
     */
    protected Terminal(String symbol, TypeSignature typeSignature) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Terminal symbol must not be blank");
        }
        this.symbol = symbol.trim();
        this.typeSignature = typeSignature == null
                ? TypeSignature.leaf(ValueType.ANY)
                : typeSignature;
    }

    /**
     * Executes symbol.
     *
     * @return the symbol
     */
    @Override
    public final String symbol() {
        return symbol;
    }

    /**
     * Executes type signature.
     *
     * @return the type signature
     */
    @Override
    public final TypeSignature typeSignature() {
        return typeSignature;
    }

    /**
     * Converts to string.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return symbol;
    }
}
