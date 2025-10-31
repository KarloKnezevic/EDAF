package com.knezevic.edaf.testing.problems.misc;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.permutation.PermutationIndividual;

import java.util.Map;

public class NQueensProblem extends AbstractProblem<PermutationIndividual> {
    private final OptimizationType optimizationType = OptimizationType.min;
    public NQueensProblem(Map<String, Object> params) { super(params); }

    @Override
    public void evaluate(PermutationIndividual individual) {
        int[] p = individual.getGenotype();
        int n = p.length;
        int conflicts = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(p[i] - p[j]) == Math.abs(i - j)) conflicts++;
            }
        }
        individual.setFitness(conflicts);
    }

    @Override
    public OptimizationType getOptimizationType() { return optimizationType; }
}

