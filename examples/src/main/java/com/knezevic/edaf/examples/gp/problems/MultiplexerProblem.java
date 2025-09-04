package com.knezevic.edaf.examples.gp.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.HashMap;
import java.util.Map;

/**
 * The 6-bit multiplexer problem.
 * Inputs:
 * - 2 address bits (a0, a1)
 * - 4 data bits (d0, d1, d2, d3)
 * The goal is to evolve a function that returns the correct data bit based on the address bits.
 * The address bits select one of the data bits:
 * a1 a0 | output
 * -----|-------
 * 0  0 | d0
 * 0  1 | d1
 * 1  0 | d2
 * 1  1 | d3
 */
public class MultiplexerProblem extends AbstractProblem<TreeIndividual> {

    public MultiplexerProblem(Map<String, Object> params) {
        super(params);
    }

    @Override
    public void evaluate(TreeIndividual individual) {
        int incorrect = 0;
        // Iterate through all 2^6 = 64 possible inputs
        for (int i = 0; i < 64; i++) {
            Map<String, Double> terminals = new HashMap<>();
            // Extract the 6 bits from the integer i
            double a1 = (i & 32) == 0 ? 0.0 : 1.0;
            double a0 = (i & 16) == 0 ? 0.0 : 1.0;
            double d3 = (i & 8) == 0 ? 0.0 : 1.0;
            double d2 = (i & 4) == 0 ? 0.0 : 1.0;
            double d1 = (i & 2) == 0 ? 0.0 : 1.0;
            double d0 = (i & 1) == 0 ? 0.0 : 1.0;

            terminals.put("A1", a1);
            terminals.put("A0", a0);
            terminals.put("D3", d3);
            terminals.put("D2", d2);
            terminals.put("D1", d1);
            terminals.put("D0", d0);

            double expectedOutput;
            if (a1 == 0.0 && a0 == 0.0) {
                expectedOutput = d0;
            } else if (a1 == 0.0 && a0 == 1.0) {
                expectedOutput = d1;
            } else if (a1 == 1.0 && a0 == 0.0) {
                expectedOutput = d2;
            } else { // a1 == 1.0 && a0 == 1.0
                expectedOutput = d3;
            }

            double actualOutput = individual.getGenotype().evaluate(terminals) >= 0.5 ? 1.0 : 0.0;

            if (actualOutput != expectedOutput) {
                incorrect++;
            }
        }
        individual.setFitness(incorrect);
    }
}
