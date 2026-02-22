package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctProblemParams;
import com.knezevic.edaf.v3.problems.discrete.disjunct.ResolvableMatrixProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for exact (t,f)-resolvable matrix optimization objective.
 */
public final class ResolvableMatrixProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "resolvable-matrix";
    }

    @Override
    public String description() {
        return "Design MxN (t,f)-resolvable matrices via fit2(A)=|{S:delta(S)>f}|";
    }

    @Override
    public ResolvableMatrixProblem create(Map<String, Object> params) {
        DisjunctProblemParams parsed = DisjunctProblemParams.from(params);
        return new ResolvableMatrixProblem(
                parsed.m(),
                parsed.n(),
                parsed.t(),
                parsed.f(),
                parsed.evaluation()
        );
    }
}
