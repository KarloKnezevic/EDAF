package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Rosenbrock benchmark.
 */
public final class RosenbrockProblem implements Problem<RealVector> {

    @Override
    public String name() {
        return "rosenbrock";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double[] x = genotype.values();
        double sum = 0.0;
        for (int i = 0; i < x.length - 1; i++) {
            double a = x[i + 1] - x[i] * x[i];
            double b = 1.0 - x[i];
            sum += 100.0 * a * a + b * b;
        }
        return new ScalarFitness(sum);
    }
}
