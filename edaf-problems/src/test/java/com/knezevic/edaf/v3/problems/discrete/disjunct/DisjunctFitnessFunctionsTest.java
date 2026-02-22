package com.knezevic.edaf.v3.problems.discrete.disjunct;

import com.knezevic.edaf.v3.repr.types.BitString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exact regression tests for fit1/fit2/fit3 formulas.
 */
class DisjunctFitnessFunctionsTest {

    @Test
    void identityMatrixHasZeroDeviationForT1() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, false, false},
                {false, true, false},
                {false, false, true}
        });

        assertEquals(0L, DisjunctFitnessFunctions.fit1(matrix, 1));
        assertEquals(0L, DisjunctFitnessFunctions.fit2(matrix, 1, 0));
        assertEquals(0.0, DisjunctFitnessFunctions.fit3(matrix, 1), 1.0e-12);
    }

    @Test
    void allOnesMatrixMatchesHandComputedValues() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, true, true},
                {true, true, true}
        });

        assertEquals(6L, DisjunctFitnessFunctions.fit1(matrix, 1));
        assertEquals(3L, DisjunctFitnessFunctions.fit2(matrix, 1, 1));
        assertEquals(1.0, DisjunctFitnessFunctions.fit3(matrix, 1), 1.0e-12);
    }

    @Test
    void bitstringColumnMajorEncodingMatchesDenseMatrix() {
        DisjunctMatrix fromDense = DisjunctMatrix.fromDense(new boolean[][]{
                {true, false, true},
                {false, true, false}
        });
        DisjunctMatrix fromBitString = DisjunctMatrix.fromBitString(
                new BitString(new boolean[]{true, false, false, true, true, false}),
                2,
                3
        );

        assertEquals(DisjunctFitnessFunctions.fit1(fromDense, 1), DisjunctFitnessFunctions.fit1(fromBitString, 1));
        assertEquals(DisjunctFitnessFunctions.fit2(fromDense, 1, 0), DisjunctFitnessFunctions.fit2(fromBitString, 1, 0));
        assertEquals(
                DisjunctFitnessFunctions.fit3(fromDense, 1),
                DisjunctFitnessFunctions.fit3(fromBitString, 1),
                1.0e-12
        );
    }

    @Test
    void sampledEstimatorsAreFiniteAndCloseOnUniformMatrix() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, true, true},
                {true, true, true}
        });

        double fit1 = DisjunctFitnessFunctions.fit1Sampled(matrix, 1, 4096L, 77L);
        double fit2 = DisjunctFitnessFunctions.fit2Sampled(matrix, 1, 1, 4096L, 91L);
        double fit3 = DisjunctFitnessFunctions.fit3Sampled(matrix, 1, 4096L, 123L);

        assertEquals(6.0, fit1, 0.35);
        assertEquals(3.0, fit2, 0.2);
        assertEquals(1.0, fit3, 0.02);
    }
}
