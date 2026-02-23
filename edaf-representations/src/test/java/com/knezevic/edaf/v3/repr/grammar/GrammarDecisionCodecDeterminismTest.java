package com.knezevic.edaf.v3.repr.grammar;

import com.knezevic.edaf.v3.repr.grammar.build.GrammarConfig;
import com.knezevic.edaf.v3.repr.grammar.build.GrammarFactory;
import com.knezevic.edaf.v3.repr.grammar.encoding.GrammarDecisionCodec;
import com.knezevic.edaf.v3.repr.grammar.encoding.GrammarEncoding;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.types.BitString;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for deterministic grammar decision decoding and ERC sampling.
 */
class GrammarDecisionCodecDeterminismTest {

    @Test
    void decodeIsDeterministicForSameGenome() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("mode", "auto");
        params.put("variables", List.of("x"));
        params.put("binary_ops", List.of("+"));
        params.put("unary_ops", List.of("sin"));
        params.put("ternary_ops", List.of());
        params.put("allow_constants", false);
        params.put("ephemeral_constants", true);
        params.put("ephemeral_range", List.of(-5.0, 5.0));
        params.put("max_depth", 5);
        params.put("bits_per_decision", 6);
        params.put("bits_per_erc", 12);
        params.put("max_nodes", 256);
        GrammarConfig config = GrammarConfig.fromParams(params);
        Grammar grammar = new GrammarFactory().build(config);
        GrammarEncoding encoding = GrammarEncoding.from(grammar, config);
        GrammarDecisionCodec codec = new GrammarDecisionCodec();

        boolean[] genes = new boolean[encoding.genomeLength()];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = (i % 3) == 0 || (i % 7) == 0;
        }
        BitString genotype = new BitString(genes);

        GrammarDecisionCodec.DecodedTree first = codec.decode(genotype, grammar, encoding);
        GrammarDecisionCodec.DecodedTree second = codec.decode(genotype, grammar, encoding);
        assertEquals(first.decisionVector(), second.decisionVector());
        assertEquals(first.ercValues(), second.ercValues());
        assertEquals(first.tree(), second.tree());
        assertEquals(first.consumedBits(), second.consumedBits());
    }

    @Test
    void ercValuesStayInsideConfiguredRange() {
        Map<String, Object> params = Map.of(
                "mode", "custom",
                "variables", List.of("x"),
                "ephemeral_constants", true,
                "ephemeral_range", List.of(-2.0, 2.0),
                "max_depth", 4
        );
        GrammarConfig config = GrammarConfig.fromParams(params);
        Grammar grammar = new com.knezevic.edaf.v3.repr.grammar.build.GrammarParser().parse("""
                <Expr> ::= erc | x
                """, config, "erc-inline");
        GrammarEncoding encoding = GrammarEncoding.from(grammar, config);
        GrammarDecisionCodec codec = new GrammarDecisionCodec();

        boolean[] genes = new boolean[encoding.genomeLength()];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = (i & 1) == 0;
        }
        BitString genotype = new BitString(genes);
        GrammarDecisionCodec.DecodedTree decoded = codec.decode(genotype, grammar, encoding);

        assertTrue(decoded.ercValues().stream().allMatch(value -> value >= -2.0 && value <= 2.0));
    }
}
