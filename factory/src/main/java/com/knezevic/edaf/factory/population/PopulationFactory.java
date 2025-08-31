package com.knezevic.edaf.factory.population;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Population;

public interface PopulationFactory {
    Population create(Configuration config, Genotype genotype) throws Exception;
}
