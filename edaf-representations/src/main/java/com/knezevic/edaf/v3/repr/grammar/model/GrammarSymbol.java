package com.knezevic.edaf.v3.repr.grammar.model;

/**
 * Common marker for non-terminals and terminals used inside grammar productions.
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
