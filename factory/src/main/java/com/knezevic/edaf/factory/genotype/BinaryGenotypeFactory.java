package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;

import java.util.Random;

/**
 * A factory for creating {@link BinaryGenotype} objects.
 */
public class BinaryGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        return new BinaryGenotype(config.getProblem().getGenotype().getLength(), random);
    }
}
