package com.knezevic.edaf.examples.misc;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

/**
 * Implements the Rastrigin function, a common benchmark for optimization algorithms.
 * It is a non-convex function with many local minima, making it challenging to optimize.
 * The global minimum is f(x) = 0, located at x_i = 0 for all dimensions.
 * The function is typically evaluated on the hypercube x_i in [-5.12, 5.12].
 */
public class RastriginProblem extends AbstractProblem<FpIndividual> {

    public RastriginProblem(Map<String, Object> params) {
        super(params);
    }

    @Override
    public void evaluate(FpIndividual individual) {
        double[] x = individual.getGenotype();
        int n = x.length;
        double rastriginValue = 10.0 * n;

        for (int i = 0; i < n; i++) {
            rastriginValue += Math.pow(x[i], 2) - 10.0 * Math.cos(2 * Math.PI * x[i]);
        }

        individual.setFitness(rastriginValue);
    }
}
