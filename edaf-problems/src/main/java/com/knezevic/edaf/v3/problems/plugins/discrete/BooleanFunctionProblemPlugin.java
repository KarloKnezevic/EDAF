/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for direct truth-table boolean-function optimization.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionProblemPlugin implements ProblemPlugin<BitString> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "boolean-function";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Cryptographic boolean-function optimization over truth-table bitstring";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public BooleanFunctionProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionProblem(config.n(), config.criteria(), config.criterionWeights());
    }
}
