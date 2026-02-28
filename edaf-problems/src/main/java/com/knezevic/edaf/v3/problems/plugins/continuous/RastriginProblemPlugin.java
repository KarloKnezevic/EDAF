/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.RastriginProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Rastrigin problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RastriginProblemPlugin implements ProblemPlugin<RealVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "rastrigin";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Rastrigin benchmark";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public RastriginProblem create(Map<String, Object> params) {
        return new RastriginProblem();
    }
}
