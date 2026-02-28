/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.RealVector;

/**
 * Rosenbrock benchmark.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RosenbrockProblem implements Problem<RealVector> {

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "rosenbrock";
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
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
