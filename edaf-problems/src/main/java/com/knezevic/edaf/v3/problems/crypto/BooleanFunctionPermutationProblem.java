package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;
import java.util.Map;

/**
 * Balanced boolean-function optimization with permutation encoding.
 *
 * <p>The first half of permutation positions are interpreted as 1-valued truth-table rows,
 * guaranteeing balancedness by construction when permutation size is exactly 2^n.</p>
 */
public final class BooleanFunctionPermutationProblem extends AbstractBooleanFunctionProblem<PermutationVector> {

    public BooleanFunctionPermutationProblem(int n, List<String> criteria, Map<String, Double> criterionWeights) {
        super(n, criteria, criterionWeights);
    }

    @Override
    public String name() {
        return "boolean-function-permutation";
    }

    @Override
    public Fitness evaluate(PermutationVector genotype) {
        return evaluateScalarFitness(toTruthTableFromBalancedPermutation(genotype.order()));
    }

    @Override
    public List<String> violations(PermutationVector genotype) {
        if (genotype.size() != truthTableSize) {
            return List.of("Permutation size must be 2^n = " + truthTableSize);
        }
        return List.of();
    }
}
