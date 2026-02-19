package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.OneMaxProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for OneMax problem.
 */
public final class OneMaxProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "onemax";
    }

    @Override
    public String description() {
        return "Maximize number of ones in bitstring";
    }

    @Override
    public OneMaxProblem create(Map<String, Object> params) {
        return new OneMaxProblem();
    }
}
