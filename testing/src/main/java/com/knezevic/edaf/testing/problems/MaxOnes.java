package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

/**
 * The MaxOnes problem. The goal is to find a binary string with all ones.
 * The fitness is the number of ones in the string.
 */
public class MaxOnes implements Problem<BinaryIndividual> {

    @Override
    public void evaluate(BinaryIndividual individual) {
        int ones = 0;
        for (byte b : individual.getGenotype()) {
            if (b == 1) {
                ones++;
            }
        }
        // We want to maximize the number of ones, but the framework is designed
        // to minimize the fitness. So, the fitness is the number of zeros.
        individual.setFitness(individual.getGenotype().length - ones);
    }
}
