/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Terminal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes derivation trees into structured JSON payloads.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TreeSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Converts tree to nested map suitable for API payloads.
     * @param tree derivation tree
     * @return the map representation
     */
    public Map<String, Object> toMap(DerivationTree tree) {
        if (tree == null) {
            return Map.of();
        }

        Map<String, Object> node = new LinkedHashMap<>();
        node.put("depth", tree.depth());
        node.put("outputType", tree.outputType().name());

        if (tree instanceof DerivationTree.TerminalNode terminalNode) {
            Terminal terminal = terminalNode.terminal();
            node.put("kind", "terminal");
            node.put("symbol", terminal.symbol());
            node.put("terminalType", terminal.getClass().getSimpleName());
            if (terminalNode.sampledValue() != null) {
                node.put("sampledValue", terminalNode.sampledValue());
            }
            node.put("children", List.of());
            return node;
        }

        DerivationTree.RuleNode ruleNode = (DerivationTree.RuleNode) tree;
        node.put("kind", "rule");
        node.put("nonTerminal", ruleNode.nonTerminal().symbol());
        node.put("ruleId", ruleNode.productionRule().id());
        OperatorTerminal operator = ruleNode.productionRule().operator();
        node.put("operator", operator == null ? null : operator.symbol());

        List<Map<String, Object>> children = new ArrayList<>();
        for (DerivationTree child : ruleNode.children()) {
            children.add(toMap(child));
        }
        node.put("children", children);
        return node;
    }

    /**
     * Converts tree map representation to pretty JSON.
     * @param tree derivation tree
     * @return json representation
     */
    public String toJson(DerivationTree tree) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(toMap(tree));
        } catch (Exception e) {
            throw new RuntimeException("Failed serializing derivation tree", e);
        }
    }
}
