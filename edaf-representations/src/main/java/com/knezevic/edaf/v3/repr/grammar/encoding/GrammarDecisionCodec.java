/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.encoding;

import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.grammar.model.GrammarSymbol;
import com.knezevic.edaf.v3.repr.grammar.model.NonTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;
import com.knezevic.edaf.v3.repr.grammar.model.Terminal;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Deterministic decoder mapping fixed-length bitstrings to grammar derivation trees.
 *
 * <p>The mapping follows breadth-first (BFS) non-terminal expansion up to max depth.
 * This yields a stable decision vector representation for discrete EDA drivers.</p>
 */
public final class GrammarDecisionCodec {

    /**
     * Decodes one genotype into derivation tree and decision metadata.
     */
    public DecodedTree decode(BitString genotype, Grammar grammar, GrammarEncoding encoding) {
        if (genotype == null) {
            throw new IllegalArgumentException("genotype must not be null");
        }
        if (grammar == null) {
            throw new IllegalArgumentException("grammar must not be null");
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null");
        }

        BitCursor cursor = new BitCursor(genotype.genes());
        MutableNode root = MutableNode.nonTerminal(grammar.startSymbol(), 0);

        Deque<MutableNode> queue = new ArrayDeque<>();
        queue.add(root);

        List<Integer> decisions = new ArrayList<>();
        List<Double> ercValues = new ArrayList<>();

        int expanded = 0;
        while (!queue.isEmpty() && expanded < encoding.maxNodes()) {
            MutableNode node = queue.removeFirst();
            List<ProductionRule> candidates = selectCandidates(
                    grammar.rulesFor(node.nonTerminal),
                    node.depth,
                    encoding.maxDepth()
            );
            if (candidates.isEmpty()) {
                node.terminal = fallbackTerminal(node.nonTerminal, node.depth, grammar);
                continue;
            }

            int rawDecision = cursor.nextInt(encoding.bitsPerDecision());
            int selectedIndex = Math.floorMod(rawDecision, candidates.size());
            decisions.add(selectedIndex);

            ProductionRule rule = candidates.get(selectedIndex);
            node.rule = rule;
            node.children = new ArrayList<>();

            int childDepth = node.depth + 1;
            for (GrammarSymbol symbol : rule.childSymbols()) {
                if (symbol instanceof NonTerminal childNt) {
                    if (childDepth > encoding.maxDepth()) {
                        MutableNode forcedTerminal = MutableNode.terminal(
                                fallbackTerminal(childNt, childDepth, grammar)
                        );
                        node.children.add(forcedTerminal);
                    } else {
                        MutableNode child = MutableNode.nonTerminal(childNt, childDepth);
                        node.children.add(child);
                        queue.addLast(child);
                    }
                } else if (symbol instanceof Terminal terminal) {
                    Double sampledValue = null;
                    if (terminal instanceof EphemeralConstantTerminal erc) {
                        sampledValue = sampleErc(cursor, encoding.bitsPerErc(), erc.min(), erc.max());
                        ercValues.add(sampledValue);
                    }
                    node.children.add(MutableNode.terminal(
                            new DerivationTree.TerminalNode(terminal, sampledValue, childDepth)
                    ));
                }
            }
            expanded++;
        }

        // Any unresolved nodes are deterministically closed with fallback terminals.
        for (MutableNode unresolved : queue) {
            unresolved.terminal = fallbackTerminal(unresolved.nonTerminal, unresolved.depth, grammar);
        }

        DerivationTree tree = root.toImmutable(grammar);
        return new DecodedTree(tree, List.copyOf(decisions), List.copyOf(ercValues), cursor.consumedBits());
    }

    /**
     * Returns BFS decision vector only.
     */
    public List<Integer> decisionVector(BitString genotype, Grammar grammar, GrammarEncoding encoding) {
        return decode(genotype, grammar, encoding).decisionVector();
    }

    private List<ProductionRule> selectCandidates(List<ProductionRule> rules, int depth, int maxDepth) {
        if (depth >= maxDepth - 1) {
            List<ProductionRule> leaves = new ArrayList<>();
            for (ProductionRule rule : rules) {
                if (rule.isLeafCandidate()) {
                    leaves.add(rule);
                }
            }
            if (!leaves.isEmpty()) {
                return leaves;
            }

            // Depth reached but no strict leaf: prefer minimal expansion rules to avoid blow-up.
            int minChildren = Integer.MAX_VALUE;
            for (ProductionRule rule : rules) {
                minChildren = Math.min(minChildren, rule.nonTerminalChildCount());
            }
            List<ProductionRule> minimal = new ArrayList<>();
            for (ProductionRule rule : rules) {
                if (rule.nonTerminalChildCount() == minChildren) {
                    minimal.add(rule);
                }
            }
            return minimal;
        }
        return rules;
    }

    private DerivationTree.TerminalNode fallbackTerminal(NonTerminal symbol, int depth, Grammar grammar) {
        List<ProductionRule> productions = new ArrayList<>(grammar.rulesFor(symbol));
        productions.sort(Comparator.comparingInt(ProductionRule::nonTerminalChildCount));

        for (ProductionRule rule : productions) {
            for (GrammarSymbol childSymbol : rule.childSymbols()) {
                if (childSymbol instanceof Terminal terminal) {
                    Double sampled = terminal instanceof EphemeralConstantTerminal erc
                            ? (erc.min() + erc.max()) * 0.5
                            : null;
                    return new DerivationTree.TerminalNode(terminal, sampled, depth);
                }
            }
        }

        // Last-resort deterministic fallback preserving output type.
        if (symbol.typeSignature().outputType() == ValueType.BOOL) {
            return new DerivationTree.TerminalNode(new com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal(false), null, depth);
        }
        return new DerivationTree.TerminalNode(new ConstantTerminal(0.0), 0.0, depth);
    }

    private static double sampleErc(BitCursor cursor, int bits, double min, double max) {
        if (bits <= 0) {
            return (min + max) * 0.5;
        }
        int width = Math.min(30, bits);
        int raw = cursor.nextInt(width);
        int maxRaw = (1 << width) - 1;
        if (maxRaw <= 0) {
            return (min + max) * 0.5;
        }
        double alpha = raw / (double) maxRaw;
        return min + alpha * (max - min);
    }

    /**
     * Decode result bundle.
     */
    public record DecodedTree(
            DerivationTree tree,
            List<Integer> decisionVector,
            List<Double> ercValues,
            int consumedBits
    ) {
    }

    /**
     * Mutable decoding node used only during BFS construction.
     */
    private static final class MutableNode {
        private final NonTerminal nonTerminal;
        private final int depth;
        private ProductionRule rule;
        private List<MutableNode> children;
        private DerivationTree.TerminalNode terminal;

        private MutableNode(NonTerminal nonTerminal, int depth) {
            this.nonTerminal = nonTerminal;
            this.depth = depth;
        }

        private static MutableNode nonTerminal(NonTerminal nonTerminal, int depth) {
            return new MutableNode(nonTerminal, depth);
        }

        private static MutableNode terminal(DerivationTree.TerminalNode terminal) {
            MutableNode node = new MutableNode(null, terminal.depth());
            node.terminal = terminal;
            return node;
        }

        private DerivationTree toImmutable(Grammar grammar) {
            if (terminal != null) {
                return terminal;
            }
            if (rule == null) {
                return new DerivationTree.RuleNode(
                        nonTerminal,
                        grammar.rulesFor(nonTerminal).getFirst(),
                        List.of(),
                        depth
                );
            }
            List<DerivationTree> immutableChildren = new ArrayList<>();
            if (children != null) {
                for (MutableNode child : children) {
                    immutableChildren.add(child.toImmutable(grammar));
                }
            }
            return new DerivationTree.RuleNode(nonTerminal, rule, immutableChildren, depth);
        }
    }
}
