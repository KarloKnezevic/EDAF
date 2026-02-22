package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * (t,f)-resolvable matrix design objective using exact paper fitness:
 * {@code fit2(A) = |{S in S_t : delta(S) > f}|}.
 */
public final class ResolvableMatrixProblem extends AbstractDisjunctMatrixProblem {

    private final int f;

    public ResolvableMatrixProblem(int m, int n, int t, int f, DisjunctEvaluationConfig evaluationConfig) {
        super("resolvable-matrix", m, n, t, evaluationConfig);
        if (f < 0 || f >= n()) {
            throw new IllegalArgumentException("f must satisfy 0 <= f < N");
        }
        this.f = f;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        DisjunctMatrix matrix = matrixFrom(genotype);
        long subsets = totalSubsets();
        DisjunctEvaluationMode mode = evaluationConfig().resolve(subsets);
        double fit = mode == DisjunctEvaluationMode.EXACT
                ? DisjunctFitnessFunctions.fit2(matrix, t(), f)
                : DisjunctFitnessFunctions.fit2Sampled(
                        matrix,
                        t(),
                        f,
                        evaluationConfig().sampleSize(),
                        samplingSeed(genotype)
                );
        return new ScalarFitness(fit);
    }
}
