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
 */
public final class GrammarXorProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "grammar-xor";
    }

    @Override
    public String description() {
        return "Grammar-based symbolic XOR classification benchmark";
    }

    @Override
    public GrammarXorProblem create(Map<String, Object> params) {
        int bits = Params.integer(params, "bits", 2);
        String prefix = Params.str(params, "variablePrefix", "x");
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);
        return new GrammarXorProblem(params, bits, prefix, penalty);
    }
}
