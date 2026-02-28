/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.continuous;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.problems.continuous.cec.Cec2014Functions;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;

/**
 * CEC 2014-style continuous benchmark adapter.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class Cec2014Problem implements Problem<RealVector> {

    private final int functionId;
    private final int dimension;
    private final int instanceId;

    /**
     * Creates a new Cec2014Problem instance.
     *
     * @param functionId benchmark function identifier
     * @param dimension the dimension argument
     * @param instanceId benchmark instance identifier
     */
    public Cec2014Problem(int functionId, int dimension, int instanceId) {
        this.functionId = functionId;
        this.dimension = dimension;
        this.instanceId = instanceId;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "cec2014-f" + functionId + "-d" + dimension + "-i" + instanceId;
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
        int n = Math.min(dimension, genotype.length());
        double[] clipped = new double[n];
        System.arraycopy(genotype.values(), 0, clipped, 0, n);
        return new ScalarFitness(Cec2014Functions.evaluate(functionId, clipped, instanceId));
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(RealVector genotype) {
        if (genotype.length() != dimension) {
            return List.of("Expected dimension " + dimension + ", got " + genotype.length());
        }
        return List.of();
    }
}
