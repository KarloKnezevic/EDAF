/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.tree;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionTreeProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin for token-tree boolean-function optimization.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionTreeProblemPlugin implements ProblemPlugin<VariableLengthVector<Integer>> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "boolean-function-tree";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Cryptographic boolean-function optimization via tokenized expression trees";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public BooleanFunctionTreeProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionTreeProblem(
                config.n(),
                config.criteria(),
                config.criterionWeights(),
                config.maxDepth()
        );
    }
}
