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
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public abstract class AbstractGrammarBitStringProblem implements Problem<BitString> {

    protected final GrammarTreeEngine treeEngine;
    protected final double complexityPenalty;

    /**
     * Creates a new AbstractGrammarBitStringProblem instance.
     *
     * @param params configuration the input value map
     * @param complexityPenalty the complexityPenalty argument
     */
    protected AbstractGrammarBitStringProblem(Map<String, Object> params, double complexityPenalty) {
        this.treeEngine = new GrammarTreeEngine(params);
        this.complexityPenalty = Math.max(0.0, complexityPenalty);
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype == null) {
            return List.of("Genotype must not be null");
        }
        return List.of();
    }

    /**
     * Returns tree inspection for one genotype.
     * @param genotype candidate genotype
     * @return the inspect
     */
    protected GrammarTreeEngine.TreeInspection inspect(BitString genotype) {
        return treeEngine.inspect(genotype);
    }

    /**
     * Complexity term used by all grammar tasks.
     * @param inspection the inspection argument
     * @return the computed complexity term
     */
    protected double complexityTerm(GrammarTreeEngine.TreeInspection inspection) {
        return complexityPenalty * inspection.metrics().size();
    }
}
