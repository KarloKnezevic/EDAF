package com.knezevic.edaf.factory.population;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryGenotype;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import com.knezevic.edaf.genotype.fp.FpGenotype;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import com.knezevic.edaf.genotype.integer.IntegerGenotype;
import com.knezevic.edaf.genotype.integer.IntegerIndividual;

public class DefaultPopulationFactory implements PopulationFactory {
    @Override
    public Population create(Configuration config, Genotype genotype) throws Exception {
        Population population = new SimplePopulation();
        for (int i = 0; i < config.getAlgorithm().getPopulation().getSize(); i++) {
            if (genotype instanceof BinaryGenotype) {
                population.add(new BinaryIndividual((byte[]) genotype.create()));
            } else if (genotype instanceof FpGenotype) {
                population.add(new FpIndividual((double[]) genotype.create()));
            } else if (genotype instanceof IntegerGenotype) {
                population.add(new IntegerIndividual((int[]) genotype.create()));
            }
        }
        return population;
    }
}
