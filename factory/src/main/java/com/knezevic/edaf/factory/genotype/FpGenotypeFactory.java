package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.fp.FpGenotype;

import java.util.Random;

public class FpGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        return new FpGenotype(config.getProblem().getGenotype().getLength(),
                config.getProblem().getGenotype().getLowerBound(),
                config.getProblem().getGenotype().getUpperBound(), random);
    }
}
