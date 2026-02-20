package com.knezevic.edaf.v3.problems.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for boolean-function spectral and ANF statistics.
 */
class BooleanFunctionStatsTest {

    @Test
    void constantZeroHasZeroNonlinearityAndDegree() {
        BooleanFunctionStats stats = BooleanFunctionStats.of(2, new int[]{0, 0, 0, 0});

        assertEquals(0, stats.ones());
        assertEquals(0.0, stats.nonlinearity(), 1e-9);
        assertEquals(0, stats.algebraicDegree());
    }

    @Test
    void xorIsBalancedWithZeroNonlinearityAndDegreeOne() {
        // f(x1,x2) = x1 XOR x2 over masks 00,01,10,11.
        BooleanFunctionStats stats = BooleanFunctionStats.of(2, new int[]{0, 1, 1, 0});

        assertEquals(2, stats.ones());
        assertEquals(0.0, stats.nonlinearity(), 1e-9);
        assertEquals(1, stats.algebraicDegree());
    }

    @Test
    void andFunctionHasExpectedNonlinearityAndDegree() {
        // f(x1,x2) = x1 AND x2.
        BooleanFunctionStats stats = BooleanFunctionStats.of(2, new int[]{0, 0, 0, 1});

        assertEquals(1.0, stats.nonlinearity(), 1e-9);
        assertEquals(2, stats.algebraicDegree());
        assertEquals(1.0, stats.nonlinearityUpperBound(), 1e-9);
    }

    @Test
    void invalidTruthTableLengthFailsFast() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> BooleanFunctionStats.of(3, new int[]{0, 1, 0})
        );
        assertEquals("Truth table length must be 8, got 3", ex.getMessage());
    }
}
