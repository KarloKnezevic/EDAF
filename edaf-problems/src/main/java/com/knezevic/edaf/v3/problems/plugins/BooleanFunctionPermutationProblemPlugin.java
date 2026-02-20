package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionPermutationProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin for permutation-encoded balanced boolean-function optimization.
 */
public final class BooleanFunctionPermutationProblemPlugin implements ProblemPlugin<PermutationVector> {

    @Override
    public String type() {
        return "boolean-function-permutation";
    }

    @Override
    public String description() {
        return "Cryptographic boolean-function optimization with balanced permutation encoding";
    }

    @Override
    public BooleanFunctionPermutationProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionPermutationProblem(config.n(), config.criteria(), config.criterionWeights());
    }
}
