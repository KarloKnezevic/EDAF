package com.knezevic.edaf.factory.statistics;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Statistics;

import java.util.Random;

/**
 * A factory for creating {@link Statistics} objects.
 */
public interface StatisticsFactory {
    /**
     * Creates a {@link Statistics} instance.
     *
     * @param config The configuration.
     * @param genotype The genotype.
     * @param random The random number generator.
     * @return A {@link Statistics} instance.
     * @throws Exception If an error occurs while creating the statistics.
     */
    Statistics create(Configuration config, Genotype genotype, Random random) throws Exception;
}
