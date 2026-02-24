/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable grammar model with start symbol, productions, and metadata.
 */
public final class Grammar {

    private final NonTerminal startSymbol;
    private final Map<String, NonTerminal> nonTerminals;
    private final Map<NonTerminal, List<ProductionRule>> rules;
    private final Map<String, Object> metadata;

    /**
     * Creates immutable grammar.
     */
    public Grammar(NonTerminal startSymbol,
                   Map<NonTerminal, List<ProductionRule>> rules,
                   Map<String, Object> metadata) {
        if (startSymbol == null) {
            throw new IllegalArgumentException("Grammar start symbol is required");
        }
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Grammar rules must not be empty");
        }

        this.startSymbol = startSymbol;

        Map<NonTerminal, List<ProductionRule>> immutableRules = new LinkedHashMap<>();
        Map<String, NonTerminal> ntBySymbol = new LinkedHashMap<>();
        Set<String> ruleIds = new LinkedHashSet<>();
        for (Map.Entry<NonTerminal, List<ProductionRule>> entry : rules.entrySet()) {
            NonTerminal nonTerminal = entry.getKey();
            if (nonTerminal == null) {
                throw new IllegalArgumentException("Grammar non-terminal key must not be null");
            }
            List<ProductionRule> productions = entry.getValue();
            if (productions == null || productions.isEmpty()) {
                throw new IllegalArgumentException("Grammar non-terminal '" + nonTerminal + "' has no productions");
            }
            List<ProductionRule> copy = new ArrayList<>(productions.size());
            for (ProductionRule rule : productions) {
                if (rule == null) {
                    throw new IllegalArgumentException("Grammar production must not be null");
                }
                if (!nonTerminal.equals(rule.left())) {
                    throw new IllegalArgumentException("Rule '" + rule.id() + "' left symbol mismatch");
                }
                if (!ruleIds.add(rule.id())) {
                    throw new IllegalArgumentException("Duplicate rule id: " + rule.id());
                }
                copy.add(rule);
            }
            immutableRules.put(nonTerminal, Collections.unmodifiableList(copy));
            ntBySymbol.put(nonTerminal.symbol(), nonTerminal);
        }

        if (!immutableRules.containsKey(startSymbol)) {
            throw new IllegalArgumentException("Start symbol '" + startSymbol + "' has no productions");
        }

        this.rules = Collections.unmodifiableMap(immutableRules);
        this.nonTerminals = Collections.unmodifiableMap(ntBySymbol);
        this.metadata = metadata == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    /**
     * Grammar start symbol.
     */
    public NonTerminal startSymbol() {
        return startSymbol;
    }

    /**
     * Returns productions for one non-terminal.
     */
    public List<ProductionRule> rulesFor(NonTerminal symbol) {
        List<ProductionRule> productions = rules.get(symbol);
        if (productions == null) {
            throw new IllegalArgumentException("Unknown non-terminal: " + symbol);
        }
        return productions;
    }

    /**
     * Returns non-terminal by symbol text or null when absent.
     */
    public NonTerminal findNonTerminal(String symbol) {
        return nonTerminals.get(symbol);
    }

    /**
     * All non-terminals.
     */
    public List<NonTerminal> nonTerminals() {
        return List.copyOf(nonTerminals.values());
    }

    /**
     * Immutable grammar metadata map.
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Maximum RHS production count among all non-terminals.
     */
    public int maxAlternatives() {
        int max = 0;
        for (List<ProductionRule> productions : rules.values()) {
            max = Math.max(max, productions.size());
        }
        return Math.max(1, max);
    }

    /**
     * Maximum number of non-terminal children among productions.
     */
    public int maxBranchingFactor() {
        int max = 0;
        for (List<ProductionRule> productions : rules.values()) {
            for (ProductionRule rule : productions) {
                max = Math.max(max, rule.nonTerminalChildCount());
            }
        }
        return max;
    }
}
