/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable derivation tree (AST) built from grammar productions.
 */
public sealed interface DerivationTree permits DerivationTree.RuleNode, DerivationTree.TerminalNode {

    /**
     * Node depth where root is zero.
     */
    int depth();

    /**
     * Output type of this expression node.
     */
    ValueType outputType();

    /**
     * Child nodes.
     */
    List<DerivationTree> children();

    /**
     * Node representation kind.
     */
    default boolean isTerminal() {
        return this instanceof TerminalNode;
    }

    /**
     * Non-terminal expansion node.
     * @author Karlo Knezevic
     * @version EDAF 3.0.0
     */
    record RuleNode(NonTerminal nonTerminal,
                    ProductionRule productionRule,
                    List<DerivationTree> children,
                    int depth) implements DerivationTree {

        /**
         * Immutable constructor.
         */
        public RuleNode {
            if (nonTerminal == null) {
                throw new IllegalArgumentException("nonTerminal must not be null");
            }
            if (productionRule == null) {
                throw new IllegalArgumentException("productionRule must not be null");
            }
            children = children == null
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(children));
        }

        /**
         * Executes output type.
         *
         * @return the output type
         */
        @Override
        public ValueType outputType() {
            return productionRule.left().typeSignature().outputType();
        }
    }

    /**
     * Leaf terminal node.
     *
     * <p>For ERC nodes, {@code sampledValue} stores sampled numeric constant.</p>
     * @author Karlo Knezevic
     * @version EDAF 3.0.0
     */
    record TerminalNode(Terminal terminal,
                        Double sampledValue,
                        int depth) implements DerivationTree {

        /**
         * Immutable constructor.
         */
        public TerminalNode {
            if (terminal == null) {
                throw new IllegalArgumentException("terminal must not be null");
            }
        }

        /**
         * Executes output type.
         *
         * @return the output type
         */
        @Override
        public ValueType outputType() {
            return terminal.typeSignature().outputType();
        }

        /**
         * Executes children.
         *
         * @return the children
         */
        @Override
        public List<DerivationTree> children() {
            return List.of();
        }
    }
}
