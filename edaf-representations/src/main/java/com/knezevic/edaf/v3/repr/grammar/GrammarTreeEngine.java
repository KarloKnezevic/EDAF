/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar;

import com.knezevic.edaf.v3.repr.grammar.build.GrammarConfig;
import com.knezevic.edaf.v3.repr.grammar.build.GrammarFactory;
import com.knezevic.edaf.v3.repr.grammar.encoding.GrammarDecisionCodec;
import com.knezevic.edaf.v3.repr.grammar.encoding.GrammarEncoding;
import com.knezevic.edaf.v3.repr.grammar.eval.EvaluationContext;
import com.knezevic.edaf.v3.repr.grammar.eval.TreeEvaluator;
import com.knezevic.edaf.v3.repr.grammar.model.DerivationTree;
import com.knezevic.edaf.v3.repr.grammar.model.Grammar;
import com.knezevic.edaf.v3.repr.grammar.render.TreeMetrics;
import com.knezevic.edaf.v3.repr.grammar.render.TreePrinter;
import com.knezevic.edaf.v3.repr.grammar.serialize.TreeSerializer;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * High-level facade for grammar decoding, evaluation, and visualization exports.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarTreeEngine {

    private final Grammar grammar;
    private final GrammarEncoding encoding;
    private final GrammarDecisionCodec codec;
    private final TreeEvaluator evaluator;
    private final TreePrinter printer;
    private final TreeSerializer serializer;

    /**
     * Creates engine from plugin the input values.
     * @param params configuration the input value map
     */
    public GrammarTreeEngine(Map<String, Object> params) {
        GrammarFactory factory = new GrammarFactory();
        GrammarConfig config = GrammarConfig.fromParams(params);
        this.grammar = factory.build(config);
        this.encoding = GrammarEncoding.from(grammar, config);
        this.codec = new GrammarDecisionCodec();
        this.evaluator = new TreeEvaluator();
        this.printer = new TreePrinter();
        this.serializer = new TreeSerializer();
    }

    /**
     * Decodes genotype into tree and export strings.
     * @param genotype encoded genotype value
     * @return the inspect
     */
    public TreeInspection inspect(BitString genotype) {
        GrammarDecisionCodec.DecodedTree decoded = codec.decode(genotype, grammar, encoding);
        DerivationTree tree = decoded.tree();
        return new TreeInspection(
                tree,
                decoded.decisionVector(),
                decoded.ercValues(),
                printer.toInfix(tree),
                printer.toPrefix(tree),
                printer.toLatex(tree),
                printer.toDot(tree),
                serializer.toMap(tree),
                serializer.toJson(tree),
                TreeMetrics.summarize(tree)
        );
    }

    /**
     * Evaluates genotype as numeric expression.
     * @param genotype encoded genotype value
     * @param context evaluation context
     * @return evaluation result
     */
    public double evaluate(BitString genotype, EvaluationContext context) {
        return evaluator.evaluate(inspect(genotype).tree(), context);
    }

    /**
     * Evaluates genotype as boolean expression.
     * @param genotype encoded genotype value
     * @param context evaluation context
     * @return true if the condition is satisfied; otherwise false
     */
    public boolean evaluateBoolean(BitString genotype, EvaluationContext context) {
        return evaluator.evaluateBoolean(inspect(genotype).tree(), context);
    }

    /**
     * Grammar instance.
     * @return the grammar
     */
    public Grammar grammar() {
        return grammar;
    }

    /**
     * Encoding plan used for fixed-length decision vector mapping.
     * @return the encoding
     */
    public GrammarEncoding encoding() {
        return encoding;
    }

    /**
     * Tree inspection bundle.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    public record TreeInspection(
            DerivationTree tree,
            List<Integer> decisionVector,
            List<Double> ercValues,
            String infix,
            String prefix,
            String latex,
            String dot,
            Map<String, Object> ast,
            String astJson,
            TreeMetrics.Summary metrics
    ) {
    }
}
