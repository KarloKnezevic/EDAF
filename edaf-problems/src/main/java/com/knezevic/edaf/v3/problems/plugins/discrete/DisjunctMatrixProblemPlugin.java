package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctMatrixProblem;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctProblemParams;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for exact t-disjunct matrix optimization objective.
 */
public final class DisjunctMatrixProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "disjunct-matrix";
    }

    @Override
    public String description() {
        return "Design MxN t-disjunct binary matrices via fit1(A)=sum delta(S)";
    }

    @Override
    public DisjunctMatrixProblem create(Map<String, Object> params) {
        DisjunctProblemParams parsed = DisjunctProblemParams.from(params);
        return new DisjunctMatrixProblem(parsed.m(), parsed.n(), parsed.t(), parsed.evaluation());
    }
}
