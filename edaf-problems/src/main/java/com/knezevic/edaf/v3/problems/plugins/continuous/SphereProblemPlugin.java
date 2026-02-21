package com.knezevic.edaf.v3.problems.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.SphereProblem;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for Sphere problem.
 */
public final class SphereProblemPlugin implements ProblemPlugin<RealVector> {

    @Override
    public String type() {
        return "sphere";
    }

    @Override
    public String description() {
        return "Sphere benchmark";
    }

    @Override
    public SphereProblem create(Map<String, Object> params) {
        return new SphereProblem();
    }
}
