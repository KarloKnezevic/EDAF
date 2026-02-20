package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionMultiObjectiveProblem;
import com.knezevic.edaf.v3.problems.crypto.CryptoProblemConfig;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for multi-objective boolean-function optimization.
 */
public final class BooleanFunctionMultiObjectiveProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "boolean-function-mo";
    }

    @Override
    public String description() {
        return "Multi-objective cryptographic boolean-function optimization";
    }

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
