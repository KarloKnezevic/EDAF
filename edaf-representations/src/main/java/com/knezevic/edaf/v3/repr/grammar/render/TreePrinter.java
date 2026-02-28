/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.render;

import com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Terminal;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Expression renderers for derivation trees.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TreePrinter {

    /**
     * Prints tree in readable infix/function form.
     * @param tree derivation tree
     * @return infix expression
     */
    public String toInfix(DerivationTree tree) {
        return print(tree, Mode.INFIX);
    }

    /**
     * Prints tree in prefix function form.
     * @param tree derivation tree
     * @return prefix expression
     */
    public String toPrefix(DerivationTree tree) {
        return print(tree, Mode.PREFIX);
    }

    /**
     * Prints tree in LaTeX-like mathematical form.
     * @param tree derivation tree
     * @return latex expression
     */
    public String toLatex(DerivationTree tree) {
        return print(tree, Mode.LATEX);
    }

    /**
     * Exports tree as Graphviz DOT graph.
     * @param tree derivation tree
     * @return dot graph text
     */
    public String toDot(DerivationTree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ExpressionTree {\n")
                .append("  rankdir=TB;\n")
                .append("  node [shape=box, style=rounded, fontname=\"IBM Plex Mono\"];\n");
        DotState state = new DotState();
        dot(tree, sb, state, -1);
        sb.append("}\n");
        return sb.toString();
    }

    private String print(DerivationTree tree, Mode mode) {
        if (tree == null) {
            return "0";
        }
        if (tree instanceof DerivationTree.TerminalNode terminalNode) {
            return terminalToString(terminalNode, mode);
        }

        DerivationTree.RuleNode ruleNode = (DerivationTree.RuleNode) tree;
        OperatorTerminal operator = ruleNode.productionRule().operator();
        if (operator == null) {
            if (ruleNode.children().isEmpty()) {
                return ruleNode.nonTerminal().symbol();
            }
            return print(ruleNode.children().getFirst(), mode);
        }

        List<String> args = new ArrayList<>(ruleNode.children().size());
        for (DerivationTree child : ruleNode.children()) {
            args.add(print(child, mode));
        }
        return formatOperator(operator.symbol(), args, mode);
    }

    private String terminalToString(DerivationTree.TerminalNode node, Mode mode) {
        Terminal terminal = node.terminal();
        if (terminal instanceof VariableTerminal variable) {
            return variable.variableName();
        }
        if (terminal instanceof ConstantTerminal constant) {
            return number(constant.value());
        }
        if (terminal instanceof EphemeralConstantTerminal) {
            return node.sampledValue() == null ? "0" : number(node.sampledValue());
        }
        if (terminal instanceof BooleanConstantTerminal boolConstant) {
            return boolConstant.value() ? "true" : "false";
        }
        if (terminal instanceof OperatorTerminal operatorTerminal) {
            return operatorTerminal.symbol();
        }
        return terminal.symbol();
    }

    private String formatOperator(String operator, List<String> args, Mode mode) {
        String op = operator.toLowerCase(Locale.ROOT);
        if (args.isEmpty()) {
            return operator;
        }

        if (mode == Mode.PREFIX) {
            return op + "(" + String.join(", ", args) + ")";
        }

        if (args.size() == 1) {
            if (mode == Mode.LATEX) {
                return switch (op) {
                    case "neg" -> "-" + args.getFirst();
                    case "sqrt" -> "\\sqrt{" + args.getFirst() + "}";
                    case "abs" -> "\\left|" + args.getFirst() + "\\right|";
                    default -> "\\operatorname{" + escapeLatex(op) + "}\\left(" + args.getFirst() + "\\right)";
                };
            }
            return switch (op) {
                case "neg" -> "(-" + args.getFirst() + ")";
                default -> op + "(" + args.getFirst() + ")";
            };
        }

        if (args.size() == 2) {
            if ("+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op)) {
                if (mode == Mode.LATEX && "/".equals(op)) {
                    return "\\frac{" + args.get(0) + "}{" + args.get(1) + "}";
                }
                String displayOp = mode == Mode.LATEX && "*".equals(op) ? "\\cdot" : op;
                return "(" + args.get(0) + " " + displayOp + " " + args.get(1) + ")";
            }
            if ("pow".equals(op)) {
                return mode == Mode.LATEX
                        ? "(" + args.get(0) + ")^{" + args.get(1) + "}"
                        : "pow(" + args.get(0) + ", " + args.get(1) + ")";
            }
            return op + "(" + args.get(0) + ", " + args.get(1) + ")";
        }

        if (args.size() == 3) {
            if ("if_then_else".equals(op) || "if".equals(op)) {
                return mode == Mode.LATEX
                        ? "\\operatorname{if}(" + args.get(0) + ", " + args.get(1) + ", " + args.get(2) + ")"
                        : "if(" + args.get(0) + ", " + args.get(1) + ", " + args.get(2) + ")";
            }
            return op + "(" + args.get(0) + ", " + args.get(1) + ", " + args.get(2) + ")";
        }

        return op + "(" + String.join(", ", args) + ")";
    }

    private void dot(DerivationTree node, StringBuilder sb, DotState state, int parentId) {
        if (node == null) {
            return;
        }
        int id = state.nextId++;
        String label;
        if (node instanceof DerivationTree.TerminalNode terminalNode) {
            label = terminalToString(terminalNode, Mode.INFIX);
        } else {
            DerivationTree.RuleNode ruleNode = (DerivationTree.RuleNode) node;
            OperatorTerminal op = ruleNode.productionRule().operator();
            label = op == null ? ruleNode.nonTerminal().symbol() : op.symbol();
        }

        sb.append("  n").append(id)
                .append(" [label=\"").append(label.replace("\"", "\\\\\"")).append("\"];\n");
        if (parentId >= 0) {
            sb.append("  n").append(parentId).append(" -> n").append(id).append(";\n");
        }

        for (DerivationTree child : node.children()) {
            dot(child, sb, state, id);
        }
    }

    private static String number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (Math.abs(value) >= 1.0e4 || (Math.abs(value) > 0 && Math.abs(value) < 1.0e-4)) {
            return String.format(Locale.ROOT, "%.4e", value);
        }
        String text = String.format(Locale.ROOT, "%.8f", value);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static String escapeLatex(String value) {
        return value.replace("_", "\\_");
    }

    private enum Mode {
        INFIX,
        PREFIX,
        LATEX
    }

    private static final class DotState {
        private int nextId;
    }
}
