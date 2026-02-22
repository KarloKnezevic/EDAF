package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.problems.continuous.Cec2014Problem;
import com.knezevic.edaf.v3.problems.crypto.BooleanFunctionProblem;
import com.knezevic.edaf.v3.problems.discrete.KnapsackProblem;
import com.knezevic.edaf.v3.problems.discrete.MaxSatProblem;
import com.knezevic.edaf.v3.problems.discrete.disjunct.AlmostDisjunctMatrixProblem;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctEvaluationConfig;
import com.knezevic.edaf.v3.problems.discrete.disjunct.DisjunctMatrixProblem;
import com.knezevic.edaf.v3.problems.discrete.disjunct.ResolvableMatrixProblem;
import com.knezevic.edaf.v3.problems.multiobjective.DtlzProblem;
import com.knezevic.edaf.v3.problems.multiobjective.ZdtProblem;
import com.knezevic.edaf.v3.problems.permutation.TsplibTspProblem;
import com.knezevic.edaf.v3.problems.tree.NguyenSymbolicRegressionProblem;
import com.knezevic.edaf.v3.repr.types.BitString;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import com.knezevic.edaf.v3.repr.types.RealVector;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for built-in benchmark problems.
 */
class ProblemSmokeTest {

    @Test
    void oneMaxCountsOnes() {
        OneMaxProblem problem = new OneMaxProblem();
        double fitness = problem.evaluate(new BitString(new boolean[]{true, false, true, true})).scalar();
        assertEquals(3.0, fitness, 1e-9);
    }

    @Test
    void sphereIsZeroAtOrigin() {
        SphereProblem problem = new SphereProblem();
        double fitness = problem.evaluate(new RealVector(new double[]{0.0, 0.0, 0.0})).scalar();
        assertEquals(0.0, fitness, 1e-9);
    }

    @Test
    void tspRouteLengthIsPositive() {
        SmallTspProblem problem = new SmallTspProblem(null);
        double fitness = problem.evaluate(new PermutationVector(new int[]{0, 1, 2, 3, 4, 5, 6, 7})).scalar();
        assertTrue(fitness > 0.0);
    }

    @Test
    void cec2014ReturnsFiniteFitness() {
        Cec2014Problem problem = new Cec2014Problem(10, 5, 1);
        double fitness = problem.evaluate(new RealVector(new double[]{1.0, -2.0, 0.5, 3.0, -1.0})).scalar();
        assertTrue(Double.isFinite(fitness));
    }

    @Test
    void knapsackPenalizesOverweightSolution() {
        KnapsackProblem problem = new KnapsackProblem(
                new int[]{5, 6, 7},
                new int[]{10, 10, 10},
                10,
                4.0
        );
        double feasible = problem.evaluate(new BitString(new boolean[]{true, false, false})).scalar();
        double overweight = problem.evaluate(new BitString(new boolean[]{true, true, true})).scalar();
        assertTrue(feasible > overweight);
    }

    @Test
    void maxSatCountsSatisfiedClauses() {
        MaxSatProblem problem = new MaxSatProblem(3, new int[][]{
                {1, -2},
                {-1, 3},
                {2, 3}
        });
        double fitness = problem.evaluate(new BitString(new boolean[]{true, false, true})).scalar();
        assertEquals(3.0, fitness, 1e-9);
    }

    @Test
    void tsplibProblemEvaluatesPermutation() {
        double[][] coords = new double[][]{
                {0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}
        };
        TsplibTspProblem problem = new TsplibTspProblem("square4", coords);
        double fitness = problem.evaluate(new PermutationVector(new int[]{0, 1, 2, 3})).scalar();
        assertTrue(fitness > 0.0);
    }

    @Test
    void zdtProducesVectorFitness() {
        ZdtProblem problem = new ZdtProblem(1, new double[]{0.5, 0.5});
        var fitness = problem.evaluate(new RealVector(new double[]{0.2, 0.1, 0.4, 0.9}));
        assertEquals(2, fitness.objectives().length);
        assertFalse(fitness.scalarNative());
    }

    @Test
    void dtlzProducesConfiguredObjectiveCount() {
        DtlzProblem problem = new DtlzProblem(2, 3, new double[]{0.34, 0.33, 0.33});
        var fitness = problem.evaluate(new RealVector(new double[]{0.2, 0.3, 0.4, 0.5, 0.6}));
        assertEquals(3, fitness.objectives().length);
        assertFalse(fitness.scalarNative());
    }

    @Test
    void nguyenProblemReturnsFiniteError() {
        NguyenSymbolicRegressionProblem problem = new NguyenSymbolicRegressionProblem(1, 21, -1.0, 1.0);
        var fitness = problem.evaluate(new VariableLengthVector<>(List.of(9, 14, 0, 11, 0, 0)));
        assertTrue(Double.isFinite(fitness.scalar()));
    }

    @Test
    void booleanFunctionProblemReturnsFiniteFitness() {
        BooleanFunctionProblem problem = new BooleanFunctionProblem(
                3,
                List.of("balancedness", "nonlinearity", "algebraic-degree"),
                java.util.Map.of()
        );
        double fitness = problem.evaluate(new BitString(new boolean[]{false, true, false, true, true, false, true, false}))
                .scalar();
        assertTrue(Double.isFinite(fitness));
    }

    @Test
    void disjunctMatrixFamilyProblemsReturnFiniteFitness() {
        BitString identity3x3 = new BitString(new boolean[]{
                true, false, false,
                false, true, false,
                false, false, true
        });

        DisjunctEvaluationConfig eval = DisjunctEvaluationConfig.defaults();
        DisjunctMatrixProblem dm = new DisjunctMatrixProblem(3, 3, 1, eval);
        ResolvableMatrixProblem rm = new ResolvableMatrixProblem(3, 3, 1, 0, eval);
        AlmostDisjunctMatrixProblem adm = new AlmostDisjunctMatrixProblem(3, 3, 1, 0.0, eval);

        assertEquals(0.0, dm.evaluate(identity3x3).scalar(), 1e-9);
        assertEquals(0.0, rm.evaluate(identity3x3).scalar(), 1e-9);
        assertEquals(0.0, adm.evaluate(identity3x3).scalar(), 1e-9);
    }
}
