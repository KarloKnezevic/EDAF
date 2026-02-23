package com.knezevic.edaf.v3.repr.grammar;

import com.knezevic.edaf.v3.repr.grammar.build.GrammarConfig;
import com.knezevic.edaf.v3.repr.grammar.build.GrammarParser;
import com.knezevic.edaf.v3.repr.grammar.model.EphemeralConstantTerminal;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for custom BNF parser and validation safeguards.
 */
class GrammarParserTest {

    @Test
    void parsesMultilineGrammarWithCommentsAndErc() {
        String bnf = """
                # Symbolic grammar with continuation lines and comments
                <Expr> ::= + <Expr> <Expr> | sin <Expr> | x
                  | 1.0
                  | erc  // ephemeral random constant
                """;
        GrammarParser parser = new GrammarParser();
        GrammarConfig config = GrammarConfig.fromParams(Map.of(
                "mode", "custom",
                "variables", List.of("x"),
                "ephemeral_constants", true,
                "ephemeral_range", List.of(-2.0, 2.0)
        ));

        Grammar grammar = parser.parse(bnf, config, "inline-test");
        assertEquals("Expr", grammar.startSymbol().symbol());
        assertTrue(grammar.rulesFor(grammar.startSymbol()).size() >= 4);
        boolean hasErc = grammar.rulesFor(grammar.startSymbol()).stream()
                .flatMap(rule -> rule.childSymbols().stream())
                .anyMatch(symbol -> symbol instanceof EphemeralConstantTerminal);
        assertTrue(hasErc, "Expected ERC terminal in custom grammar");
    }

    @Test
    void rejectsUnknownNonTerminal() {
        String bnf = """
                <Expr> ::= + <Expr> <Missing> | x
                """;
        GrammarParser parser = new GrammarParser();
        GrammarConfig config = GrammarConfig.fromParams(Map.of(
                "mode", "custom",
                "variables", List.of("x"),
                "ephemeral_constants", false
        ));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(bnf, config, "unknown-nt-test"));
        assertTrue(error.getMessage().contains("Unknown non-terminal"));
    }

    @Test
    void rejectsUnreachableNonTerminal() {
        String bnf = """
                <Expr> ::= x
                <Unused> ::= 1
                """;
        GrammarParser parser = new GrammarParser();
        GrammarConfig config = GrammarConfig.fromParams(Map.of(
                "mode", "custom",
                "variables", List.of("x")
        ));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(bnf, config, "unreachable-test"));
        assertTrue(error.getMessage().contains("Unreachable non-terminal"));
    }

    @Test
    void rejectsRecursiveGrammarWithoutBaseCase() {
        String bnf = """
                <Expr> ::= + <Expr> <Expr>
                """;
        GrammarParser parser = new GrammarParser();
        GrammarConfig config = GrammarConfig.fromParams(Map.of(
                "mode", "custom",
                "variables", List.of("x")
        ));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(bnf, config, "no-base-case-test"));
        assertTrue(error.getMessage().contains("recursive without base case"));
    }
}
