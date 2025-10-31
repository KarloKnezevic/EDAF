package com.knezevic.edaf.testing.problems.crypto;

import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import java.util.Map;

public class BooleanFunctionProblem extends AbstractBooleanFunctionProblem<BinaryIndividual> {
    public BooleanFunctionProblem(Map<String, Object> params) { super(params); }

    @Override
    public void evaluate(BinaryIndividual individual) {
        byte[] genotype = individual.getGenotype();
        int[] function = new int[genotype.length];
        for (int i = 0; i < genotype.length; i++) {
            function[i] = genotype[i];
        }
        double totalFitness = calculateFitness(function);
        individual.setFitness(totalFitness);
    }
}


