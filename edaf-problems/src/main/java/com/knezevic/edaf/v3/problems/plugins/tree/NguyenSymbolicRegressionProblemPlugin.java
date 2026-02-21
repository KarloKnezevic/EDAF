package com.knezevic.edaf.v3.problems.plugins.tree;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.tree.NguyenSymbolicRegressionProblem;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin factory for Nguyen symbolic regression benchmark.
 */
public final class NguyenSymbolicRegressionProblemPlugin implements ProblemPlugin<VariableLengthVector<Integer>> {

    @Override
    public String type() {
        return "nguyen-sr";
    }

    @Override
    public String description() {
        return "Nguyen symbolic regression suite (variants 1..8)";
    }

    @Override
    public NguyenSymbolicRegressionProblem create(Map<String, Object> params) {
        int variant = Params.integer(params, "variant", 1);
        int samples = Params.integer(params, "samples", 25);
        double minX = Params.dbl(params, "minX", -1.0);
        double maxX = Params.dbl(params, "maxX", 1.0);
        return new NguyenSymbolicRegressionProblem(variant, samples, minX, maxX);
    }
}
