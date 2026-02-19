package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Sphere benchmark: minimize sum(x_i^2).
 */
public final class SphereProblem implements Problem<RealVector> {

    @Override
    public String name() {
        return "sphere";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double sum = 0.0;
        for (double value : genotype.values()) {
            sum += value * value;
        }
        return new ScalarFitness(sum);
    }
}
