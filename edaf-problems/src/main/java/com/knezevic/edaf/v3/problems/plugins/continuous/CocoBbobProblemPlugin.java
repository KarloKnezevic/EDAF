/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.CocoBbobProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for COCO/BBOB benchmark adapter problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CocoBbobProblemPlugin implements ProblemPlugin<RealVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "coco-bbob";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "COCO/BBOB adapter problem (function + dimension + instance)";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public CocoBbobProblem create(Map<String, Object> params) {
        String suite = Params.str(params, "suite", "bbob");
        int functionId = Params.integer(params, "functionId", 1);
        int dimension = Params.integer(params, "dimension", 10);
        int instanceId = Params.integer(params, "instanceId", 1);
        return new CocoBbobProblem(suite, functionId, dimension, instanceId);
    }
}
