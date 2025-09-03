package com.knezevic.edaf.examples.crypto;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.tree.TreeIndividual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A problem that defines the optimization of boolean functions using Genetic Programming.
 */
public class BooleanFunctionGPProblem implements Problem<TreeIndividual> {

    private final List<FitnessCriterion> criteria;
    private final int n;
    private final List<String> terminalNames;

    public BooleanFunctionGPProblem(Map<String, Object> params) {
        if (params == null || !params.containsKey("n")) {
            throw new IllegalArgumentException("Parameter 'n' (number of variables) must be provided.");
        }
        this.n = (int) params.get("n");

        if (!params.containsKey("criteria")) {
            throw new IllegalArgumentException("Parameter 'criteria' must be provided.");
        }

        List<String> criteriaNames = (List<String>) params.get("criteria");
        this.criteria = new ArrayList<>();
        for (String name : criteriaNames) {
            switch (name.toLowerCase()) {
                case "balancedness":
                    this.criteria.add(new Balancedness(n));
                    break;
                case "nonlinearity":
                    this.criteria.add(new Nonlinearity(n));
                    break;
                case "algebraicdegree":
                    this.criteria.add(new AlgebraicDegree(n));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown criterion: " + name);
            }
        }

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

        double totalFitness = 0;
        for (FitnessCriterion criterion : criteria) {
            totalFitness += criterion.compute(truthTable);
        }

        individual.setFitness(totalFitness);
    }
}
