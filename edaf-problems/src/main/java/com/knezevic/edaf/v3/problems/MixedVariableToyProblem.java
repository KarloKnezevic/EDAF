package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

/**
 * Mixed-variable toy benchmark to validate mixed representation plumbing.
 */
public final class MixedVariableToyProblem implements Problem<MixedRealDiscreteVector> {

    @Override
    public String name() {
        return "mixed-toy";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(MixedRealDiscreteVector genotype) {
        double realSum = 0.0;
        for (double value : genotype.realPart()) {
            realSum += value * value;
        }
        double discretePenalty = 0.0;
        for (int value : genotype.discretePart()) {
            discretePenalty += value;
        }
        return new ScalarFitness(realSum + 0.1 * discretePenalty);
    }
}
