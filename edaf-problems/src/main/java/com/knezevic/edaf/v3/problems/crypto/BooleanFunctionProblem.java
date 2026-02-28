/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * Boolean-function optimization over direct truth-table bitstring encoding.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionProblem extends AbstractBooleanFunctionProblem<BitString> {

    /**
     * Creates a new BooleanFunctionProblem instance.
     *
     * @param n problem dimension
     * @param criteria fitness criteria list
     * @param criterionWeights the criterionWeights argument
     */
    public BooleanFunctionProblem(int n, List<String> criteria, Map<String, Double> criterionWeights) {
        super(n, criteria, criterionWeights);
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "boolean-function";
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        return evaluateScalarFitness(toTruthTableFromBits(genotype.genes()));
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() != truthTableSize) {
            return List.of("Bitstring length must be 2^n = " + truthTableSize);
        }
        return List.of();
    }
}
