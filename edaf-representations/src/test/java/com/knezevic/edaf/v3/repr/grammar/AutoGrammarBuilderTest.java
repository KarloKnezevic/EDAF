package com.knezevic.edaf.v3.repr.grammar;

import com.knezevic.edaf.v3.repr.grammar.build.AutoGrammarBuilder;
import com.knezevic.edaf.v3.repr.grammar.build.GrammarConfig;
import com.knezevic.edaf.v3.repr.grammar.model.BooleanConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.grammar.model.OperatorTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.VariableTerminal;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for auto grammar construction mode.
 */
class AutoGrammarBuilderTest {

    @Test
    void buildsRealExpressionGrammarWithExpectedLeavesAndOperators() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("mode", "auto");
        params.put("variables", List.of("x", "y"));
        params.put("binary_ops", List.of("+", "*"));
        params.put("unary_ops", List.of("sin"));
        params.put("ternary_ops", List.of("if_then_else"));
        params.put("allow_constants", true);
        params.put("constants", List.of(0.0, 1.0));
        params.put("ephemeral_constants", true);
        params.put("ephemeral_range", List.of(-3.0, 3.0));
        params.put("boolean_mode", false);
        params.put("typed", false);
        GrammarConfig config = GrammarConfig.fromParams(params);
        Grammar grammar = new AutoGrammarBuilder().build(config);

        assertEquals("ExprReal", grammar.startSymbol().symbol());
        boolean hasVariable = grammar.rulesFor(grammar.startSymbol()).stream()
                .flatMap(rule -> rule.childSymbols().stream())
                .anyMatch(symbol -> symbol instanceof VariableTerminal variable && "x".equals(variable.variableName()));
        boolean hasErc = grammar.rulesFor(grammar.startSymbol()).stream()
                .flatMap(rule -> rule.childSymbols().stream())
                .anyMatch(symbol -> symbol instanceof EphemeralConstantTerminal);
        boolean hasPlus = grammar.rulesFor(grammar.startSymbol()).stream()
                .map(rule -> rule.operator())
                .filter(java.util.Objects::nonNull)
                .map(OperatorTerminal::symbol)
                .anyMatch("+"::equals);

        assertTrue(hasVariable, "Expected variable leaves in auto grammar");
        assertTrue(hasErc, "Expected ERC leaf in auto grammar");
        assertTrue(hasPlus, "Expected '+' operator rule in auto grammar");
    }

    @Test
    void buildsBooleanGrammarWhenBooleanModeIsEnabled() {
        GrammarConfig config = GrammarConfig.fromParams(Map.of(
                "mode", "auto",
                "variables", List.of("b0", "b1", "b2"),
                "boolean_mode", true,
                "typed", false,
                "allow_constants", false,
                "ephemeral_constants", false
        ));
        Grammar grammar = new AutoGrammarBuilder().build(config);
        assertEquals("ExprBool", grammar.startSymbol().symbol());

        boolean hasBooleanLiteral = grammar.rulesFor(grammar.startSymbol()).stream()
                .flatMap(rule -> rule.childSymbols().stream())
                .anyMatch(symbol -> symbol instanceof BooleanConstantTerminal);
        boolean hasAnd = grammar.rulesFor(grammar.startSymbol()).stream()
                .map(rule -> rule.operator())
                .filter(java.util.Objects::nonNull)
                .map(OperatorTerminal::symbol)
                .anyMatch("and"::equalsIgnoreCase);
        assertTrue(hasBooleanLiteral, "Expected true/false literal leaves in boolean mode");
        assertTrue(hasAnd, "Expected boolean AND operator in boolean mode");
    }
}
