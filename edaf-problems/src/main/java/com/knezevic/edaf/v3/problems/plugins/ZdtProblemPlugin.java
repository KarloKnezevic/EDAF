package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.multiobjective.ZdtProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for ZDT benchmark suite.
 */
public final class ZdtProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "zdt";
    }

    @Override
    public String description() {
        return "ZDT multi-objective benchmark (1,2,3,4,6)";
    }

    @Override
    public ZdtProblem create(Map<String, Object> params) {
        int functionId = Params.integer(params, "functionId", 1);
        double[] weights = parseWeights(Params.list(params, "scalarWeights"));
        return new ZdtProblem(functionId, weights);
    }

    private static double[] parseWeights(List<Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return new double[]{0.5, 0.5};
        }
        double[] out = new double[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            out[i] = Double.parseDouble(String.valueOf(raw.get(i)));
        }
        return out;
    }
}
