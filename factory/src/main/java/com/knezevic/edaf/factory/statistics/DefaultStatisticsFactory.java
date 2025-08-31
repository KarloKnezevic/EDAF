package com.knezevic.edaf.factory.statistics;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.fp.FpGenotype;
import com.knezevic.edaf.statistics.bmda.BmdaBinaryStatistics;
import com.knezevic.edaf.statistics.mimic.MimicStatistics;
import com.knezevic.edaf.statistics.umda.UmdaBinaryStatistics;
import com.knezevic.edaf.statistics.umda.UmdaFpStatistics;

import java.util.Random;

public class DefaultStatisticsFactory implements StatisticsFactory {
    @Override
    public Statistics create(Configuration config, Genotype genotype, Random random) throws Exception {
        String algorithmName = config.getAlgorithm().getName();
        if ("umda".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new UmdaBinaryStatistics(genotype, random);
            } else if (genotype instanceof FpGenotype) {
                return new UmdaFpStatistics(genotype, random);
            }
        } else if ("mimic".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new MimicStatistics(genotype, random);
            }
        } else if ("bmda".equals(algorithmName)) {
            if (genotype instanceof BinaryGenotype) {
                return new BmdaBinaryStatistics(genotype, random);
            }
        }
        return null;
    }
}
