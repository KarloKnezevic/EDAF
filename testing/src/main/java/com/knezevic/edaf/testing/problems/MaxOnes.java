package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Map;

/**
 * The MaxOnes problem. The goal is to find a binary string with all ones.
 * The fitness is the number of ones in the string. This is a maximization problem.
 */
public class MaxOnes extends AbstractProblem<BinaryIndividual> {

    public MaxOnes(Map<String, Object> params) {
        super(params);
    }

    @Override
    public void evaluate(BinaryIndividual individual) {
        int ones = 0;
        for (byte b : individual.getGenotype()) {
            if (b == 1) {
                ones++;
            }
        }
        individual.setFitness(ones);
    }
}
