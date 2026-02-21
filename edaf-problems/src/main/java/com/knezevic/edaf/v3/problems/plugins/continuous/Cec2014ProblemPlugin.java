package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.continuous.Cec2014Problem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for CEC 2014-style benchmark suite.
 */
public final class Cec2014ProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "cec2014";
    }

    @Override
    public String description() {
        return "CEC 2014 style continuous benchmark suite (f1..f30)";
    }

    @Override
    public Cec2014Problem create(Map<String, Object> params) {
        int functionId = Params.integer(params, "functionId", 1);
        int dimension = Params.integer(params, "dimension", 10);
        int instanceId = Params.integer(params, "instanceId", 1);
        return new Cec2014Problem(functionId, dimension, instanceId);
    }
}
