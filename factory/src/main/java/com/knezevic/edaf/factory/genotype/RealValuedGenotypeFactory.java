package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.realvalued.RealValuedGenotype;

import java.util.Random;

/**
 * A factory for creating {@link RealValuedGenotype} objects.
 */
public class RealValuedGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        return new RealValuedGenotype(
                config.getProblem().getGenotype().getLength(),
                config.getProblem().getGenotype().getLowerBound(),
                config.getProblem().getGenotype().getUpperBound(),
                1.0,  // default initial sigma
                random
        );
    }
}
