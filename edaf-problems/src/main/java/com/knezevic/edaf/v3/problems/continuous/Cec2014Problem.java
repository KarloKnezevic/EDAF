package com.knezevic.edaf.v3.problems.continuous;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.problems.continuous.cec.Cec2014Functions;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.List;

/**
 * CEC 2014-style continuous benchmark adapter.
 */
public final class Cec2014Problem implements Problem<RealVector> {

    private final int functionId;
    private final int dimension;
    private final int instanceId;

    public Cec2014Problem(int functionId, int dimension, int instanceId) {
        this.functionId = functionId;
        this.dimension = dimension;
        this.instanceId = instanceId;
    }

    @Override
    public String name() {
        return "cec2014-f" + functionId + "-d" + dimension + "-i" + instanceId;
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MINIMIZE;
    }

    @Override
    public Fitness evaluate(RealVector genotype) {
        int n = Math.min(dimension, genotype.length());
        double[] clipped = new double[n];
        System.arraycopy(genotype.values(), 0, clipped, 0, n);
        return new ScalarFitness(Cec2014Functions.evaluate(functionId, clipped, instanceId));
    }

    @Override
    public List<String> violations(RealVector genotype) {
        if (genotype.length() != dimension) {
            return List.of("Expected dimension " + dimension + ", got " + genotype.length());
        }
        return List.of();
    }
}
