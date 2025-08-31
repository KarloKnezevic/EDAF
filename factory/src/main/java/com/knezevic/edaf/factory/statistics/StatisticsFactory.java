package com.knezevic.edaf.factory.statistics;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Statistics;

import java.util.Random;

public interface StatisticsFactory {
    Statistics create(Configuration config, Genotype genotype, Random random) throws Exception;
}
