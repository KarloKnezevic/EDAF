/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.continuous.Cec2014Problem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for CEC 2014-style benchmark suite.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class Cec2014ProblemPlugin implements ProblemPlugin<RealVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "cec2014";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "CEC 2014 style continuous benchmark suite (f1..f30)";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public Cec2014Problem create(Map<String, Object> params) {
        int functionId = Params.integer(params, "functionId", 1);
        int dimension = Params.integer(params, "dimension", 10);
        int instanceId = Params.integer(params, "instanceId", 1);
        return new Cec2014Problem(functionId, dimension, instanceId);
    }
}
