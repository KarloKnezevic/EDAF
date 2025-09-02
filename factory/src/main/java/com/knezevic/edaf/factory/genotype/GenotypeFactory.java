package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;

import java.util.Random;

/**
 * A factory for creating {@link Genotype} objects.
 */
public interface GenotypeFactory {
    /**
     * Creates a {@link Genotype} instance.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Genotype} instance.
     * @throws Exception If an error occurs while creating the genotype.
     */
    Genotype create(Configuration config, Random random) throws Exception;
}
