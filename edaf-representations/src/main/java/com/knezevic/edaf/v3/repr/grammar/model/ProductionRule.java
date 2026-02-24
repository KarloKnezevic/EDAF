/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One grammar production rule.
 *
 * <p>Right-hand side may begin with an {@link OperatorTerminal} followed by argument
 * symbols, or contain a single leaf terminal/non-terminal.</p>
 */
public final class ProductionRule {

    private final String id;
    private final NonTerminal left;
    private final List<GrammarSymbol> right;

    /**
     * Creates immutable production rule.
     */
    public ProductionRule(String id, NonTerminal left, List<GrammarSymbol> right) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rule id must not be blank");
        }
        if (left == null) {
            throw new IllegalArgumentException("Rule LHS must not be null");
        }
        if (right == null || right.isEmpty()) {
            throw new IllegalArgumentException("Rule RHS must not be empty");
        }
        this.id = id.trim();
        this.left = left;
        this.right = Collections.unmodifiableList(new ArrayList<>(right));
    }

    /**
     * Rule identifier.
     */
    public String id() {
        return id;
    }

    /**
     * Left non-terminal.
     */
    public NonTerminal left() {
        return left;
    }

    /**
     * Full right-hand side symbol sequence.
     */
    public List<GrammarSymbol> right() {
        return right;
    }

    /**
     * Returns operator terminal when RHS begins with an operator.
     */
    public OperatorTerminal operator() {
        GrammarSymbol first = right.getFirst();
        return first instanceof OperatorTerminal operatorTerminal ? operatorTerminal : null;
    }

    /**
     * Returns child symbols used to build derivation tree children.
     */
    public List<GrammarSymbol> childSymbols() {
        if (operator() == null) {
            return right;
        }
        return right.subList(1, right.size());
    }

    /**
     * Returns true when rule can terminate without expanding additional non-terminals.
     */
    public boolean isLeafCandidate() {
        for (GrammarSymbol symbol : childSymbols()) {
            if (symbol instanceof NonTerminal) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of non-terminal child symbols.
     */
    public int nonTerminalChildCount() {
        int count = 0;
        for (GrammarSymbol symbol : childSymbols()) {
            if (symbol instanceof NonTerminal) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return left.symbol() + " ::= " + right;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, left, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProductionRule other)) {
            return false;
        }
        return id.equals(other.id) && left.equals(other.left) && right.equals(other.right);
    }
}
