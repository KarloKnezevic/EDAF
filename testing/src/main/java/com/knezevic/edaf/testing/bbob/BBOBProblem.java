package com.knezevic.edaf.testing.bbob;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.genotype.fp.FpIndividual;

/**
 * A wrapper for the BBOB benchmark problems.
 */
public class BBOBProblem implements Problem<FpIndividual> {

    private final BBOB bbob;

    public BBOBProblem(int benchmarkId, int instanceId, int dimension) {
        this.bbob = new BBOB(benchmarkId, instanceId, dimension);
        this.bbob.init();
    }

    @Override
    public void evaluate(FpIndividual individual) {
        double fitness = bbob.evaluate(individual.getGenotype());
        individual.setFitness(fitness);
    }
}
