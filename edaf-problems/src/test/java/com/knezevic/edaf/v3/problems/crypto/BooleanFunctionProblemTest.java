package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.repr.types.BitString;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the boolean-function crypto problem variants.
 */
class BooleanFunctionProblemTest {

    @Test
    void scalarProblemUsesConfiguredCriteriaAggregation() {
        BooleanFunctionProblem problem = new BooleanFunctionProblem(
                2,
                List.of("balancedness", "nonlinearity", "algebraic-degree"),
                Map.of()
        );

        double fitness = problem.evaluate(new BitString(new boolean[]{false, true, true, false})).scalar();
        // XOR scores: balancedness=1.0, nonlinearity=0.0, algebraic-degree=0.5, average=0.5.
        assertEquals(0.5, fitness, 1e-9);
    }

    @Test
    void permutationVariantIsBalancedByConstruction() {
        BooleanFunctionPermutationProblem problem = new BooleanFunctionPermutationProblem(
                2,
                List.of("balancedness"),
                Map.of("balancedness", 1.0)
        );

        double fitness = problem.evaluate(new PermutationVector(new int[]{0, 1, 2, 3})).scalar();
        assertEquals(1.0, fitness, 1e-9);
    }

    @Test
    void multiObjectiveVariantReturnsVectorFitness() {
        BooleanFunctionMultiObjectiveProblem problem = new BooleanFunctionMultiObjectiveProblem(
                2,
                List.of("balancedness", "nonlinearity", "algebraic-degree"),
                Map.of(),
                new double[]{0.2, 0.3, 0.5}
        );

        var fitness = problem.evaluate(new BitString(new boolean[]{false, true, true, false}));
        assertEquals(3, fitness.objectives().length);
        assertEquals(1.0, fitness.objectives()[0], 1e-9);
        assertEquals(0.0, fitness.objectives()[1], 1e-9);
        assertEquals(0.5, fitness.objectives()[2], 1e-9);
        assertEquals(0.45, fitness.scalar(), 1e-9);
    }

    @Test
    void treeVariantEvaluatesTokenizedPrefixExpression() {
        BooleanFunctionTreeProblem problem = new BooleanFunctionTreeProblem(
                2,
                List.of("balancedness"),
                Map.of("balancedness", 1.0),
                6
        );

        // variables=2, xor op token = variables + 5 => 7; expression: xor(x0,x1)
        var tokens = new VariableLengthVector<>(List.of(7, 0, 1));
        double fitness = problem.evaluate(tokens).scalar();
        assertEquals(1.0, fitness, 1e-9);
    }

    @Test
    void violationsContainActionableMessage() {
        BooleanFunctionProblem problem = new BooleanFunctionProblem(3, List.of("balancedness"), Map.of());

        var violations = problem.violations(new BitString(new boolean[]{true, false, true, false}));
        assertTrue(violations.getFirst().contains("2^n = 8"));
    }
}
