/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.SphereProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Sphere problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SphereProblemPlugin implements ProblemPlugin<RealVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "sphere";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Sphere benchmark";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public SphereProblem create(Map<String, Object> params) {
        return new SphereProblem();
    }
}
