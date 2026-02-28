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
 * Rastrigin benchmark.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RastriginProblem implements Problem<RealVector> {

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "rastrigin";
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
        double sum = 10.0 * x.length;
        for (double value : x) {
            sum += value * value - 10.0 * Math.cos(2.0 * Math.PI * value);
        }
        return new ScalarFitness(sum);
    }
}
