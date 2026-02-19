package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Rastrigin benchmark.
 */
public final class RastriginProblem implements Problem<RealVector> {

    @Override
    public String name() {
        return "rastrigin";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double[] x = genotype.values();
        double sum = 10.0 * x.length;
        for (double value : x) {
            sum += value * value - 10.0 * Math.cos(2.0 * Math.PI * value);
        }
        return new ScalarFitness(sum);
    }
}
