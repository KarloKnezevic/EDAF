/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.build;

import com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.grammar.model.GrammarSymbol;
import com.knezevic.edaf.v3.repr.grammar.model.NonTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;
import com.knezevic.edaf.v3.repr.grammar.model.TypeSignature;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorDefinition;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Safe parser for custom BNF-like grammar files.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarParser {

    private final OperatorRegistry operatorRegistry;

    /**
     * Creates parser with default operator registry.
     */
    public GrammarParser() {
        this(new OperatorRegistry());
    }

    /**
     * Creates parser with custom operator registry.
     * @param operatorRegistry operator registry
     */
    public GrammarParser(OperatorRegistry operatorRegistry) {
        this.operatorRegistry = operatorRegistry;
    }

    /**
     * Parses custom grammar from file path.
     * @param file grammar source file
     * @param config grammar configuration
     * @return parsed grammar
     */
    public Grammar parse(Path file, GrammarConfig config) {
        if (file == null) {
            throw new IllegalArgumentException("grammar.file must be provided for custom mode");
        }
        try {
            return parse(Files.readString(file), config, file.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed reading grammar file: " + file, e);
        }
    }

    /**
     * Parses custom grammar from text payload.
     * @param bnf BNF grammar source text
     * @param config grammar configuration
     * @param sourceName source identifier
     * @return parsed grammar
     */
    public Grammar parse(String bnf, GrammarConfig config, String sourceName) {
        if (bnf == null || bnf.isBlank()) {
            throw new IllegalArgumentException("Custom grammar source is empty");
        }
        Map<String, List<List<String>>> rawRules = readRules(bnf, sourceName);
        if (rawRules.isEmpty()) {
            throw new IllegalArgumentException("Custom grammar contains no productions");
        }

        Map<String, NonTerminal> nonTerminals = new LinkedHashMap<>();
        for (String symbol : rawRules.keySet()) {
            nonTerminals.put(symbol, new NonTerminal(symbol, TypeSignature.leaf(ValueType.ANY)));
        }

        NonTerminal start = nonTerminals.get(rawRules.keySet().iterator().next());

        Map<NonTerminal, List<ProductionRule>> rules = new LinkedHashMap<>();
        int ruleCounter = 0;

        for (Map.Entry<String, List<List<String>>> entry : rawRules.entrySet()) {
            NonTerminal left = nonTerminals.get(entry.getKey());
            List<ProductionRule> productions = new ArrayList<>();
            for (List<String> altTokens : entry.getValue()) {
                List<GrammarSymbol> rhs = toSymbols(altTokens, nonTerminals, config, sourceName, left.symbol());
                ProductionRule rule = new ProductionRule("C" + (++ruleCounter), left, rhs);
                validateRuleShape(rule, sourceName);
                productions.add(rule);
            }
            rules.put(left, productions);
        }

        validateReachability(start, rules, sourceName);
        validateTerminability(rules, sourceName);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("mode", "custom");
        metadata.put("source", sourceName);
        metadata.put("maxDepth", config.maxDepth());
        metadata.put("typed", config.typed());
        metadata.put("booleanMode", config.booleanMode());

        return new Grammar(start, rules, metadata);
    }

    private Map<String, List<List<String>>> readRules(String bnf, String sourceName) {
        Map<String, List<List<String>>> rules = new LinkedHashMap<>();
        String activeLeft = null;

        String[] lines = bnf.split("\\R");
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = stripComments(lines[lineIndex]).trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.contains("::=")) {
                int split = line.indexOf("::=");
                String leftToken = line.substring(0, split).trim();
                String rhsToken = line.substring(split + 3).trim();
                activeLeft = parseNonTerminalToken(leftToken, sourceName, lineIndex + 1);
                List<List<String>> alternatives = parseAlternatives(rhsToken);
                if (alternatives.isEmpty()) {
                    throw new IllegalArgumentException("Rule " + leftToken + " in " + sourceName
                            + " has no alternatives at line " + (lineIndex + 1));
                }
                rules.computeIfAbsent(activeLeft, ignored -> new ArrayList<>())
                        .addAll(alternatives);
            } else if (activeLeft != null) {
                List<List<String>> alternatives = parseAlternatives(line);
                if (alternatives.isEmpty()) {
                    throw new IllegalArgumentException("Rule <" + activeLeft + "> in " + sourceName
                            + " has malformed continuation at line " + (lineIndex + 1));
                }
                rules.computeIfAbsent(activeLeft, ignored -> new ArrayList<>())
                        .addAll(alternatives);
            } else {
                throw new IllegalArgumentException("Invalid grammar line " + (lineIndex + 1)
                        + " in " + sourceName + ": expected '<NonTerminal> ::= ...'");
            }
        }
        return rules;
    }

    private List<List<String>> parseAlternatives(String rhs) {
        List<List<String>> alternatives = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < rhs.length(); i++) {
            char ch = rhs.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                current.append(ch);
                continue;
            }
            if (ch == '|' && !inQuotes) {
                List<String> candidate = tokenize(current.toString().trim());
                if (!candidate.isEmpty()) {
                    alternatives.add(candidate);
                }
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        List<String> last = tokenize(current.toString().trim());
        if (!last.isEmpty()) {
            alternatives.add(last);
        }
        return alternatives;
    }

    private List<String> tokenize(String value) {
        List<String> tokens = new ArrayList<>();
        if (value.isBlank()) {
            return tokens;
        }
        StringBuilder token = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                token.append(ch);
                continue;
            }
            if (!inQuotes && Character.isWhitespace(ch)) {
                if (!token.isEmpty()) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
                continue;
            }
            token.append(ch);
        }
        if (!token.isEmpty()) {
            tokens.add(token.toString());
        }
        return tokens;
    }

    private List<GrammarSymbol> toSymbols(List<String> tokens,
                                          Map<String, NonTerminal> nonTerminals,
                                          GrammarConfig config,
                                          String sourceName,
                                          String leftSymbol) {
        List<GrammarSymbol> symbols = new ArrayList<>();
        for (String raw : tokens) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String token = stripQuotes(raw.trim());
            if (token.isEmpty() || "(".equals(token) || ")".equals(token) || ",".equals(token)) {
                continue;
            }

            if (isNonTerminalToken(token)) {
                String name = normalizeNonTerminal(token);
                NonTerminal nonTerminal = nonTerminals.get(name);
                if (nonTerminal == null) {
                    throw new IllegalArgumentException("Unknown non-terminal '" + token + "' in "
                            + sourceName + " for rule " + leftSymbol);
                }
                symbols.add(nonTerminal);
                continue;
            }

            GrammarSymbol terminal = parseTerminal(token, config, sourceName, leftSymbol);
            symbols.add(terminal);
        }
        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("Rule " + leftSymbol + " in " + sourceName
                    + " contains empty alternative");
        }
        return symbols;
    }

    private GrammarSymbol parseTerminal(String token,
                                        GrammarConfig config,
                                        String sourceName,
                                        String leftSymbol) {
        String normalized = token.trim().toLowerCase(Locale.ROOT);

        if ("erc".equals(normalized)) {
            if (!config.ephemeralConstants()) {
                throw new IllegalArgumentException("Rule " + leftSymbol + " in " + sourceName
                        + " uses ERC but ephemeral_constants=false");
            }
            return new EphemeralConstantTerminal(
                    config.ephemeralDistribution(),
                    config.ephemeralMin(),
                    config.ephemeralMax());
        }

        OperatorDefinition operator = operatorRegistry.find(normalized).orElse(null);
        if (operator != null) {
            return new OperatorTerminal(operator);
        }

        if ("true".equals(normalized) || "false".equals(normalized)) {
            return new BooleanConstantTerminal(Boolean.parseBoolean(normalized));
        }

        if (isNumeric(token)) {
            return new ConstantTerminal(Double.parseDouble(token));
        }

        if (config.variables().contains(token)) {
            ValueType type = config.booleanMode() ? ValueType.BOOL : ValueType.REAL;
            return new VariableTerminal(token, type);
        }

        throw new IllegalArgumentException("Unknown terminal token '" + token + "' in "
                + sourceName + " rule " + leftSymbol
                + ". Declare it as variable, operator, numeric constant, boolean literal, or ERC.");
    }

    private void validateRuleShape(ProductionRule rule, String sourceName) {
        OperatorTerminal operator = rule.operator();
        if (operator != null) {
            int args = rule.childSymbols().size();
            if (args != operator.operator().arity()) {
                throw new IllegalArgumentException("Rule " + rule.id() + " in " + sourceName
                        + " uses operator '" + operator.symbol() + "' with " + args
                        + " args; expected " + operator.operator().arity());
            }
            return;
        }

        // Non-operator alternatives should be simple aliases/leaves.
        if (rule.right().size() != 1) {
            throw new IllegalArgumentException("Rule " + rule.id() + " in " + sourceName
                    + " must have exactly one symbol when no leading operator is present");
        }
    }

    private void validateReachability(NonTerminal start,
                                      Map<NonTerminal, List<ProductionRule>> rules,
                                      String sourceName) {
        Set<NonTerminal> reachable = new LinkedHashSet<>();
        Deque<NonTerminal> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            NonTerminal current = queue.removeFirst();
            if (!reachable.add(current)) {
                continue;
            }
            for (ProductionRule rule : rules.getOrDefault(current, List.of())) {
                for (GrammarSymbol symbol : rule.childSymbols()) {
                    if (symbol instanceof NonTerminal next && !reachable.contains(next)) {
                        queue.addLast(next);
                    }
                }
            }
        }

        for (NonTerminal nonTerminal : rules.keySet()) {
            if (!reachable.contains(nonTerminal)) {
                throw new IllegalArgumentException("Unreachable non-terminal '" + nonTerminal.symbol()
                        + "' in " + sourceName);
            }
        }
    }

    private void validateTerminability(Map<NonTerminal, List<ProductionRule>> rules, String sourceName) {
        Set<NonTerminal> terminable = new LinkedHashSet<>();
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<NonTerminal, List<ProductionRule>> entry : rules.entrySet()) {
                if (terminable.contains(entry.getKey())) {
                    continue;
                }
                for (ProductionRule rule : entry.getValue()) {
                    if (canTerminate(rule, terminable)) {
                        terminable.add(entry.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);

        for (NonTerminal nonTerminal : rules.keySet()) {
            if (!terminable.contains(nonTerminal)) {
                throw new IllegalArgumentException("Non-terminal '" + nonTerminal.symbol() + "' in "
                        + sourceName + " is recursive without base case");
            }
        }
    }

    private boolean canTerminate(ProductionRule rule, Set<NonTerminal> terminable) {
        for (GrammarSymbol symbol : rule.childSymbols()) {
            if (symbol instanceof NonTerminal nonTerminal && !terminable.contains(nonTerminal)) {
                return false;
            }
        }
        return true;
    }

    private static String parseNonTerminalToken(String token, String sourceName, int line) {
        if (!isNonTerminalToken(token)) {
            throw new IllegalArgumentException("Invalid non-terminal token '" + token
                    + "' at " + sourceName + ":" + line + " (expected <Name>)");
        }
        return normalizeNonTerminal(token);
    }

    private static boolean isNonTerminalToken(String token) {
        String trimmed = token == null ? "" : token.trim();
        return trimmed.startsWith("<") && trimmed.endsWith(">");
    }

    private static String normalizeNonTerminal(String token) {
        String trimmed = token.trim();
        return trimmed.substring(1, trimmed.length() - 1).trim();
    }

    private static String stripQuotes(String token) {
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }

    private static String stripComments(String line) {
        String noHash = stripOutsideQuotes(line, '#');
        int idx = indexOfDoubleSlashOutsideQuotes(noHash);
        if (idx >= 0) {
            return noHash.substring(0, idx);
        }
        return noHash;
    }

    private static String stripOutsideQuotes(String line, char marker) {
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && ch == marker) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static int indexOfDoubleSlashOutsideQuotes(String line) {
        boolean inQuotes = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && ch == '/' && line.charAt(i + 1) == '/') {
                return i;
            }
        }
        return -1;
    }

    private static boolean isNumeric(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
