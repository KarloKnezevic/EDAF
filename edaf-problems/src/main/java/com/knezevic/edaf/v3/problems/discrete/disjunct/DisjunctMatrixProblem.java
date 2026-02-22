package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * t-disjunct matrix design objective using exact paper fitness:
 * {@code fit1(A) = sum_{S in S_t} delta(S)}.
 */
public final class DisjunctMatrixProblem extends AbstractDisjunctMatrixProblem {

    public DisjunctMatrixProblem(int m, int n, int t, DisjunctEvaluationConfig evaluationConfig) {
        super("disjunct-matrix", m, n, t, evaluationConfig);
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        DisjunctMatrix matrix = matrixFrom(genotype);
        long subsets = totalSubsets();
        DisjunctEvaluationMode mode = evaluationConfig().resolve(subsets);
        double fit = mode == DisjunctEvaluationMode.EXACT
                ? DisjunctFitnessFunctions.fit1(matrix, t())
                : DisjunctFitnessFunctions.fit1Sampled(
                        matrix,
                        t(),
                        evaluationConfig().sampleSize(),
                        samplingSeed(genotype)
                );
        return new ScalarFitness(fit);
    }
}
