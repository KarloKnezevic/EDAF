package com.knezevic.edaf.v3.problems.discrete;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;

/**
 * MAX-SAT benchmark over fixed CNF formula.
 */
public final class MaxSatProblem implements Problem<BitString> {

    private final int variableCount;
    private final int[][] clauses;

    public MaxSatProblem(int variableCount, int[][] clauses) {
        this.variableCount = variableCount;
        this.clauses = clauses;
    }

    @Override
    public String name() {
        return "maxsat";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

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

    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() < variableCount) {
            return List.of("Bitstring length must be >= variable count " + variableCount);
        }
        return List.of();
    }

    public int variableCount() {
        return variableCount;
    }

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
