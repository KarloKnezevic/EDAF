/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.mo;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionMultiObjectiveProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for multi-objective boolean-function optimization.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionMultiObjectiveProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "boolean-function-mo";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Multi-objective cryptographic boolean-function optimization";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public BooleanFunctionMultiObjectiveProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionMultiObjectiveProblem(
                config.n(),
                config.criteria(),
                config.criterionWeights(),
                config.objectiveWeights()
        );
    }
}
