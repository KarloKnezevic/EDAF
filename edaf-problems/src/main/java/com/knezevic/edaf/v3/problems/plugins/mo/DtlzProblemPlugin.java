package com.knezevic.edaf.v3.problems.plugins.mo;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.multiobjective.DtlzProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for DTLZ benchmark suite.
 */
public final class DtlzProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "dtlz";
    }

    @Override
    public String description() {
        return "DTLZ multi-objective benchmark (1,2,7)";
    }

    @Override
    public DtlzProblem create(Map<String, Object> params) {
        int functionId = Params.integer(params, "functionId", 2);
        int objectives = Params.integer(params, "objectives", 3);
        double[] weights = parseWeights(Params.list(params, "scalarWeights"), objectives);
        return new DtlzProblem(functionId, objectives, weights);
    }

    private static double[] parseWeights(List<Object> raw, int objectives) {
        if (raw == null || raw.isEmpty()) {
            double[] weights = new double[objectives];
            java.util.Arrays.fill(weights, 1.0 / objectives);
            return weights;
        }
        double[] out = new double[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            out[i] = Double.parseDouble(String.valueOf(raw.get(i)));
        }
        return out;
    }
}
