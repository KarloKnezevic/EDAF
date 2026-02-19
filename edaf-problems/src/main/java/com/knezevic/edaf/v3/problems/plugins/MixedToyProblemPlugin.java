package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.MixedVariableToyProblem;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

import java.util.Map;

/**
 * Plugin factory for mixed-variable toy problem.
 */
public final class MixedToyProblemPlugin implements ProblemPlugin<MixedRealDiscreteVector> {

    @Override
    public String type() {
        return "mixed-toy";
    }

    @Override
    public String description() {
        return "Mixed variable toy objective";
    }

    @Override
    public MixedVariableToyProblem create(Map<String, Object> params) {
        return new MixedVariableToyProblem();
    }
}
