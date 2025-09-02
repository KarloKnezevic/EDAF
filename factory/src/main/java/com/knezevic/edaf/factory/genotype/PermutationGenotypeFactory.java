package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.genotype.permutation.PermutationGenotype;

import java.util.Random;

public class PermutationGenotypeFactory implements GenotypeFactory {
    @Override
    public Genotype create(Configuration config, Random random) throws Exception {
        return new PermutationGenotype(config.getProblem().getGenotype().getLength(),
                random);
    }
}
