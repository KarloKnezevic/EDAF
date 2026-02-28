/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.problems.coco.BbobFunctions;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;

/**
 * COCO/BBOB benchmark problem adapter.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CocoBbobProblem implements Problem<RealVector> {

    private final String suite;
    private final int functionId;
    private final int dimension;
    private final int instanceId;

    /**
     * Creates a new CocoBbobProblem instance.
     *
     * @param suite the suite argument
     * @param functionId benchmark function identifier
     * @param dimension the dimension argument
     * @param instanceId benchmark instance identifier
     */
    public CocoBbobProblem(String suite, int functionId, int dimension, int instanceId) {
        this.suite = suite;
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
        return suite + "-f" + functionId + "-d" + dimension + "-i" + instanceId;
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
        double[] values = genotype.values();
        int n = Math.min(values.length, dimension);
        double[] clipped = new double[n];
        System.arraycopy(values, 0, clipped, 0, n);
        double result = BbobFunctions.evaluate(functionId, clipped, instanceId);
        return new ScalarFitness(result);
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
