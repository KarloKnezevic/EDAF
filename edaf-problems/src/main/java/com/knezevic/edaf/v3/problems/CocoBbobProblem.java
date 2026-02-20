package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.problems.coco.BbobFunctions;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;

/**
 * COCO/BBOB benchmark problem adapter.
 */
public final class CocoBbobProblem implements Problem<RealVector> {

    private final String suite;
    private final int functionId;
    private final int dimension;
    private final int instanceId;

    public CocoBbobProblem(String suite, int functionId, int dimension, int instanceId) {
        this.suite = suite;
        this.functionId = functionId;
        this.dimension = dimension;
        this.instanceId = instanceId;
    }

    @Override
    public String name() {
        return suite + "-f" + functionId + "-d" + dimension + "-i" + instanceId;
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        double[] values = genotype.values();
        int n = Math.min(values.length, dimension);
        double[] clipped = new double[n];
        System.arraycopy(values, 0, clipped, 0, n);
        double result = BbobFunctions.evaluate(functionId, clipped, instanceId);
        return new ScalarFitness(result);
    }

    @Override
    public List<String> violations(RealVector genotype) {
        if (genotype.length() != dimension) {
            return List.of("Expected dimension " + dimension + ", got " + genotype.length());
        }
        return List.of();
    }
}
