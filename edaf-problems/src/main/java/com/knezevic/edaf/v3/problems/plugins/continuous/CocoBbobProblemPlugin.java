package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.CocoBbobProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for COCO/BBOB benchmark adapter problem.
 */
public final class CocoBbobProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "coco-bbob";
    }

    @Override
    public String description() {
        return "COCO/BBOB adapter problem (function + dimension + instance)";
    }

    @Override
    public CocoBbobProblem create(Map<String, Object> params) {
        String suite = Params.str(params, "suite", "bbob");
        int functionId = Params.integer(params, "functionId", 1);
        int dimension = Params.integer(params, "dimension", 10);
        int instanceId = Params.integer(params, "instanceId", 1);
        return new CocoBbobProblem(suite, functionId, dimension, instanceId);
    }
}
