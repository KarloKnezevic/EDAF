/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctProblemParams;
import com.knezevic.edaf.v3.problems.discrete.disjunct.ResolvableMatrixProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for exact (t,f)-resolvable matrix optimization objective.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class ResolvableMatrixProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "resolvable-matrix";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Design MxN (t,f)-resolvable matrices via fit2(A)=|{S:delta(S)>f}|";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public ResolvableMatrixProblem create(Map<String, Object> params) {
        DisjunctProblemParams parsed = DisjunctProblemParams.from(params);
        return new ResolvableMatrixProblem(
                parsed.m(),
                parsed.n(),
                parsed.t(),
                parsed.f(),
                parsed.evaluation()
        );
    }
}
