/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.mixed;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.MixedVariableToyProblem;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

import java.util.Map;

/**
 * Plugin factory for mixed-variable toy problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MixedToyProblemPlugin implements ProblemPlugin<MixedRealDiscreteVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "mixed-toy";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Mixed variable toy objective";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public MixedVariableToyProblem create(Map<String, Object> params) {
        return new MixedVariableToyProblem();
    }
}
