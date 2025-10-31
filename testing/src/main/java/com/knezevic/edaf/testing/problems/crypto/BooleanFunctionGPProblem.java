package com.knezevic.edaf.testing.problems.crypto;

import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import java.util.*;

public class BooleanFunctionGPProblem extends AbstractProblem<TreeIndividual> {
    private final List<String> terminalNames;
    private final int n;
    public BooleanFunctionGPProblem(Map<String, Object> params) {
        super(params);
        this.n = (int) params.get("n");
        this.terminalNames = new ArrayList<>();
        for (int i = 0; i < n; i++) terminalNames.add("x" + i);
    }

    @Override
    public void evaluate(TreeIndividual individual) {
        int[] truthTable = new int[1 << n];
        for (int i = 0; i < (1 << n); i++) {
            Map<String, Double> terminals = new HashMap<>();
            for (int j = 0; j < n; j++) terminals.put(terminalNames.get(j), (double) ((i >> j) & 1));
            truthTable[i] = individual.getGenotype().evaluate(terminals) >= 0.5 ? 1 : 0;
        }
        double totalFitness = new Nonlinearity(n).compute(truthTable) + new Balancedness(n).compute(truthTable);
        individual.setFitness(totalFitness);
    }
}


