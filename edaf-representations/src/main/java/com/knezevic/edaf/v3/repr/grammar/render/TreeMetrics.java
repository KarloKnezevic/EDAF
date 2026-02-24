/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.render;

import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structural metrics for derivation trees.
 */
public final class TreeMetrics {

    private TreeMetrics() {
        // utility class
    }

    /**
     * Computes depth, size, and operator usage summary.
     */
    public static Summary summarize(DerivationTree tree) {
        Accumulator accumulator = new Accumulator();
        walk(tree, accumulator);
        return new Summary(
                accumulator.maxDepth,
                accumulator.nodeCount,
                accumulator.terminalCount,
                accumulator.ruleCount,
                Map.copyOf(accumulator.operatorUsage)
        );
    }

    private static void walk(DerivationTree node, Accumulator accumulator) {
        if (node == null) {
            return;
        }
        accumulator.nodeCount++;
        accumulator.maxDepth = Math.max(accumulator.maxDepth, node.depth());

        if (node instanceof DerivationTree.TerminalNode) {
            accumulator.terminalCount++;
            return;
        }

        accumulator.ruleCount++;
        DerivationTree.RuleNode ruleNode = (DerivationTree.RuleNode) node;
        ProductionRule rule = ruleNode.productionRule();
        OperatorTerminal operator = rule.operator();
        if (operator != null) {
            accumulator.operatorUsage.merge(operator.symbol(), 1L, Long::sum);
        }
        for (DerivationTree child : ruleNode.children()) {
            walk(child, accumulator);
        }
    }

    /**
     * Immutable metrics summary.
     */
    public record Summary(
            int depth,
            int size,
            int terminalCount,
            int ruleCount,
            Map<String, Long> operatorUsage
    ) {
    }

    private static final class Accumulator {
        private int maxDepth;
        private int nodeCount;
        private int terminalCount;
        private int ruleCount;
        private final Map<String, Long> operatorUsage = new LinkedHashMap<>();
    }
}
