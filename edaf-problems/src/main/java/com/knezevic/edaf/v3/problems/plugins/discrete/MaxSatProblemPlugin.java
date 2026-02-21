package com.knezevic.edaf.v3.problems.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.discrete.MaxSatProblem;
import com.knezevic.edaf.v3.problems.discrete.maxsat.DimacsCnf;
import com.knezevic.edaf.v3.problems.util.ProblemResourceLoader;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for MAX-SAT benchmark instances.
 */
public final class MaxSatProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "maxsat";
    }

    @Override
    public String description() {
        return "MAX-SAT from DIMACS CNF instance";
    }

    @Override
    public MaxSatProblem create(Map<String, Object> params) {
        String instance = Params.str(params, "instance", "classpath:maxsat/uf20-01.cnf");
        DimacsCnf cnf = DimacsCnf.parse(ProblemResourceLoader.readText(instance));
        return new MaxSatProblem(cnf.variableCount(), cnf.clauses());
    }
}
