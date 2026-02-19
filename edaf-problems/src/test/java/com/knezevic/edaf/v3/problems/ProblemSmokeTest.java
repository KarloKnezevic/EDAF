package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.repr.types.BitString;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import com.knezevic.edaf.v3.repr.types.RealVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
