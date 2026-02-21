package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for direct truth-table boolean-function optimization.
 */
public final class BooleanFunctionProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "boolean-function";
    }

    @Override
    public String description() {
        return "Cryptographic boolean-function optimization over truth-table bitstring";
    }

    @Override
    public BooleanFunctionProblem create(Map<String, Object> params) {
        CryptoProblemConfig config = CryptoProblemConfig.from(params);
        return new BooleanFunctionProblem(config.n(), config.criteria(), config.criterionWeights());
    }
}
