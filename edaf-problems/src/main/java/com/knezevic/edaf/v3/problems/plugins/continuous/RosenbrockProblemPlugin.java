package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.RosenbrockProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Rosenbrock problem.
 */
public final class RosenbrockProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "rosenbrock";
    }

    @Override
    public String description() {
        return "Rosenbrock benchmark";
    }

    @Override
    public RosenbrockProblem create(Map<String, Object> params) {
        return new RosenbrockProblem();
    }
}
