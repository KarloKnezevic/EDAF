package com.knezevic.edaf.testing.bbob;

import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.impl.AbstractProblem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

import java.util.Map;

/**
 * A wrapper for the BBOB benchmark problems.
 */
public class BBOBProblem extends AbstractProblem<FpIndividual> {

    private final BBOB bbob;

    public BBOBProblem(Map<String, Object> params) {
        super(params);
        int benchmarkId = (int) params.get("benchmarkId");
        int instanceId = (int) params.get("instanceId");
        int dimension = (int) params.get("dimension");
        this.bbob = new BBOB(benchmarkId, instanceId, dimension);
        this.bbob.init();
    }

    @Override
    public void evaluate(FpIndividual individual) {
        double fitness = bbob.evaluate(individual.getGenotype());
        individual.setFitness(fitness);
    }
}
