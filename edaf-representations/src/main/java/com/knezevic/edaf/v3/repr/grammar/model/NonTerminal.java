/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.Objects;

/**
 * One non-terminal symbol in a grammar.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NonTerminal implements GrammarSymbol {

    private final String symbol;
    private final TypeSignature typeSignature;

    /**
     * Creates a non-terminal symbol.
     * @param symbol grammar symbol
     * @param typeSignature type signature
     */
    public NonTerminal(String symbol, TypeSignature typeSignature) {
        this.symbol = normalize(symbol);
        this.typeSignature = typeSignature == null
                ? TypeSignature.leaf(ValueType.ANY)
                : typeSignature;
    }

    /**
     * Creates an untyped non-terminal symbol.
     * @param symbol grammar symbol
     * @return the untyped
     */
    public static NonTerminal untyped(String symbol) {
        return new NonTerminal(symbol, TypeSignature.leaf(ValueType.ANY));
    }

    /**
     * Executes symbol.
     *
     * @return the symbol
     */
    @Override
    public String symbol() {
        return symbol;
    }

    /**
     * Executes type signature.
     *
     * @return the type signature
     */
    @Override
    public TypeSignature typeSignature() {
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

    /**
     * Executes hash code.
     *
     * @return true if the instance has h code; otherwise false
     */
    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    /**
     * Executes equals.
     *
     * @param obj the obj argument
     * @return true if the condition is satisfied; otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NonTerminal other)) {
            return false;
        }
        return symbol.equals(other.symbol);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Non-terminal symbol must not be blank");
        }
        return value.trim();
    }
}
