package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.Objects;

/**
 * One non-terminal symbol in a grammar.
 */
public final class NonTerminal implements GrammarSymbol {

    private final String symbol;
    private final TypeSignature typeSignature;

    /**
     * Creates a non-terminal symbol.
     */
    public NonTerminal(String symbol, TypeSignature typeSignature) {
        this.symbol = normalize(symbol);
        this.typeSignature = typeSignature == null
                ? TypeSignature.leaf(ValueType.ANY)
                : typeSignature;
    }

    /**
     * Creates an untyped non-terminal symbol.
     */
    public static NonTerminal untyped(String symbol) {
        return new NonTerminal(symbol, TypeSignature.leaf(ValueType.ANY));
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public TypeSignature typeSignature() {
        return typeSignature;
    }

    @Override
    public String toString() {
        return symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

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
