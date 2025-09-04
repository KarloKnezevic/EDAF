package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A problem that defines the optimization of boolean functions using Genetic Programming.
 */
public class BooleanFunctionGPProblem extends AbstractBooleanFunctionProblem<TreeIndividual> {

    private final List<String> terminalNames;

    public BooleanFunctionGPProblem(Map<String, Object> params) {
        super(params);
        this.terminalNames = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            terminalNames.add("x" + i);
        }
    }

    @Override
    public void evaluate(TreeIndividual individual) {
        int[] truthTable = new int[1 << n];
        for (int i = 0; i < (1 << n); i++) {
            Map<String, Double> terminals = new HashMap<>();
            for (int j = 0; j < n; j++) {
                terminals.put(terminalNames.get(j), (double) ((i >> j) & 1));
            }
            truthTable[i] = individual.getGenotype().evaluate(terminals) >= 0.5 ? 1 : 0;
        }

        double totalFitness = calculateFitness(truthTable);
        individual.setFitness(totalFitness);
    }
}
