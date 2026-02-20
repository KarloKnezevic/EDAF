package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * Boolean-function optimization over direct truth-table bitstring encoding.
 */
public final class BooleanFunctionProblem extends AbstractBooleanFunctionProblem<BitString> {

    public BooleanFunctionProblem(int n, List<String> criteria, Map<String, Double> criterionWeights) {
        super(n, criteria, criterionWeights);
    }

    @Override
    public String name() {
        return "boolean-function";
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        return evaluateScalarFitness(toTruthTableFromBits(genotype.genes()));
    }

    @Override
    public List<String> violations(BitString genotype) {
        if (genotype.length() != truthTableSize) {
            return List.of("Bitstring length must be 2^n = " + truthTableSize);
        }
        return List.of();
    }
}
