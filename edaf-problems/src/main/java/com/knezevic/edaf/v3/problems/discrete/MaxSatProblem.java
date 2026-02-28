/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.discrete;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;

/**
 * MAX-SAT benchmark over fixed CNF formula.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class MaxSatProblem implements Problem<BitString> {

    private final int variableCount;
    private final int[][] clauses;

    /**
     * Creates a new MaxSatProblem instance.
     *
     * @param variableCount the variableCount argument
     * @param clauses the clauses argument
     */
    public MaxSatProblem(int variableCount, int[][] clauses) {
        this.variableCount = variableCount;
        this.clauses = clauses;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "maxsat";
    }

    /**
     * Returns objective optimization sense.
     *
     * @return objective sense
     */
    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        boolean[] assignment = genotype.genes();
        int satisfied = 0;

        for (int[] clause : clauses) {
            if (isClauseSatisfied(clause, assignment)) {
                satisfied++;
            }
        }
        return new ScalarFitness(satisfied);
    }

    /**
     * Returns feasibility violations.
     *
     * @param genotype candidate genotype
     * @return violation message list
     */
    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() < variableCount) {
            return List.of("Bitstring length must be >= variable count " + variableCount);
        }
        return List.of();
    }

    /**
     * Executes variable count.
     *
     * @return the computed variable count
     */
    public int variableCount() {
        return variableCount;
    }

    /**
     * Executes clause count.
     *
     * @return the computed clause count
     */
    public int clauseCount() {
        return clauses.length;
    }

    private static boolean isClauseSatisfied(int[] clause, boolean[] assignment) {
        for (int literal : clause) {
            int index = Math.abs(literal) - 1;
            if (index < 0 || index >= assignment.length) {
                continue;
            }
            boolean value = assignment[index];
            if (literal < 0) {
                value = !value;
            }
            if (value) {
                return true;
            }
        }
        return false;
    }
}
