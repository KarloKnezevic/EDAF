/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;
import java.util.Map;

/**
 * Balanced boolean-function optimization with permutation encoding.
 *
 * <p>The first half of permutation positions are interpreted as 1-valued truth-table rows,
 * guaranteeing balancedness by construction when permutation size is exactly 2^n.</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BooleanFunctionPermutationProblem extends AbstractBooleanFunctionProblem<PermutationVector> {

    /**
     * Creates a new BooleanFunctionPermutationProblem instance.
     *
     * @param n problem dimension
     * @param criteria fitness criteria list
     * @param criterionWeights the criterionWeights argument
     */
    public BooleanFunctionPermutationProblem(int n, List<String> criteria, Map<String, Double> criterionWeights) {
        super(n, criteria, criterionWeights);
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "boolean-function-permutation";
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(PermutationVector genotype) {
        return evaluateScalarFitness(toTruthTableFromBalancedPermutation(genotype.order()));
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(PermutationVector genotype) {
        if (genotype.size() != truthTableSize) {
            return List.of("Permutation size must be 2^n = " + truthTableSize);
        }
        return List.of();
    }
}
