/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.grammar;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.grammar.GrammarNguyenRegressionProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for Nguyen symbolic regression benchmarks using grammar representation.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarNguyenRegressionProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "grammar-nguyen-regression";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Grammar-based Nguyen symbolic regression benchmark";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public GrammarNguyenRegressionProblem create(Map<String, Object> params) {
        int variant = Params.integer(params, "variant", 1);
        int sampleCount = Params.integer(params, "sampleCount", 40);
        double minX = Params.dbl(params, "minX", -1.0);
        double maxX = Params.dbl(params, "maxX", 1.0);
        String variable = Params.str(params, "variable", "x");
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);
        return new GrammarNguyenRegressionProblem(params, variant, sampleCount, minX, maxX, variable, penalty);
    }
}
