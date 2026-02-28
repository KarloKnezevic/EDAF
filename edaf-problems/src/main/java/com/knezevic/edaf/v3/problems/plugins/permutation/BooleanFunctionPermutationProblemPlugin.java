/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.permutation;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionPermutationProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for permutation-encoded balanced boolean-function optimization.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionPermutationProblemPlugin implements ProblemPlugin<PermutationVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "boolean-function-permutation";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Cryptographic boolean-function optimization with balanced permutation encoding";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public BooleanFunctionPermutationProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionPermutationProblem(config.n(), config.criteria(), config.criterionWeights());
    }
}
