/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.grammar;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.grammar.GrammarXorProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for XOR boolean symbolic classification problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarXorProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "grammar-xor";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Grammar-based symbolic XOR classification benchmark";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public GrammarXorProblem create(Map<String, Object> params) {
        int bits = Params.integer(params, "bits", 2);
        String prefix = Params.str(params, "variablePrefix", "x");
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);
        return new GrammarXorProblem(params, bits, prefix, penalty);
    }
}
