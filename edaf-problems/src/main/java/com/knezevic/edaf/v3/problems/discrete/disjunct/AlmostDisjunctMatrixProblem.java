package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * (t,epsilon)-disjunct matrix design objective using exact paper fitness:
 * {@code fit3(A) = fit1(A)/(C(N,t)*(N-t))}.
 */
public final class AlmostDisjunctMatrixProblem extends AbstractDisjunctMatrixProblem {

    private final double epsilon;

    public AlmostDisjunctMatrixProblem(int m, int n, int t, double epsilon, DisjunctEvaluationConfig evaluationConfig) {
        super("almost-disjunct-matrix", m, n, t, evaluationConfig);
        if (epsilon < 0.0 || epsilon > 1.0) {
            throw new IllegalArgumentException("epsilon must be in [0,1]");
        }
        this.epsilon = epsilon;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        DisjunctMatrix matrix = matrixFrom(genotype);
        long subsets = totalSubsets();
        DisjunctEvaluationMode mode = evaluationConfig().resolve(subsets);
        double fit = mode == DisjunctEvaluationMode.EXACT
                ? DisjunctFitnessFunctions.fit3(matrix, t())
                : DisjunctFitnessFunctions.fit3Sampled(
                        matrix,
                        t(),
                        evaluationConfig().sampleSize(),
                        samplingSeed(genotype)
                );
        return new ScalarFitness(fit);
    }

    /**
     * Exposes configured epsilon threshold for reporting/diagnostics.
     */
    public double epsilon() {
        return epsilon;
    }
}
