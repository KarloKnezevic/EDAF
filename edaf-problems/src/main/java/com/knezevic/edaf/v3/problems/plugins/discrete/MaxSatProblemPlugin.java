/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.discrete.MaxSatProblem;
import com.knezevic.edaf.v3.problems.discrete.maxsat.DimacsCnf;
import com.knezevic.edaf.v3.problems.util.ProblemResourceLoader;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for MAX-SAT benchmark instances.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MaxSatProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "maxsat";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "MAX-SAT from DIMACS CNF instance";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public MaxSatProblem create(Map<String, Object> params) {
        String instance = Params.str(params, "instance", "classpath:maxsat/uf20-01.cnf");
        DimacsCnf cnf = DimacsCnf.parse(ProblemResourceLoader.readText(instance));
        return new MaxSatProblem(cnf.variableCount(), cnf.clauses());
    }
}
