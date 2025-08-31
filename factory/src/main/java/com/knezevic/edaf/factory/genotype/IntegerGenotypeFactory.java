package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.integer.IntegerGenotype;

import java.util.Random;

public class IntegerGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        return new IntegerGenotype(config.getProblem().getGenotype().getLength(),
                config.getProblem().getGenotype().getMinBound(),
                config.getProblem().getGenotype().getMaxBound(), random);
    }
}
