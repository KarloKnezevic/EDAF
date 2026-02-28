/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;

/**
 * Mixed-variable toy benchmark to validate mixed representation plumbing.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MixedVariableToyProblem implements Problem<MixedRealDiscreteVector> {

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "mixed-toy";
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
