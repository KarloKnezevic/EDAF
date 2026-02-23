package com.knezevic.edaf.v3.repr.grammar.build;

import com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.grammar.model.NonTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.ProductionRule;
import com.knezevic.edaf.v3.repr.grammar.model.TypeSignature;
import com.knezevic.edaf.v3.repr.grammar.model.ValueType;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorDefinition;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorKind;
import com.knezevic.edaf.v3.repr.grammar.ops.OperatorRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds grammar structure from {@link GrammarConfig} for auto mode.
 */
public final class AutoGrammarBuilder {

    private final OperatorRegistry operatorRegistry;

    /**
     * Creates builder with default operator registry.
     */
    public AutoGrammarBuilder() {
        this(new OperatorRegistry());
    }

    /**
     * Creates builder with explicit operator registry.
     */
    public AutoGrammarBuilder(OperatorRegistry operatorRegistry) {
        this.operatorRegistry = operatorRegistry;
    }

    /**
     * Builds grammar from config in auto mode.
     */
    public Grammar build(GrammarConfig config) {
        if (!"auto".equals(config.mode())) {
            throw new IllegalArgumentException("AutoGrammarBuilder requires grammar.mode=auto");
        }

        NonTerminal exprReal = new NonTerminal("ExprReal", TypeSignature.leaf(ValueType.REAL));
        NonTerminal exprBool = new NonTerminal("ExprBool", TypeSignature.leaf(ValueType.BOOL));

        NonTerminal start = config.booleanMode() ? exprBool : exprReal;

        Map<NonTerminal, List<ProductionRule>> rules = new LinkedHashMap<>();
        rules.put(exprReal, new ArrayList<>());
        rules.put(exprBool, new ArrayList<>());

        int ruleCounter = 0;

        if (!config.booleanMode()) {
            // Real-valued variable and constant leaves.
            for (String variable : config.variables()) {
                rules.get(exprReal).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprReal,
                        List.of(new VariableTerminal(variable, ValueType.REAL))
                ));
            }
            if (config.allowConstants()) {
                for (double constant : config.constants()) {
                    rules.get(exprReal).add(new ProductionRule(
                            "R" + (++ruleCounter),
                            exprReal,
                            List.of(new ConstantTerminal(constant))
                    ));
                }
            }
            if (config.ephemeralConstants()) {
                rules.get(exprReal).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprReal,
                        List.of(new EphemeralConstantTerminal(
                                config.ephemeralDistribution(),
                                config.ephemeralMin(),
                                config.ephemeralMax()))
                ));
            }

            for (String unary : config.unaryOps()) {
                OperatorDefinition operator = requireOperator(unary, OperatorKind.REAL_UNARY);
                rules.get(exprReal).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprReal,
                        List.of(new OperatorTerminal(operator), exprReal)
                ));
            }
            for (String binary : config.binaryOps()) {
                OperatorDefinition operator = requireOperator(binary, OperatorKind.REAL_BINARY);
                rules.get(exprReal).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprReal,
                        List.of(new OperatorTerminal(operator), exprReal, exprReal)
                ));
            }

            for (String ternary : config.ternaryOps()) {
                OperatorDefinition operator = requireOperator(ternary, OperatorKind.REAL_TERNARY);
                if (config.typed()) {
                    rules.get(exprReal).add(new ProductionRule(
                            "R" + (++ruleCounter),
                            exprReal,
                            List.of(new OperatorTerminal(operator), exprBool, exprReal, exprReal)
                    ));
                } else {
                    rules.get(exprReal).add(new ProductionRule(
                            "R" + (++ruleCounter),
                            exprReal,
                            List.of(new OperatorTerminal(operator), exprReal, exprReal, exprReal)
                    ));
                }
            }
        }

        // Boolean-mode leaves and operators (also available for typed real grammars).
        if (config.booleanMode() || config.typed()) {
            for (String variable : config.variables()) {
                rules.get(exprBool).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprBool,
                        List.of(new VariableTerminal(variable, ValueType.BOOL))
                ));
            }
            rules.get(exprBool).add(new ProductionRule(
                    "R" + (++ruleCounter),
                    exprBool,
                    List.of(new BooleanConstantTerminal(false))
            ));
            rules.get(exprBool).add(new ProductionRule(
                    "R" + (++ruleCounter),
                    exprBool,
                    List.of(new BooleanConstantTerminal(true))
            ));

            List<String> boolUnary = List.of("not");
            List<String> boolBinary = List.of("and", "or", "xor", "nand", "nor");
            List<String> boolTernary = List.of("if");

            for (String name : boolUnary) {
                OperatorDefinition operator = requireOperator(name, OperatorKind.BOOLEAN_UNARY);
                rules.get(exprBool).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprBool,
                        List.of(new OperatorTerminal(operator), exprBool)
                ));
            }
            for (String name : boolBinary) {
                OperatorDefinition operator = requireOperator(name, OperatorKind.BOOLEAN_BINARY);
                rules.get(exprBool).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprBool,
                        List.of(new OperatorTerminal(operator), exprBool, exprBool)
                ));
            }
            for (String name : boolTernary) {
                OperatorDefinition operator = requireOperator(name, OperatorKind.BOOLEAN_TERNARY);
                rules.get(exprBool).add(new ProductionRule(
                        "R" + (++ruleCounter),
                        exprBool,
                        List.of(new OperatorTerminal(operator), exprBool, exprBool, exprBool)
                ));
            }
        }

        // Remove empty non-terminal groups to avoid unreachable noise.
        rules.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("mode", "auto");
        metadata.put("maxDepth", config.maxDepth());
        metadata.put("typed", config.typed());
        metadata.put("booleanMode", config.booleanMode());
        metadata.put("variables", config.variables());

        return new Grammar(start, rules, metadata);
    }

    private OperatorDefinition requireOperator(String name, OperatorKind expectedKind) {
        OperatorDefinition operator = operatorRegistry.find(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown operator in auto grammar: " + name));
        if (operator.kind() != expectedKind) {
            throw new IllegalArgumentException("Operator '" + name + "' has incompatible kind "
                    + operator.kind().name().toLowerCase(Locale.ROOT)
                    + " (expected " + expectedKind.name().toLowerCase(Locale.ROOT) + ")");
        }
        return operator;
    }
}
