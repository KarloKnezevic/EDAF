package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.discrete.disjunct.AlmostDisjunctMatrixProblem;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctProblemParams;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for exact (t,epsilon)-disjunct matrix optimization objective.
 */
public final class AlmostDisjunctMatrixProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "almost-disjunct-matrix";
    }

    @Override
    public String description() {
        return "Design MxN (t,epsilon)-disjunct matrices via fit3(A)=fit1/(C(N,t)*(N-t))";
    }

    @Override
    public AlmostDisjunctMatrixProblem create(Map<String, Object> params) {
        DisjunctProblemParams parsed = DisjunctProblemParams.from(params);
        return new AlmostDisjunctMatrixProblem(
                parsed.m(),
                parsed.n(),
                parsed.t(),
                parsed.epsilon(),
                parsed.evaluation()
        );
    }
}
