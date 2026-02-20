package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionTreeProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin for token-tree boolean-function optimization.
 */
public final class BooleanFunctionTreeProblemPlugin implements ProblemPlugin<VariableLengthVector<Integer>> {

    @Override
    public String type() {
        return "boolean-function-tree";
    }

    @Override
    public String description() {
        return "Cryptographic boolean-function optimization via tokenized expression trees";
    }

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
