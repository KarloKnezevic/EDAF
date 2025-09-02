package com.knezevic.edaf.factory.statistics;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.fp.FpGenotype;
import com.knezevic.edaf.statistics.distribution.BernoulliDistribution;
import com.knezevic.edaf.statistics.distribution.NormalDistribution;
import com.knezevic.edaf.statistics.mimic.MimicStatistics;

import java.util.Random;

/**
 * A default implementation of the {@link StatisticsFactory} interface.
 */
public class DefaultStatisticsFactory implements StatisticsFactory {
    @Override
    public Statistics create(Configuration config, Genotype genotype, Random random) throws Exception {
        String algorithmName = config.getAlgorithm().getName();
        if ("umda".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new BernoulliDistribution(genotype, random);
            } else if (genotype instanceof FpGenotype) {
                return new NormalDistribution(genotype.getLength(), random);
            }
        } else if ("mimic".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new MimicStatistics(genotype, random);
            }
        }
        return null;
    }
}
