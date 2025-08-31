package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;

import java.util.Random;

public interface GenotypeFactory {
    Genotype create(Configuration config, Random random) throws Exception;
}
