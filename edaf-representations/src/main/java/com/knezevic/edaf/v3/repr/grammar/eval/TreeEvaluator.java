package com.knezevic.edaf.v3.repr.grammar.eval;

import com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;
import com.knezevic.edaf.v3.repr.grammar.model.Terminal;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates grammar derivation trees for provided variable bindings.
 */
public final class TreeEvaluator {

    /**
     * Evaluates tree to numeric output.
     */
    public double evaluate(DerivationTree tree, EvaluationContext context) {
        if (tree == null) {
            return 0.0;
        }
        if (context == null) {
            context = EvaluationContext.real(java.util.Map.of());
        }
        return evaluateNode(tree, context);
    }

    /**
     * Evaluates tree to boolean output using numeric thresholding.
     */
    public boolean evaluateBoolean(DerivationTree tree, EvaluationContext context) {
        return evaluate(tree, context) > 0.5;
    }

    private double evaluateNode(DerivationTree node, EvaluationContext context) {
        if (node instanceof DerivationTree.TerminalNode terminalNode) {
            return evaluateTerminal(terminalNode, context);
        }

        DerivationTree.RuleNode ruleNode = (DerivationTree.RuleNode) node;
        ProductionRule rule = ruleNode.productionRule();
        OperatorTerminal operator = rule.operator();

        if (operator != null) {
            List<Double> args = new ArrayList<>(ruleNode.children().size());
            for (DerivationTree child : ruleNode.children()) {
                args.add(evaluateNode(child, context));
            }
            double value = operator.operator().evaluate(args);
            return sanitize(value);
        }

        if (ruleNode.children().isEmpty()) {
            return 0.0;
        }
        if (ruleNode.children().size() == 1) {
            return evaluateNode(ruleNode.children().getFirst(), context);
        }
        // Deterministic fallback for alias-like custom grammars.
        return evaluateNode(ruleNode.children().getFirst(), context);
    }

    private double evaluateTerminal(DerivationTree.TerminalNode terminalNode, EvaluationContext context) {
        Terminal terminal = terminalNode.terminal();

        if (terminal instanceof VariableTerminal variable) {
            return sanitize(context.real(variable.variableName()));
        }
        if (terminal instanceof ConstantTerminal constant) {
            return sanitize(constant.value());
        }
        if (terminal instanceof EphemeralConstantTerminal erc) {
            double sampled = terminalNode.sampledValue() == null
                    ? (erc.min() + erc.max()) * 0.5
                    : terminalNode.sampledValue();
            return sanitize(sampled);
        }
        if (terminal instanceof BooleanConstantTerminal boolConstant) {
            return boolConstant.value() ? 1.0 : 0.0;
        }
        if (terminal instanceof OperatorTerminal) {
            // Operator terminals should not appear as leaf AST nodes.
            return 0.0;
        }
        return 0.0;
    }

    private static double sanitize(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        if (value > 1.0e15) {
            return 1.0e15;
        }
        if (value < -1.0e15) {
            return -1.0e15;
        }
        return value;
    }
}
