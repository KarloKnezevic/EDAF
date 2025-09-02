package com.knezevic.edaf.factory.population;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Population;

/**
 * A factory for creating {@link Population} objects.
 */
public interface PopulationFactory {
    /**
     * Creates a {@link Population} instance.
     *
     * @param config The configuration.
     * @param genotype The genotype.
     * @return A {@link Population} instance.
     * @throws Exception If an error occurs while creating the population.
     */
    Population create(Configuration config, Genotype genotype) throws Exception;
}
