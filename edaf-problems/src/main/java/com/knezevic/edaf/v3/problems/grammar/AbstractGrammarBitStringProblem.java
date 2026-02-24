/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.repr.grammar.GrammarTreeEngine;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * Shared base for grammar-encoded symbolic optimization problems.
 */
public abstract class AbstractGrammarBitStringProblem implements Problem<BitString> {

    protected final GrammarTreeEngine treeEngine;
    protected final double complexityPenalty;

    protected AbstractGrammarBitStringProblem(Map<String, Object> params, double complexityPenalty) {
        this.treeEngine = new GrammarTreeEngine(params);
        this.complexityPenalty = Math.max(0.0, complexityPenalty);
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public List<String> violations(BitString genotype) {
        if (genotype == null) {
            return List.of("Genotype must not be null");
        }
        return List.of();
    }

    /**
     * Returns tree inspection for one genotype.
     */
    protected GrammarTreeEngine.TreeInspection inspect(BitString genotype) {
        return treeEngine.inspect(genotype);
    }

    /**
     * Complexity term used by all grammar tasks.
     */
    protected double complexityTerm(GrammarTreeEngine.TreeInspection inspection) {
        return complexityPenalty * inspection.metrics().size();
    }
}
