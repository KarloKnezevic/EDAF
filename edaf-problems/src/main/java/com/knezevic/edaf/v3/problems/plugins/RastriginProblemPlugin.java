package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.RastriginProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Rastrigin problem.
 */
public final class RastriginProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "rastrigin";
    }

    @Override
    public String description() {
        return "Rastrigin benchmark";
    }

    @Override
    public RastriginProblem create(Map<String, Object> params) {
        return new RastriginProblem();
    }
}
