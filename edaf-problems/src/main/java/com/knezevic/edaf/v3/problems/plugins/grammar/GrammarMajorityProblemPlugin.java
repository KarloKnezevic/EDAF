/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.grammar;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.grammar.GrammarMajorityProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for majority boolean symbolic classification problem.
 */
public final class GrammarMajorityProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "grammar-majority";
    }

    @Override
    public String description() {
        return "Grammar-based symbolic majority classification benchmark";
    }

    @Override
    public GrammarMajorityProblem create(Map<String, Object> params) {
        int bits = Params.integer(params, "bits", 5);
        String prefix = Params.str(params, "variablePrefix", "x");
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);
        return new GrammarMajorityProblem(params, bits, prefix, penalty);
    }
}
