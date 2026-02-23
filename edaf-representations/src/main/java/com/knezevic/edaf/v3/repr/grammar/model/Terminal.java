package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Base terminal symbol class.
 */
public abstract sealed class Terminal implements GrammarSymbol
        permits VariableTerminal, ConstantTerminal, EphemeralConstantTerminal, BooleanConstantTerminal, OperatorTerminal {

    private final String symbol;
    private final TypeSignature typeSignature;

    protected Terminal(String symbol, TypeSignature typeSignature) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Terminal symbol must not be blank");
        }
        this.symbol = symbol.trim();
        this.typeSignature = typeSignature == null
                ? TypeSignature.leaf(ValueType.ANY)
                : typeSignature;
    }

    @Override
    public final String symbol() {
        return symbol;
    }

    @Override
    public final TypeSignature typeSignature() {
        return typeSignature;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
