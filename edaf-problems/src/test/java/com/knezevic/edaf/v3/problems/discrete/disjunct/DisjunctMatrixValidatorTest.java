package com.knezevic.edaf.v3.problems.discrete.disjunct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validation tests for DM/RM/ADM definitions in exact and sampled modes.
 */
class DisjunctMatrixValidatorTest {

    @Test
    void exactValidationAcceptsIdentityMatrix() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, false, false},
                {false, true, false},
                {false, false, true}
        });
        DisjunctMatrixValidationOptions options =
                new DisjunctMatrixValidationOptions(100_000L, 0L, 0.95, 0.02, 7L);

        DisjunctMatrixValidationResult dm = DisjunctMatrixValidator.validateDisjunct(matrix, 1, options);
        DisjunctMatrixValidationResult rm = DisjunctMatrixValidator.validateResolvable(matrix, 1, 0, options);
        DisjunctMatrixValidationResult adm = DisjunctMatrixValidator.validateAlmostDisjunct(matrix, 1, 0.0, options);

        assertTrue(dm.valid());
        assertTrue(dm.exact());
        assertTrue(rm.valid());
        assertTrue(rm.exact());
        assertTrue(adm.valid());
        assertTrue(adm.exact());
    }

    @Test
    void exactValidationRejectsAllOnesMatrix() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, true, true},
                {true, true, true}
        });
        DisjunctMatrixValidationOptions options =
                new DisjunctMatrixValidationOptions(100_000L, 0L, 0.95, 0.02, 13L);

        DisjunctMatrixValidationResult dm = DisjunctMatrixValidator.validateDisjunct(matrix, 1, options);
        DisjunctMatrixValidationResult rm = DisjunctMatrixValidator.validateResolvable(matrix, 1, 0, options);
        DisjunctMatrixValidationResult adm = DisjunctMatrixValidator.validateAlmostDisjunct(matrix, 1, 0.5, options);

        assertFalse(dm.valid());
        assertTrue(dm.exact());
        assertTrue(dm.hasWitness());

        assertFalse(rm.valid());
        assertTrue(rm.exact());
        assertTrue(rm.hasWitness());

        assertFalse(adm.valid());
        assertTrue(adm.exact());
        assertTrue(adm.hasWitness());
        assertEquals(2, adm.witnessDeviation());
    }

    @Test
    void sampledValidationReportsProbabilisticBoundForLargeCombinationSpace() {
        DisjunctMatrix matrix = DisjunctMatrix.fromDense(new boolean[][]{
                {true, true, true, true, true, true},
                {true, true, true, true, true, true}
        });
        DisjunctMatrixValidationOptions options =
                new DisjunctMatrixValidationOptions(2L, 512L, 0.95, 0.05, 42L);

        DisjunctMatrixValidationResult sampled = DisjunctMatrixValidator.validateDisjunct(matrix, 2, options);

        assertEquals(DisjunctMatrixValidationMode.SAMPLED, sampled.mode());
        assertFalse(sampled.valid());
        assertFalse(sampled.exact());
        assertTrue(sampled.hasWitness());
        assertTrue(sampled.estimatedViolationRate() > 0.1);
    }
}
