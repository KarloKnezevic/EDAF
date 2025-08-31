package com.knezevic.edaf.testing;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.fp.FpIndividual;

public interface ContinuousProblem extends Problem<FpIndividual> {
    FpIndividual createIndividual(double[] genotype);
}
