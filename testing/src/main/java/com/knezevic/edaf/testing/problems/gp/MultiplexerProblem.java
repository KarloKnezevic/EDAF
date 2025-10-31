package com.knezevic.edaf.testing.problems.gp;

import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.HashMap;
import java.util.Map;

public class MultiplexerProblem extends AbstractProblem<TreeIndividual> {
    public MultiplexerProblem(java.util.Map<String, Object> params) { super(params); }

    @Override
    public void evaluate(TreeIndividual individual) {
        int incorrect = 0;
        for (int i = 0; i < 64; i++) {
            Map<String, Double> terminals = new HashMap<>();
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
            double expectedOutput = (a1 == 0.0 && a0 == 0.0) ? d0 :
                    (a1 == 0.0 && a0 == 1.0) ? d1 :
                            (a1 == 1.0 && a0 == 0.0) ? d2 : d3;
            double actualOutput = individual.getGenotype().evaluate(terminals) >= 0.5 ? 1.0 : 0.0;
            if (actualOutput != expectedOutput) incorrect++;
        }
        individual.setFitness(incorrect);
    }
}


