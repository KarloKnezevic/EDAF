package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;

public class GenotypeFactoryProvider {
    public static GenotypeFactory getFactory(Configuration config) {
        String type = config.getProblem().getGenotype().getType();
        if ("binary".equals(type)) {
            return new BinaryGenotypeFactory();
        } else if ("fp".equals(type)) {
            return new FpGenotypeFactory();
        } else if ("integer".equals(type)) {
            return new IntegerGenotypeFactory();
        }
        return null;
    }
}
