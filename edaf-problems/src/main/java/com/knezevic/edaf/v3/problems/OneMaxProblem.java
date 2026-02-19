package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * OneMax benchmark: maximize number of ones in bitstring.
 */
public final class OneMaxProblem implements Problem<BitString> {

    @Override
    public String name() {
        return "onemax";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        return new ScalarFitness(genotype.ones());
    }
}
