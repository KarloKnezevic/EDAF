package hr.fer.zemris.edaf.testing.bbob;

import hr.fer.zemris.edaf.core.api.*;
import hr.fer.zemris.edaf.core.impl.*;
import hr.fer.zemris.edaf.genotype.fp.FpIndividual;

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
