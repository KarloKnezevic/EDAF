package com.knezevic.edaf.testing.problems;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.integer.IntegerIndividual;

import java.util.Map;

public class IntTarget extends AbstractProblem<IntegerIndividual> {

    private final int target;

    public IntTarget(Map<String, Object> params) {
        super(params);
        this.target = (int) params.get("target");
    }

    @Override
    public void evaluate(IntegerIndividual individual) {
        int[] genotype = individual.getGenotype();
        double fitness = 0;
        for (int gene : genotype) {
            fitness += Math.abs(gene - target);
        }
        individual.setFitness(fitness);
    }
}
