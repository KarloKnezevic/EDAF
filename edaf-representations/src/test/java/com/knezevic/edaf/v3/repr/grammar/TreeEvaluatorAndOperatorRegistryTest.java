/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar;

import com.knezevic.edaf.v3.repr.grammar.eval.EvaluationContext;
import com.knezevic.edaf.v3.repr.grammar.eval.TreeEvaluator;
import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.NonTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;
import com.knezevic.edaf.v3.repr.grammar.model.TypeSignature;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorDefinition;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for tree evaluation semantics and protected operators.
 */
class TreeEvaluatorAndOperatorRegistryTest {

    @Test
    void protectedOperatorsRemainFinite() {
        OperatorRegistry registry = new OperatorRegistry();
        double div = registry.find("/").orElseThrow().evaluate(List.of(3.0, 0.0));
        double log = registry.find("log").orElseThrow().evaluate(List.of(-9.0));
        double sqrt = registry.find("sqrt").orElseThrow().evaluate(List.of(-4.0));
        double pow = registry.find("pow").orElseThrow().evaluate(List.of(1.0e6, 200.0));
        assertTrue(Double.isFinite(div));
        assertTrue(Double.isFinite(log));
        assertTrue(Double.isFinite(sqrt));
        assertTrue(Double.isFinite(pow));
    }

    @Test
    void evaluatorComputesProtectedDivisionFromTree() {
        OperatorDefinition division = new OperatorRegistry().find("/").orElseThrow();
        NonTerminal expr = new NonTerminal("Expr", TypeSignature.leaf(ValueType.REAL));
        ProductionRule rule = new ProductionRule(
                "R1",
                expr,
                List.of(
                        new OperatorTerminal(division),
                        new VariableTerminal("x", ValueType.REAL),
                        new ConstantTerminal(0.0)
                )
        );
        DerivationTree tree = new DerivationTree.RuleNode(
                expr,
                rule,
                List.of(
                        new DerivationTree.TerminalNode(new VariableTerminal("x", ValueType.REAL), null, 1),
                        new DerivationTree.TerminalNode(new ConstantTerminal(0.0), null, 1)
                ),
                0
        );

        TreeEvaluator evaluator = new TreeEvaluator();
        double value = evaluator.evaluate(tree, EvaluationContext.real(Map.of("x", 7.5)));
        assertEquals(7.5, value, 1.0e-9);
    }

    @Test
    void evaluatorBooleanThresholdingWorks() {
        TreeEvaluator evaluator = new TreeEvaluator();
        DerivationTree trueLeaf = new DerivationTree.TerminalNode(new ConstantTerminal(0.9), null, 0);
        DerivationTree falseLeaf = new DerivationTree.TerminalNode(new ConstantTerminal(0.1), null, 0);
        assertTrue(evaluator.evaluateBoolean(trueLeaf, EvaluationContext.real(Map.of())));
        assertFalse(evaluator.evaluateBoolean(falseLeaf, EvaluationContext.real(Map.of())));
    }
}
